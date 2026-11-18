package com.tecsup.tecsupplus.data.remote

import com.tecsup.tecsupplus.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqChatRequest(
    val model: String = "openai/gpt-oss-20b",
    val messages: List<GroqMessage>,
    val temperature: Double = 0.5,
    val max_tokens: Int = 2048
)

data class GroqChoice(
    val message: GroqMessage?
)

data class GroqChatResponse(
    val choices: List<GroqChoice>?
)

data class GroqModelInfo(
    val id: String
)

data class GroqModelsResponse(
    val data: List<GroqModelInfo>?
)

interface GroqRetrofitApi {
    @POST("openai/v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse

    @GET("openai/v1/models")
    suspend fun listarModelos(
        @Header("Authorization") authorization: String
    ): GroqModelsResponse
}

object GroqClient {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val api: GroqRetrofitApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqRetrofitApi::class.java)
    }

    suspend fun consultarGroq(
        promptSistema: String,
        promptUsuario: String,
        temperatura: Double = 0.5
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY.trim().removeSurrounding("\"")
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("GROQ_API_KEY vacía"))
        }

        val authHeader = "Bearer $apiKey"
        val modelosCandidatos = linkedSetOf(
            "openai/gpt-oss-20b",
            "openai/gpt-oss-120b",
            "qwen/qwen3.8-27b",
            "llama-3.1-8b-instant"
        )

        // Intentar primero con los modelos activos; si alguno falla, consultar /v1/models dinámicamente
        try {
            val disponibles = api.listarModelos(authHeader).data
                ?.map { it.id }
                ?.filter { !it.contains("whisper", true) && !it.contains("guard", true) && !it.contains("orpheus", true) }
                .orEmpty()
            modelosCandidatos.addAll(disponibles)
        } catch (_: Exception) { }

        var ultimoError = "No se encontró un modelo activo en Groq"
        for (modelo in modelosCandidatos) {
            try {
                val respuesta = api.chatCompletions(
                    authorization = authHeader,
                    request = GroqChatRequest(
                        model = modelo,
                        messages = listOf(
                            GroqMessage(role = "system", content = promptSistema),
                            GroqMessage(role = "user", content = promptUsuario)
                        ),
                        temperature = temperatura
                    )
                )
                val texto = respuesta.choices?.firstOrNull()?.message?.content?.trim()
                if (!texto.isNullOrBlank()) {
                    return@withContext Result.success(texto)
                }
            } catch (e: HttpException) {
                val detalle = e.response()?.errorBody()?.string() ?: e.message()
                ultimoError = "HTTP ${e.code()}: $detalle"
            } catch (e: Exception) {
                ultimoError = e.message ?: "Error de red"
            }
        }
        Result.failure(IllegalStateException(ultimoError))
    }
}
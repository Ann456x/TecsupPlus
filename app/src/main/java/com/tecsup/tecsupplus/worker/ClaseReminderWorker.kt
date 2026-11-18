package com.tecsup.tecsupplus.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tecsup.tecsupplus.util.NotificationHelper
import java.util.concurrent.TimeUnit

class ClaseReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.enviarNotificacionPush(
            applicationContext,
            "Alerta de Clase TecsupPlus (15 min antes)",
            "Tu clase empieza pronto. Revisa tu pabellón asignado en el Plano Oficial del Campus."
        )
        return Result.success()
    }

    companion object {
        fun programarRecordatorios(context: Context) {
            val request = PeriodicWorkRequestBuilder<ClaseReminderWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "tecsup_clase_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
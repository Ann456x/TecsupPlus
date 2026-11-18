package com.tecsup.tecsupplus.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.tecsup.tecsupplus.R

class TecsupWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_tecsup_plus)
            views.setTextViewText(R.id.widget_titulo, "TecsupPlus Arequipa")
            views.setTextViewText(R.id.widget_detalle, "Próxima Clase: 18:50 pm • Pabellón A / C\nPromedio Ponderado Activo")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
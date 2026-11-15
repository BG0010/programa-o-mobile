package com.todolist.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.todolist.app.MainActivity
import com.todolist.app.R

object ReminderNotifications {
    private const val CHANNEL_ID = "task_reminders"

    /** O canal precisa existir antes da primeira notificação (Android 8+). */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Lembretes de tarefas", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Avisos antes do prazo das suas tarefas"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** No Android 13+ a permissão é pedida ao usuário; nas versões anteriores é concedida na instalação. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, taskId: Long, title: String, text: String) {
        if (!hasPermission(context)) return
        val openApp = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permissão revogada entre a checagem e o envio: ignora silenciosamente.
        }
    }
}

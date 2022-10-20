package com.ume.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationBuilder(
    var text: Text,
    var icon: Icon,
    val channel: ChannelConfig,
    var bodyAction: Action? = null,
    var progress: Progress? = null,
    var style: Style? = null,
    var actions: List<ClickableAction>? = null,
    var ongoing: Boolean = false
) {

    fun createNotification(
        context: Context,
        configure: ((NotificationCompat.Builder) -> Unit)? = null
    ): Notification {
        val mn = NotificationManagerCompat.from(context)
        createChannel(channel, mn)
        val builder = NotificationCompat.Builder(context, channel.id)
            .setContentTitle(text.title)
            .setContentText(text.body)
            .setSmallIcon(icon.smallIcon)
            .setLargeIcon(icon.largeIcon)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)

        when (val s = style) {
            is BigText -> {
                builder.setStyle(
                    NotificationCompat.BigTextStyle(builder)
                        .setBigContentTitle(text.title)
                        .bigText(text.body)
                )
            }

            is BigImage -> {
                val bigPictureStyle = NotificationCompat.BigPictureStyle(builder)
                    .setBigContentTitle(text.title)
                    .setSummaryText(text.body)
                    .bigLargeIcon(icon.largeIcon)
                    .bigPicture(s.image)
                if (sdkAtLeast(Build.VERSION_CODES.S)) {
                    bigPictureStyle.showBigPictureWhenCollapsed(true)
                }
                builder.setStyle(bigPictureStyle)
            }
        }

        progress?.let { progress ->
            builder.setProgress(progress.max, progress.progress, progress.indeterminate)
            builder.setOngoing(progress.progress < progress.max)
        }

        bodyAction?.let { intent ->
            builder.setContentIntent(getPendingIntent(intent, context))
        }

        actions?.let {
            it.forEach { action ->
                builder.addAction(
                    NotificationCompat.Action.Builder(
                        action.icon,
                        action.text,
                        getPendingIntent(action, context)
                    ).build()
                )
            }
        }

        configure?.let { it(builder) }

        return builder.build()
    }

    @SuppressLint("WrongConstant")
    private fun createChannel(config: ChannelConfig, mn: NotificationManagerCompat) {
        if (sdkAtLeast(Build.VERSION_CODES.O)) {
            val channel = NotificationChannel(
                config.id, config.name,
                config.importance
            )
                .apply {
                    description = config.desc
                }
            mn.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(
        intent: Action,
        context: Context
    ): PendingIntent {
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or
                if (sdkAtLeast(Build.VERSION_CODES.M)) PendingIntent.FLAG_IMMUTABLE else 0
        return when (intent.type) {
            ActionType.SERVICE -> PendingIntent.getService(
                context, intent.code,
                intent.intent, flag
            )
            ActionType.ACTIVITY -> PendingIntent.getActivity(
                context, intent.code, intent.intent,
                flag
            )
            ActionType.BROADCAST -> PendingIntent.getBroadcast(
                context, intent.code, intent.intent,
                flag
            )
        }
    }

    @ChecksSdkIntAtLeast(parameter = 0)
    private fun sdkAtLeast(sdk: Int): Boolean {
        return Build.VERSION.SDK_INT >= sdk
    }

    class Text(var title: CharSequence? = null, var body: CharSequence? = null)

    class Progress(val progress: Int, val max: Int, val indeterminate: Boolean = false)

    open class Action(
        val type: ActionType, val intent: Intent,
        val code: Int
    )

    class ClickableAction(
        type: ActionType, intent: Intent, code: Int,
        var icon: Int = 0, var text: CharSequence? = null
    ) : Action(type, intent, code)

    class Icon(
        var smallIcon: Int,
        var largeIcon: Bitmap? = null
    )

    enum class ActionType {
        SERVICE, ACTIVITY, BROADCAST
    }

    class ChannelConfig(
        val name: String, val desc: String, val id: String,
        val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
    )

    class BigText : Style

    class BigImage(var image: Bitmap?) : Style

    interface Style
}
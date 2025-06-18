package ru.crmkurgan.main.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.*
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity
import java.io.IOException
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {
    var uid = 0
    var sharedPref: SharedPreferences? = null
    var type = ""
    var from = ""

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val title = data["title"]
            val message = data["body"]
            val pic = data["icon"]
            val all = data["all"]
            from = data["sender"].toString()
            val id = data["id"]
            type = data["type"].toString()
            if (all == "1") {
                ssd(title, message, pic)
                saveToServerAll(title, message, from, id)
            } else {
                try {
                    val uid1 = remoteMessage.data["uid"]?.toInt()
                    val sharedPref: SharedPreferences =
                        getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
                    if (uid1 == sharedPref.getInt(Constants.UID, 0)) {
                        uid = uid1
                        ssd(title, message, pic)
                        if (type == "0") {
                            saveToServer(title, message, from, id)
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun saveToServerAll(title: String?, message: String?, from: String?, id: String?) {
        if (NetworkUtils.isConnected(this)) {
            title?.let {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()
                formBodyBuilder.add(Constants.TITLE, it)
                message?.let { it1 ->
                    formBodyBuilder.add(Constants.MESSAGE, it1)
                    from?.let { it2 ->
                        formBodyBuilder.add(Constants.FROM, it2)
                        id?.let { it3 ->
                            formBodyBuilder.add(Constants.ID, it3)
                            formBodyBuilder.add(Constants.ALL, Constants.ALL)
                            requestBuilder.url(Constants.SAVE_TO_SERVER)
                            requestBuilder.post(formBodyBuilder.build())
                            NetworkUtils.httpClient().newCall(requestBuilder.build())
                                .enqueue(object :
                                    Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                    }

                                    @Throws(IOException::class)
                                    override fun onResponse(call: Call, response: Response) {
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    private fun saveToServer(title: String?, message: String?, from: String?, id: String?) {
        if (NetworkUtils.isConnected(this)) {
            title?.let {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()
                formBodyBuilder.add(Constants.TITLE, it)
                message?.let { it1 ->
                    formBodyBuilder.add(Constants.MESSAGE, it1)
                    from?.let { it2 ->
                        formBodyBuilder.add(Constants.FROM, it2)
                        id?.let { it3 ->
                            formBodyBuilder.add(Constants.ID, it3)
                            formBodyBuilder.add(Constants.UID, uid.toString())
                            requestBuilder.url(Constants.SAVE_TO_SERVER)
                            requestBuilder.post(formBodyBuilder.build())
                            NetworkUtils.httpClient().newCall(requestBuilder.build())
                                .enqueue(object :
                                    Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                    }

                                    @Throws(IOException::class)
                                    override fun onResponse(call: Call, response: Response) {
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    private fun ssd(title: String?, message: String?, pic: String?) {
        Glide.with(this)
            .asBitmap()
            .load(pic)
            .error(R.drawable.ic_profile)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    sendNotification(title, message, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun sendNotification(
        title: String?,
        message: String?,
        bitmap: Bitmap?
    ) {
        val bitmap1 = bitmap?.let {
            Functions.getCircleBitmap(it)
        }
        val intent = NavDeepLinkBuilder(this)
        intent.setComponentName(DashboardActivity::class.java)
        intent.setGraph(R.navigation.home_nav_graph)
        if (type == "0") {
            intent.setDestination(R.id.notificationFragment)
        } else {
            intent.setDestination(R.id.chatPersonFragment)
            val bundle = bundleOf(
                Pair(Constants.AGENT, from)
            )
            intent.setArguments(bundle)
        }
        val pendingIntent = intent.createPendingIntent()
        if (Build.VERSION.SDK_INT < 26) {
            val groupBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, Constants.DEFAULT_NOTIFICATION_CHANNEL_ID)
            groupBuilder.setSmallIcon(R.drawable.icon)
            groupBuilder.setContentTitle(title)
            groupBuilder.setContentText(message)
            groupBuilder.setLargeIcon(bitmap1)
            groupBuilder.setGroupSummary(true)
            groupBuilder.setAutoCancel(true)
            groupBuilder.setGroup(Constants.GROUP_WORK)
            groupBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            groupBuilder.setContentIntent(pendingIntent)

            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, Constants.DEFAULT_NOTIFICATION_CHANNEL_ID)
            notificationBuilder.setSmallIcon(R.drawable.icon)
            notificationBuilder.setContentTitle(title)
            notificationBuilder.setContentText(message)
            notificationBuilder.setLargeIcon(bitmap1)
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setDefaults(DEFAULT_SOUND)
            notificationBuilder.setGroup(Constants.GROUP_WORK)
            notificationBuilder.setContentIntent(pendingIntent)
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(0, groupBuilder.build())
            val m = (Date().time / 1000L % Int.MAX_VALUE).toInt()
            notificationManager.notify(m, notificationBuilder.build())
        } else {
            val groupBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, Constants.DEFAULT_NOTIFICATION_CHANNEL_ID)
            groupBuilder.setSmallIcon(R.drawable.icon)
            groupBuilder.setContentTitle(title)
            groupBuilder.setContentText(message)
            groupBuilder.setLargeIcon(bitmap1)
            groupBuilder.setGroupSummary(true)
            groupBuilder.setAutoCancel(true)
            groupBuilder.setGroup(Constants.GROUP_WORK)
            groupBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            groupBuilder.setContentIntent(pendingIntent)

            val ringURI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                Constants.DEFAULT_NOTIFICATION_CHANNEL_ID,
                Constants.DEFAULT_NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            channel.setSound(ringURI, attributes)
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.createNotificationChannel(channel)
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, Constants.DEFAULT_NOTIFICATION_CHANNEL_ID)
            notificationBuilder.setSmallIcon(R.drawable.icon)
            notificationBuilder.setContentTitle(title)
            notificationBuilder.setContentText(message)
            notificationBuilder.setLargeIcon(bitmap1)
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
            notificationBuilder.setOngoing(false)
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setGroup(Constants.GROUP_WORK)
            notificationBuilder.setContentIntent(pendingIntent)
            notificationManager.notify(0, groupBuilder.build())
            val m = (Date().time / 1000L % Int.MAX_VALUE).toInt()
            notificationManager.notify(m, notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sharedPref: SharedPreferences =
            getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val tok = sharedPref.getString(Constants.DEVICE_TOKEN, "null")
        if (tok.equals("null")) {
            val edit = sharedPref.edit()
            edit.putString(Constants.DEVICE_TOKEN, token)
            edit.apply()
            saveToken(token)
        }
    }

    private fun saveToken(token: String) {
        val requestBuilder = Request.Builder()
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add(Constants.UID, uid.toString())
        formBodyBuilder.add(Constants.DEVICE_TOKEN, token)
        formBodyBuilder.add(Constants.SERVICE, "FCM")
        requestBuilder.url(Constants.SAVE_TOKEN)
        requestBuilder.post(formBodyBuilder.build())
        NetworkUtils.httpClient().newCall(requestBuilder.build())
            .enqueue(object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                }
            })
    }
}

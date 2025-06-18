package ru.crmkurgan.main.utils

import android.Manifest.permission
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission
import okhttp3.OkHttpClient
import org.jetbrains.annotations.Contract
import java.util.concurrent.TimeUnit

class NetworkUtils {
    companion object {
        @RequiresPermission(permission.ACCESS_NETWORK_STATE)
        @Suppress("DEPRECATION")
        fun isConnected(context: Context): Boolean {
            val connection =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connection.activeNetworkInfo ?: return false
            return info.isConnected
        }

        @Contract("-> new")
        fun httpClient(): OkHttpClient {
            val seconds = TimeUnit.SECONDS
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(30, seconds)
            builder.readTimeout(30, seconds)
            builder.writeTimeout(30, seconds)
            return builder.build()
        }
    }
}
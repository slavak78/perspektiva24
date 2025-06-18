package ru.crmkurgan.main.ui.form

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import okhttp3.*
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.*


class SplashFragment : Fragment() {

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPref =
            requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val viewWelcome = sharedPref.getInt(Constants.WELCOME, 0)
        val skipping = sharedPref.getInt(Constants.SKIPPING, 0)
        val uid = sharedPref.getInt(Constants.UID, 0)
        val cont = context
        if (AGConnectInstance.getInstance() == null) {
            AGConnectInstance.initialize(requireContext())
        }
        val edit = sharedPref.edit()
        val zone = TimeZone.getDefault().id.toString()
        edit.putString(Constants.ZONE, zone)
        edit.apply()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (viewWelcome == 0) {
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
            } else {
                if (uid == 0) {
                    if (skipping == 1) {
                        val intent = Intent(cont, DashboardActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_startUpFragment)
                    }
                } else {
                    object : Thread() {
                        override fun run() {
                            try {
                                val appId = AGConnectOptionsBuilder().build(requireContext())
                                    .getString("client/app_id")
                                val token = HmsInstanceId.getInstance(requireContext()).getToken(appId, "HCM")
                                if (!TextUtils.isEmpty(token)) {
                                    saveToken(token, uid)
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }.start()
                }
                val intent = Intent(cont, DashboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            null
        } else {
            val window = requireActivity().window
            window.statusBarColor = Color.WHITE
            window.navigationBarColor = Color.WHITE
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            viewSplash(viewWelcome, uid, skipping)
            inflater.inflate(R.layout.fragment_splash, container, false)
        }
    }

    private fun viewSplash(viewWelcome: Int, uid: Int, skipping: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (viewWelcome == 0) {
                findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
            } else {
                if (uid == 0) {
                    if (skipping == 1) {
                        val intent = Intent(context, DashboardActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_startUpFragment)
                    }
                } else {
                    object : Thread() {
                        override fun run() {
                            try {
                                val appId = AGConnectOptionsBuilder().build(requireContext())
                                    .getString("client/app_id")
                                val token = HmsInstanceId.getInstance(requireContext()).getToken(appId, "HCM")
                                if (!TextUtils.isEmpty(token)) {
                                    saveToken(token, uid)
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }.start()
                }
                val intent = Intent(context, DashboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }, 3000)
    }

    private fun saveToken(token: String, uid: Int) {
        val requestBuilder = Request.Builder()
        val formBodyBuilder = FormBody.Builder()
        formBodyBuilder.add(Constants.UID, uid.toString())
        formBodyBuilder.add(Constants.DEVICE_TOKEN, token)
        formBodyBuilder.add(Constants.SERVICE, Constants.HUAWEI)
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

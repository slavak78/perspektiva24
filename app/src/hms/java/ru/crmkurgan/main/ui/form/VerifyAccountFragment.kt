package ru.crmkurgan.main.ui.form

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.alimuzaffar.lib.pin.PinEntryEditText
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.*
import com.huawei.agconnect.auth.PhoneAuthProvider.credentialWithVerifyCode
import com.huawei.agconnect.auth.PhoneAuthProvider.requestVerifyCode
import com.huawei.hms.aaid.HmsInstanceId
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.*

class VerifyAccountFragment : Fragment() {
    private var codeEditText: PinEntryEditText? = null
    var phone = ""
    private var repeatText: TextView? = null
    private var repeatButton: MaterialButton? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_verify_account, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity1.setTitle("")
        val bundle1 = arguments
        bundle1?.let {
            sendVerifyCode(it.getString("phone"))
            phone = it.getString("phone").toString()
        }
        codeEditText = view.findViewById(R.id.code)
        val buttonSubmit = view.findViewById<MaterialButton>(R.id.button_submit)
        buttonSubmit.setOnClickListener {
            if (!NetworkUtils.isConnected(requireContext())) {
                Functions.viewDialog(R.string.nointernet, requireContext())
            } else {
                verifyCodeHms(
                    codeEditText?.text.toString(),
                    bundle1?.getString("phone")
                )
            }
        }
        repeatText = view.findViewById(R.id.repeatText)
        val repeatButton1 = view.findViewById<MaterialButton>(R.id.repeat)
        repeatButton1.setOnClickListener {
            countDownTimer()
            repeatText?.visibility = View.VISIBLE
            it.visibility = View.GONE
            sendVerifyCode(phone)
        }
        repeatButton = repeatButton1
        countDownTimer()
        return view
    }

    private fun countDownTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val tim = if (millisUntilFinished < 10000) {
                    ": 0:0"
                } else {
                    ": 0:"
                }
                repeatText?.text =
                    resources.getString(R.string.repeat1) + tim + millisUntilFinished / 1000
            }

            override fun onFinish() {
                repeatText?.visibility = View.GONE
                repeatButton?.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun verifyCodeHms(code: String, phone: String?) {
        phone?.let { it1 ->
            var phone1 = it1.replace("+7 (", "")
            phone1 = phone1.replace(") ", "")
            phone1 = phone1.replace("-", "")
            val credential = credentialWithVerifyCode(
                Constants.COUNTRY_CODE,
                phone1,
                null,
                code
            )
            AGConnectAuth.getInstance().signIn(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    val requestBuilder = Request.Builder()
                    val formBodyBuilder = FormBody.Builder()
                    formBodyBuilder.add(Constants.PHONE, it1)
                    requestBuilder.url(Constants.VERIFY)
                    requestBuilder.post(formBodyBuilder.build())
                    NetworkUtils.httpClient().newCall(requestBuilder.build())
                        .enqueue(object :
                            Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                requireActivity().runOnUiThread {
                                    Functions.viewDialog(
                                        R.string.noserver,
                                        requireContext()
                                    )
                                }
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                response.use { resp ->
                                    if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                                    resp.body?.let { body ->
                                        val content = body.string()
                                        requireActivity().runOnUiThread {
                                            processSignUp(
                                                content,
                                                phone
                                            )
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }
    }

    private fun sendVerifyCode(phone: String?) {
        phone?.let { it ->
            val settings = VerifyCodeSettings.newBuilder()
            settings.action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
            settings.sendInterval(30)
            settings.locale(Locale.getDefault())
            val settings1 = settings.build()
            var phone1 = it.replace("+7 (", "")
            phone1 = phone1.replace(") ", "")
            phone1 = phone1.replace("-", "")
            val task = requestVerifyCode(Constants.COUNTRY_CODE, phone1, settings1)
            task.addOnSuccessListener {
            }
            task.addOnFailureListener {
            }
        }
    }

    private fun processSignUp(
        content: String,
        tel: String?
    ) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                val userdata = msg.getJSONObject(0)
                val sharedPref: SharedPreferences =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putInt(Constants.UID, userdata.optInt(Constants.UID))
                editor.putInt(Constants.AGENT, userdata.optInt(Constants.AGENT))
                editor.putString(Constants.PHONE, tel)
                editor.apply()
                object : Thread() {
                    override fun run() {
                        try {
                            val appId = AGConnectOptionsBuilder().build(requireContext())
                                .getString("client/app_id")
                            val token = HmsInstanceId.getInstance(requireContext()).getToken(appId, "HCM")
                            if (!TextUtils.isEmpty(token)) {
                                saveToken(token, userdata.optInt(Constants.UID))
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                }.start()
                val intent = Intent(context, DashboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        } catch (ignored: Exception) {
        }
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

    override fun onDestroy() {
        super.onDestroy()
        codeEditText = null
        repeatText = null
        repeatButton = null
    }
}
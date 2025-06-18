package ru.crmkurgan.main.ui.form

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.alimuzaffar.lib.pin.PinEntryEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.concurrent.TimeUnit


class VerifyAccountFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    var verificationId: String? = null
    private var codeEditText: PinEntryEditText? = null
    var token: PhoneAuthProvider.ForceResendingToken? = null
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
            mAuth = FirebaseAuth.getInstance()
            sendVerificationCode(it.getString("phone"))
            phone = it.getString("phone").toString()
        }
        codeEditText = view.findViewById(R.id.code)
        val buttonSubmit = view.findViewById<MaterialButton>(R.id.button_submit)
        buttonSubmit.setOnClickListener {
            if (!NetworkUtils.isConnected(requireContext())) {
                Functions.viewDialog(R.string.nointernet, requireContext())
            } else {
                verifyCode(
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
            resendVerificationCode(phone)
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

    private fun sendVerificationCode(phone: String?) {
        phone?.let {
            mAuth?.let { it1 ->
                val options = PhoneAuthOptions.newBuilder(it1)
                options.setPhoneNumber(it)
                options.setTimeout(60L, TimeUnit.SECONDS)
                options.setActivity(requireActivity())
                options.setCallbacks(object :
                    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(
                        s: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(s, forceResendingToken)
                        verificationId = s
                        token = forceResendingToken
                    }

                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        val gh = ""
                        val h = gh
                    }
                })
                val options1 = options.build()
                PhoneAuthProvider.verifyPhoneNumber(options1)
            }
        }
    }

    private fun resendVerificationCode(phone: String?) {
        phone?.let {
            mAuth?.let { it1 ->
                token?.let { it2 ->
                    val options = PhoneAuthOptions.newBuilder(it1)
                    options.setPhoneNumber(it)
                    options.setTimeout(60L, TimeUnit.SECONDS)
                    options.setActivity(requireActivity())
                    options.setCallbacks(object :
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onCodeSent(
                            s: String,
                            forceResendingToken: PhoneAuthProvider.ForceResendingToken
                        ) {
                            super.onCodeSent(s, forceResendingToken)
                            verificationId = s
                        }

                        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        }

                        override fun onVerificationFailed(p0: FirebaseException) {
                            val gh = ""
                            val h = gh
                        }
                    })
                    options.setForceResendingToken(it2)
                    val options1 = options.build()
                    PhoneAuthProvider.verifyPhoneNumber(options1)
                }
            }
        }
    }


    private fun verifyCode(code: String, phone: String?) {
        val credential = verificationId?.let { PhoneAuthProvider.getCredential(it, code) }
        signInWithCredential(credential, phone)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential?, phone: String?) {
        mAuth?.let {
            credential?.let { it1 ->
                val res1 = it.signInWithCredential(it1)
                res1.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (!NetworkUtils.isConnected(requireContext())) {
                            Functions.viewDialog(R.string.nointernet, requireContext())
                        } else {
                            phone?.let { it2 ->
                                val requestBuilder = Request.Builder()
                                val formBodyBuilder = FormBody.Builder()
                                formBodyBuilder.add(Constants.PHONE, it2)
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
                                        override fun onResponse(
                                            call: Call,
                                            response: Response
                                        ) {
                                            response.use { resp ->
                                                if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                                                resp.body?.let { body ->
                                                    val content = body.string()
                                                    requireActivity().runOnUiThread {
                                                        processSignUp(
                                                            content,
                                                            it2
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
                val instance = FirebaseMessaging.getInstance()
                instance.subscribeToTopic(Constants.DEFAULT_NOTIFICATION_CHANNEL_ID)
                instance.token.addOnCompleteListener {
                    if (it.isComplete) {
                        val token = it.result.toString()
                        saveToken(token, userdata.optInt(Constants.UID))
                    }
                }
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
        formBodyBuilder.add(Constants.SERVICE, Constants.FCM)
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
        mAuth = null
        verificationId = null
        codeEditText = null
        token = null
        repeatText = null
        repeatButton = null
    }
}
package ru.crmkurgan.main.ui.form

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException

class PolicyFragment : Fragment() {
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var webView: WebView? = null


    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_policy, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = activity1.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.close)
        activity1.setTitle("")
        webView = view.findViewById(R.id.webView)
        progress = view.findViewById(R.id.progress)
        noConnection = view.findViewById(R.id.noConnection)
        getPolicy()
        val repeat = view.findViewById<TextView>(R.id.repeat)
        repeat.setOnClickListener {
            getPolicy()
        }
        return view
    }

    private fun getPolicy() {
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            requestBuilder.url(Constants.POLICY)
            NetworkUtils.httpClient().newCall(requestBuilder.build())
                .enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        requireActivity().runOnUiThread {
                            progress1?.visibility = View.GONE
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                            resp.body?.let { body ->
                                val content = body.string()
                                requireActivity().runOnUiThread {
                                    processPolicy(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processPolicy(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                val userdata = msg.getJSONObject(0)
                val text = userdata.optString(Constants.TEXT_POLICY)
                webView?.loadDataWithBaseURL(null, text, Constants.MIME_TYPE, Constants.ENCODING, null)
            }
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progress = null
        noConnection = null
        webView = null
    }
}
package ru.crmkurgan.main.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.ChatAdapter
import ru.crmkurgan.main.items.Chat
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {
    private val chatList = ArrayList<Chat>()
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var chatAdapter: ChatAdapter? = null
    private var chatRecycler: RecyclerView? = null
    private val timer = Timer()
    private var bottomNavigationView: BottomNavigationView? = null
    private var okHttpClient:OkHttpClient? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        val window = requireActivity().window
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        activity1.title = resources.getString(R.string.chat)
        progress = view.findViewById(R.id.progress)
        noConnection = view.findViewById(R.id.noConnection)
        val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1.visibility = View.VISIBLE
        bottomNavigationView = bottomNavigationView1
        val chatRecycler1 = view.findViewById<RecyclerView>(R.id.recycler_view_chats)
        val chatAdapter1 = ChatAdapter(chatList, requireContext())
        chatRecycler1.adapter = chatAdapter1
        chatRecycler1.layoutManager = LinearLayoutManager(requireContext())
        (chatRecycler1?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        chatRecycler = chatRecycler1
        chatAdapter = chatAdapter1
        okHttpClient = NetworkUtils.httpClient()
        val repeat = view.findViewById<TextView>(R.id.repeat)
        repeat.setOnClickListener {
                getChat()
        }
            timer.schedule(object : TimerTask() {
                override fun run() {
                    requireActivity().runOnUiThread {
                        getChat()
                    }
                }
            }, 0, 10000)
        return view
    }

    private fun getChat() {
        try {
            val progress1 = progress
            if (!NetworkUtils.isConnected(requireContext())) {
                progress1?.visibility = View.GONE
            } else {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()
                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val uid = sharedPref.getInt(Constants.UID, 0)
                val zone = sharedPref.getString(Constants.ZONE, "GMT").toString()
                formBodyBuilder.add(Constants.UID, uid.toString())
                formBodyBuilder.add(Constants.ZONE, zone)
                requestBuilder.url(Constants.CHAT_LIST)
                requestBuilder.post(formBodyBuilder.build())
                okHttpClient?.newCall(requestBuilder.build())
                    ?.enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            try {
                                requireActivity().runOnUiThread {
                                    progress1?.visibility = View.GONE
                                }
                            } catch (ignored:Exception) {
                            }
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            response.use { resp ->
                                if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                                resp.body?.let { body ->
                                    val content = body.string()
                                    try {
                                        requireActivity().runOnUiThread {
                                            processChat(content)
                                        }
                                    } catch (ignored: Exception) {
                                    }
                                }
                            }
                        }
                    })
            }
        } catch (ignored: Exception) {
        }
    }

    private fun processChat(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                for (i in 0 until msg.length()) {
                    val userdata1 = msg.getJSONObject(i)
                    val chat = Chat(
                        0,
                        userdata1.optString(Constants.MESSAGE),
                        userdata1.optString(Constants.DATE),
                        "",
                        userdata1.optInt(Constants.READIED),
                        userdata1.optString(Constants.PHOTO_AGENT),
                        userdata1.optString(Constants.NAME_AGENT),
                        userdata1.optString(Constants.ID),
                        userdata1.optString(Constants.USER_ID),
                        userdata1.optInt(Constants.LAST)
                    )
                    val exists = Functions.getItem(userdata1.optString(Constants.ID), chatList)
                    if (exists) {
                        chatList[userdata1.optString(Constants.ID).toInt()] = chat
                        chatAdapter?.notifyItemChanged(userdata1.optString(Constants.ID).toInt())
                    } else {
                        chatList.add(chat)
                        chatAdapter?.notifyItemInserted(i)
                    }
                }
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
        chatRecycler = null
        chatAdapter = null
        timer.cancel()
        bottomNavigationView = null
        okHttpClient = null
    }
}
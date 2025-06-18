package ru.crmkurgan.main.ui.chat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.rxbinding.widget.RxTextView
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.ChatPersonAdapter
import ru.crmkurgan.main.items.Chat
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class ChatPersonFragment : Fragment() {
    private val chatList = ArrayList<Chat>()
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var chatPersonAdapter: ChatPersonAdapter? = null
    private var chatPersonRecycler: RecyclerView? = null
    private var cvProfileImage: ImageView? = null
    private var textTitle: TextView? = null
    private var textSubTitle: TextView? = null
    private var etMessage: EditText? = null
    private var timer = Timer()
    private var bottomNavigationView: BottomNavigationView? = null
    private var first = true
    private var timer1 = Timer()
    private var btnSendMessage: ImageView? = null
    private var okHttpClient: OkHttpClient? = null


    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat_person, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        progress = view.findViewById(R.id.progress)
        noConnection = view.findViewById(R.id.noConnection)
        cvProfileImage = view.findViewById(R.id.cvProfileImage)
        textTitle = view.findViewById(R.id.text_title)
        textSubTitle = view.findViewById(R.id.text_subtitle)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        val bottomNavigationView1 =
            requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1?.visibility = View.GONE
        bottomNavigationView = bottomNavigationView1
        val etMessage1 = view.findViewById<EditText>(R.id.etMessage)
        val back = view.findViewById<ImageView>(R.id.back)
        back?.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val chatPersonRecycler1 = view.findViewById<RecyclerView>(R.id.recycler_view_chat)
        val chatPersonAdapter1 = ChatPersonAdapter(chatList, requireContext())
        chatPersonRecycler1.adapter = chatPersonAdapter1
        chatPersonRecycler1.layoutManager = LinearLayoutManager(requireContext())
        (chatPersonRecycler1?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        chatPersonRecycler = chatPersonRecycler1
        chatPersonAdapter = chatPersonAdapter1
        okHttpClient = NetworkUtils.httpClient()
        val bundle1 = arguments
        bundle1?.let {
            val agent = it.getString(Constants.AGENT)
            val repeat = view.findViewById<TextView>(R.id.repeat)
            repeat.setOnClickListener {
                getChat(agent)
            }
            timer.schedule(object : TimerTask() {
                override fun run() {
                    requireActivity().runOnUiThread {
                        getChat(agent)
                    }
                }
            }, 0, 1000)
            timer1.schedule(object : TimerTask() {
                override fun run() {
                    requireActivity().runOnUiThread {
                        getTyping(agent)
                    }
                }
            }, 0, 1000)
            etMessage1.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    postTyping(count != 0, agent)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            RxTextView.textChanges(etMessage1)
                .debounce(1, TimeUnit.SECONDS)
                .subscribe {
                    if (etMessage1.hasFocus()) {
                        postTyping(false, agent)
                    }
                }
            btnSendMessage?.setOnClickListener {
                if (etMessage1.text.isNotEmpty()) {
                    postMessage(agent)
                    first = true
                }
            }
        }

        etMessage = etMessage1
        return view
    }

    private fun postMessage(agent: String?) {
        val sharedPref =
            requireActivity().getSharedPreferences(
                Constants.PREFERENCES,
                Context.MODE_PRIVATE
            )
        if (NetworkUtils.isConnected(requireContext())) {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            val uid = sharedPref.getInt(Constants.UID, 0)
            agent?.let { formBodyBuilder.add(Constants.AGENT, it) }
            formBodyBuilder.add(Constants.UID, uid.toString())
            formBodyBuilder.add(Constants.MESSAGE, etMessage?.text.toString())
            requestBuilder.url(Constants.SEND_MESSAGE)
            requestBuilder.post(formBodyBuilder.build())
            okHttpClient?.newCall(requestBuilder.build())
                ?.enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                    }
                })
            etMessage?.text?.clear()
        }
    }

    private fun postTyping(b: Boolean, agent: String?) {
        try {
            val typing = if (b) {
                "1"
            } else {
                "0"
            }
            if (NetworkUtils.isConnected(requireContext())) {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()
                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val uid = sharedPref.getInt(Constants.UID, 0)
                agent?.let { formBodyBuilder.add(Constants.UID, it) }
                formBodyBuilder.add(Constants.AGENT, uid.toString())
                formBodyBuilder.add(Constants.TYPING_OR_NOT, typing)
                requestBuilder.url(Constants.CHANGE_TYPING)
                requestBuilder.post(formBodyBuilder.build())
                okHttpClient?.newCall(requestBuilder.build())
                    ?.enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                        }
                    })

            }
        } catch (ignored: Exception) {
        }
    }

    private fun getTyping(agent: String?) {
        try {
            if (NetworkUtils.isConnected(requireContext())) {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()

                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val uid = sharedPref.getInt(Constants.UID, 0)
                agent?.let { formBodyBuilder.add(Constants.AGENT, it) }
                formBodyBuilder.add(Constants.UID, uid.toString())
                val zone = sharedPref.getString(Constants.ZONE, "GMT")
                formBodyBuilder.add(Constants.ZONE, zone.toString())
                requestBuilder.url(Constants.TYPING)
                requestBuilder.post(formBodyBuilder.build())
                okHttpClient?.newCall(requestBuilder.build())
                    ?.enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            response.use { resp ->
                                if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                                resp.body?.let { body ->
                                    val content = body.string()
                                    try {
                                        requireActivity().runOnUiThread {
                                            processTyping(content)
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

    private fun processTyping(content: String) {
        val textSubTitle1 = textSubTitle
        if (content == "1") {
            textSubTitle1?.text = resources.getString(R.string.typing)
        } else {
            textSubTitle1?.text = content
        }
    }

    private fun getChat(agent: String?) {
        try {
            val progress1 = progress
            if (!NetworkUtils.isConnected(requireContext())) {
                progress1?.visibility = View.GONE
            } else {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()
                agent?.let { formBodyBuilder.add(Constants.AGENT, it) }
                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val uid = sharedPref.getInt(Constants.UID, 0)
                formBodyBuilder.add(Constants.UID, uid.toString())
                val zone = sharedPref.getString(Constants.ZONE, "GMT")
                formBodyBuilder.add(Constants.ZONE, zone.toString())
                requestBuilder.url(Constants.CHAT_PERSON)
                requestBuilder.post(formBodyBuilder.build())
                okHttpClient?.newCall(requestBuilder.build())
                    ?.enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            try {
                                requireActivity().runOnUiThread {
                                    progress1?.visibility = View.GONE
                                }
                            } catch (ignored: Exception) {
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
                        userdata1.optInt(Constants.TYPE),
                        userdata1.optString(Constants.MESSAGE),
                        userdata1.optString(Constants.DATE),
                        userdata1.optString(Constants.TIME),
                        userdata1.optInt(Constants.READIED),
                        "",
                        "",
                        userdata1.optString(Constants.ID),
                        "",
                        0
                    )
                    val exists = Functions.getItem(userdata1.optString(Constants.ID), chatList)
                    if (exists) {
                        chatList[userdata1.optString(Constants.ID).toInt()] = chat
                        chatPersonAdapter?.notifyItemChanged(
                            userdata1.optString(Constants.ID).toInt()
                        )
                    } else {
                        chatList.add(chat)
                        chatPersonAdapter?.notifyItemInserted(i)
                    }
                }
                if (first) {
                    chatPersonRecycler?.smoothScrollToPosition(msg.length() - 1)
                    first = false
                }
                val agent = jsonObject.getJSONArray(Constants.AGENT)
                val userdata2 = agent.getJSONObject(0)
                cvProfileImage?.let {
                    Glide.with(requireContext())
                        .load(userdata2.optString(Constants.PHOTO_AGENT))
                        .error(R.drawable.ic_profile)
                        .placeholder(R.drawable.ic_profile)
                        .centerCrop()
                        .into(it)
                }
                textTitle?.text = userdata2.optString(Constants.NAME_AGENT_STROKE)
            }
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        try {
            if (NetworkUtils.isConnected(requireContext())) {
                val requestBuilder = Request.Builder()
                val formBodyBuilder = FormBody.Builder()

                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                val uid = sharedPref.getInt(Constants.UID, 0)
                formBodyBuilder.add(Constants.UID, uid.toString())
                requestBuilder.url(Constants.UPDATE_ONLINE)
                requestBuilder.post(formBodyBuilder.build())
                okHttpClient?.newCall(requestBuilder.build())
                    ?.enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                        }
                    })

            }
        } catch (ignored: Exception) {
        }
        super.onDestroy()
        progress = null
        noConnection = null
        chatPersonAdapter = null
        chatPersonRecycler = null
        cvProfileImage = null
        textTitle = null
        textSubTitle = null
        timer.cancel()
        etMessage = null
        bottomNavigationView = null
        timer1.cancel()
        btnSendMessage = null
        okHttpClient = null
    }
}
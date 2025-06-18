package ru.crmkurgan.main.ui.agent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.AgentListingAdapter
import ru.crmkurgan.main.items.Property
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException


class AgentFragment : Fragment() {
    private lateinit var contentView: View
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var avatar: ImageView? = null
    private var name: TextView? = null
    private var phone: TextView? = null
    private var agentListingAdapter: AgentListingAdapter? = null
    private var propertyRecycler: RecyclerView? = null
    private var propertyList = java.util.ArrayList<Property>()
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.fragment_agent_ad_listing, container, false)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
            activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val bundle1 = arguments
            bundle1?.let {
                val agent = it.getString(Constants.AGENT)
                getAgent(agent)
                val repeat = view.findViewById<TextView>(R.id.repeat)
                repeat.setOnClickListener {
                    getAgent(agent)
                }
            }
            avatar = view.findViewById(R.id.avatar)
            name = view.findViewById(R.id.name)
            phone = view.findViewById(R.id.phone)
            progress = view.findViewById(R.id.progress)
            noConnection = view.findViewById(R.id.noConnection)
            val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1?.visibility = View.GONE
            bottomNavigationView = bottomNavigationView1
            val propertyRecycler1 =
                view.findViewById<RecyclerView>(R.id.recycler_view_listing)
            val agentListingAdapter1 = AgentListingAdapter(propertyList, requireContext())
            propertyRecycler1.adapter = agentListingAdapter1
            propertyRecycler1.layoutManager = GridLayoutManager(requireContext(), 1)
            propertyRecycler = propertyRecycler1
            agentListingAdapter = agentListingAdapter1

            contentView = view
        }
        return contentView
    }

    private fun getAgent(agent: String?) {
        propertyList.clear()
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            agent?.let { formBodyBuilder.add(Constants.AGENT, it) }
            requestBuilder.url(Constants.AGENT_DETAIL)
            requestBuilder.post(formBodyBuilder.build())
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
                                    processAgentDetail(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processAgentDetail(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                val userdata = msg.getJSONObject(0)
                val activity1 = activity
                activity1?.title = userdata.optString(Constants.NAME_AGENT_STROKE)
                avatar?.let {
                    Glide.with(requireContext())
                        .load(userdata.optString(Constants.PHOTO_AGENT))
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(it)
                }
                name?.text = userdata.optString(Constants.NAME_AGENT_STROKE)
                phone?.let {
                    it.text = userdata.optString(Constants.TEL)
                    it.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:" + userdata.optString(Constants.TEL))
                        startActivity(callIntent)
                    }
                }
                val listingArr = msg.getJSONObject(0).getJSONArray(Constants.AGENT_LISTING)
                for (i in 0 until listingArr.length()) {
                    val userdata1 = listingArr.getJSONObject(i)
                    val listing = Property(
                        userdata1.optString(Constants.ID),
                        userdata1.optString(Constants.IMAGE),
                        userdata1.optString(Constants.TYPE),
                        userdata1.optString(Constants.PRICE),
                        userdata1.optString(Constants.ADDRESS),
                        userdata1.optString(Constants.TABLE)
                    )
                    propertyList.add(listing)
                    agentListingAdapter?.notifyItemInserted(i)
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
        avatar = null
        name = null
        phone = null
        agentListingAdapter = null
        propertyRecycler = null
        bottomNavigationView = null
    }
}
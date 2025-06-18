package ru.crmkurgan.main.ui.review

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.ReviewAdapter
import ru.crmkurgan.main.items.Review
import ru.crmkurgan.main.ui.property.RatingDialogFragment
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException

class ReviewFragment : Fragment() {
    private var reviewList = java.util.ArrayList<Review>()
    private var reviewAdapter: ReviewAdapter? = null
    private var reviewRecycler: RecyclerView? = null
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var agentRate: TextView? = null
    private var rating: RatingBar? = null
    private var agentTotalRates: TextView? = null
    private var photoAgent: ImageView? = null
    private var name: TextView? = null
    private var viewRatingDialog: MaterialButton? = null
    private var agent: String = ""
    private var uid: Int = 0
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity1.title = resources.getString(R.string.reviews)
        agentRate = view.findViewById(R.id.agentRate)
        rating = view.findViewById(R.id.rating_package)
        agentTotalRates = view.findViewById(R.id.agentTotalRates)
        progress = view.findViewById(R.id.progress)
        noConnection = view.findViewById(R.id.noConnection)
        photoAgent = view.findViewById(R.id.photoAgent)
        name = view.findViewById(R.id.name)
        val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1?.visibility = View.GONE
        bottomNavigationView = bottomNavigationView1
        viewRatingDialog = view.findViewById(R.id.viewRatingDialog)
        val reviewRecycler1 = view.findViewById<RecyclerView>(R.id.recycler_view_reviews)
        val reviewAdapter1 = ReviewAdapter(reviewList, requireContext())
        reviewRecycler1.adapter = reviewAdapter1
        reviewRecycler1.layoutManager = LinearLayoutManager(requireContext())
        //reviewRecycler1.setHasFixedSize(true)
        reviewRecycler = reviewRecycler1
        reviewAdapter = reviewAdapter1
        val bundle1 = arguments
        bundle1?.let {
            val agent1 = it.getString(Constants.AGENT)
            agent1?.let { it3 ->
                getReviews(agent1)
                agent = it3
                val sharedPref =
                    requireActivity().getSharedPreferences(
                        Constants.PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                uid = sharedPref.getInt(Constants.UID, 0)
            }
            val repeat = view.findViewById<TextView>(R.id.repeat)
            repeat.setOnClickListener {
                getReviews(agent)
            }
        }
        return view
    }

    private fun getReviews(agent: String?) {
        reviewList.clear()
        val progress1 = progress
        val viewRatingDialog1 = viewRatingDialog
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
            viewRatingDialog1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            requestBuilder.url(Constants.GET_REVIEWS)
            agent?.let {
                formBodyBuilder.add(Constants.AGENT, it)
                requestBuilder.post(formBodyBuilder.build())
                NetworkUtils.httpClient().newCall(requestBuilder.build())
                    .enqueue(object :
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            requireActivity().runOnUiThread {
                                progress1?.visibility = View.GONE
                                viewRatingDialog1?.visibility = View.GONE
                            }
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            response.use { resp ->
                                if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                                resp.body?.let { body ->
                                    val content = body.string()
                                    requireActivity().runOnUiThread {
                                        processReviews(content)
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun processReviews(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val total = jsonObject.getJSONArray(Constants.TOTAL)
                val userdata = total.getJSONObject(0)
                val agentRate1 = userdata.optString(Constants.AGENT_RATE)
                agentRate?.text = agentRate1
                val rating1 = rating
                try {
                    val agentRate2 = agentRate1.toFloat()
                    rating1?.rating = agentRate2
                } catch (e: Exception) {
                    rating1?.rating = 0.0F
                }
                photoAgent?.let {
                    Glide.with(requireContext())
                        .load(userdata.optString(Constants.PHOTO_AGENT))
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(it)
                }
                val name1 = name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    name1?.text = Html.fromHtml(
                        userdata.optString(Constants.NAME_AGENT),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    name1?.text = Html.fromHtml(
                        userdata.optString(Constants.NAME_AGENT)
                    )
                }
                val agentTotalRates1 = userdata.optString(Constants.AGENT_TOTAL_RATES)
                agentTotalRates?.text = agentTotalRates1
                val reviews = jsonObject.getJSONArray(Constants.MSG)
                for (i in 0 until reviews.length()) {
                    val userdata1 = reviews.getJSONObject(i)
                    val review = Review(
                        userdata1.optString(Constants.IMAGE),
                        userdata1.optString(Constants.REVIEW),
                        userdata1.optString(Constants.DATE),
                        userdata1.optString(Constants.NAME),
                        userdata1.optString(Constants.RATING_VALUE)
                    )
                    reviewList.add(review)
                    reviewAdapter?.notifyItemInserted(i)
                }
            }
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
            if (uid != 0) {
                viewRatingDialog?.let { it2 ->
                    it2.visibility = View.VISIBLE
                    it2.setOnClickListener {
                        RatingDialogFragment.agent = agent
                        RatingDialogFragment().show(
                            parentFragmentManager,
                            RatingDialogFragment.TAG
                        )
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reviewAdapter = null
        reviewRecycler = null
        progress = null
        noConnection = null
        agentRate = null
        rating = null
        agentTotalRates = null
        photoAgent = null
        name = null
        viewRatingDialog = null
        bottomNavigationView = null
    }
}
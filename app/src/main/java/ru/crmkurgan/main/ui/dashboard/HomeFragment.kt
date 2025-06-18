package ru.crmkurgan.main.ui.dashboard

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
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.ImageSliderAdapter
import ru.crmkurgan.main.items.Image
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException
import java.util.*


class HomeFragment : Fragment() {
    private val imageList = ArrayList<Image>()
    private var imageSliderAdapter: ImageSliderAdapter? = null
    private var imageSlider: ViewPager2? = null
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private lateinit var contentView: View
    private var bottomNavigationView: BottomNavigationView? = null
    private var filter: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.fragment_home, container, false)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
            activity1.title = resources.getString(R.string.app_name)
            val imageSlider1 = view.findViewById<ViewPager2>(R.id.recycler_view_header_banner)
            val imageSliderAdapter1 = ImageSliderAdapter(imageList, requireContext())
            imageSliderAdapter = imageSliderAdapter1
            imageSlider1?.adapter = imageSliderAdapter1
            imageSlider = imageSlider1
            progress = view.findViewById(R.id.progress)
            noConnection = view.findViewById(R.id.noConnection)
            val bottomNavigationView1 =
                requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1.visibility = View.VISIBLE
            bottomNavigationView = bottomNavigationView1
            getImages()
            val repeat = view.findViewById<TextView>(R.id.repeat)
            repeat.setOnClickListener {
                getImages()
            }
            val filter1 = view.findViewById<ImageView>(R.id.filter)
            filter1.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_search_nav_graph)
            }
            filter = filter1
            contentView = view
        }
        return contentView
    }

    private fun getImages() {
        imageList.clear()
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            requestBuilder.url(Constants.IMAGES)
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
                                    processImages(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processImages(content: String) {
        try {
            val imageSliderAdapter1 = imageSliderAdapter
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                for (i in 0 until msg.length()) {
                    val userdata = msg.getJSONObject(i)
                    val image = Image(
                        userdata.optString(Constants.IMAGE),
                        userdata.optString(Constants.TABLE),
                        userdata.optString(Constants.ID),
                        2,
                        ""
                    )
                    imageList.add(image)
                    imageSliderAdapter1?.notifyItemInserted(i)
                }
            } else {
                val image = Image(Constants.DEFAULT, "", "0", 2, "")
                imageList.add(image)
                imageSliderAdapter1?.notifyItemInserted(0)
            }
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageSliderAdapter = null
        imageSlider = null
        progress = null
        noConnection = null
        bottomNavigationView = null
        filter = null
    }
}
package ru.crmkurgan.main.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import multiSelectSpinner.KeyPairBoolData
import multiSelectSpinner.MultiSpinnerSearch
import multiSelectSpinner.SingleSpinnerSearch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException


class SearchFragment : Fragment() {
    private lateinit var contentView: View
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private var type: MultiSpinnerSearch? = null
    private var selectedTypes: List<KeyPairBoolData>? = null
    private var category: SingleSpinnerSearch? = null
    private var categoryItem: KeyPairBoolData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.fragment_search, container, false)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
            activity1.title = resources.getString(R.string.search_menu_title)
            progress = view.findViewById(R.id.progress)
            noConnection = view.findViewById(R.id.noConnection)
            val bottomNavigationView1 =
                requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1.visibility = View.VISIBLE
            bottomNavigationView = bottomNavigationView1
            val resources1 = resources
            val type1 = view.findViewById<MultiSpinnerSearch>(R.id.type)
            type1?.isSearchEnabled = false
            type1?.setClearText(resources1.getString(R.string.clear))
            type = type1
            val category1 = view.findViewById<SingleSpinnerSearch>(R.id.category)
            category1?.isSearchEnabled = false
            val listArrayCategories: MutableList<KeyPairBoolData> = ArrayList()
            val h = KeyPairBoolData()
            h.id = 0
            h.name = resources1.getString(R.string.secondary)
            h.isSelected = true
            listArrayCategories.add(h)

            val h1 = KeyPairBoolData()
            h1.id = 1
            h1.name = resources1.getString(R.string.houses)
            listArrayCategories.add(h1)

            val h2 = KeyPairBoolData()
            h2.id = 2
            h2.name = resources1.getString(R.string.commerce)
            listArrayCategories.add(h2)

            category1.setItems(
                listArrayCategories
            ) { selectedItem -> categoryItem = selectedItem }
            category = category1
            getFilter()
            val repeat = view.findViewById<TextView>(R.id.repeat)
            repeat.setOnClickListener {
                getFilter()
            }
            contentView = view
        }
        return contentView
    }

    private fun getFilter() {
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            requestBuilder.url(Constants.FILTER)
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
                                    processFilter(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processFilter(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val types = jsonObject.getJSONArray(Constants.TYPE)
                val listArrayTypes: MutableList<KeyPairBoolData> = ArrayList()
                for (i in 0 until types.length()) {
                    val userdata = types.getJSONObject(i)
                    val h = KeyPairBoolData()
                    h.id = userdata.optLong("id")
                    h.name = userdata.optString("type")
                    listArrayTypes.add(h)
                }
                type?.setItems(
                    listArrayTypes
                ) { selectedItems -> selectedTypes = selectedItems }
            }
        } catch (ignored: Exception) {
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progress = null
        noConnection = null
        bottomNavigationView = null
        type = null
        selectedTypes = null
        category = null
    }
}
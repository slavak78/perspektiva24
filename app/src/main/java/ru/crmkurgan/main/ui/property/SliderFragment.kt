package ru.crmkurgan.main.ui.property

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.CenteredImageSliderAdapter
import ru.crmkurgan.main.items.Image
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import ru.crmkurgan.main.utils.ObjectsDBSQLiteHelper
import ru.crmkurgan.main.views.TouchImageView
import java.io.IOException


class SliderFragment : Fragment() {
    private val imageList = ArrayList<Image>()
    private var imageSliderAdapter: CenteredImageSliderAdapter? = null
    private var imageSlider: ViewPager? = null
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var toolbar: Toolbar? = null
    private var idProperty: String = ""
    private var table: String = ""
    private var url: String = ""
    private lateinit var contentView: View
    private var favorite: MenuItem? = null
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        val flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        window.decorView.systemUiVisibility = flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.slider, container, false)
            val toolbar1 = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar1.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar1)
            activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val lp: MarginLayoutParams =
                toolbar1.layoutParams as MarginLayoutParams
            lp.topMargin = Functions.getStatusBarHeight(resources, window)
            toolbar = toolbar1
            setHasOptionsMenu(true)
            progress = view.findViewById(R.id.progress)
            noConnection = view.findViewById(R.id.noConnection)
            val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1?.visibility = View.GONE
            bottomNavigationView = bottomNavigationView1
            val imageSlider1 = view.findViewById<ViewPager>(R.id.recycler_slider)
            val imageSliderAdapter1 = CenteredImageSliderAdapter(imageList)
            imageSliderAdapter = imageSliderAdapter1
            imageSlider1.adapter = imageSliderAdapter1
            imageSlider1.setPageTransformer(false) { page: View, position: Float ->
                if ((position == -1f || position == 1f)) {
                    var current = imageSlider1.currentItem
                    if (position == -1f) {
                        current--
                    } else {
                        current++
                    }
                    if (imageList[current].type == 2) {
                        val touch = page.findViewById<TouchImageView>(R.id.photo)
                        touch?.resetZoom()
                    }
                }
            }

            imageSlider = imageSlider1
            val bundle1 = arguments
            bundle1?.let {
                val idObject1 = it.getString(Constants.ID)
                val table11 = it.getString(Constants.TABLE)
                getSlider(idObject1, table11)
                val repeat = view.findViewById<TextView>(R.id.repeat)
                repeat.setOnClickListener {
                    getSlider(idObject1, table11)
                }
                idObject1?.let { it2 ->
                    table11?.let { it3 ->
                        idProperty = it2
                        table = it3
                    }
                }
            }
            contentView = view
        }
        return contentView
    }

    private fun getSlider(id: String?, table: String?) {
        imageList.clear()
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            id?.let { formBodyBuilder.add(Constants.ID, it) }
            table?.let { formBodyBuilder.add(Constants.TABLE, it) }
            requestBuilder.url(Constants.SLIDER)
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
                                    processSlider(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    @Suppress("DEPRECATION")
    private fun processSlider(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                val userdata = msg.getJSONObject(0)
                val subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(
                        userdata.optString(Constants.NAME),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    ).toString()
                } else {
                    Html.fromHtml(
                        userdata.optString(Constants.NAME)
                    ).toString()
                }
                url = userdata.optString(Constants.URL)
                val activity1 = activity
                activity1?.title = userdata.optString(Constants.PRICE)
                toolbar?.subtitle = subtitle
                val images = msg.getJSONObject(0).getJSONArray(Constants.GALLERY_IMAGE)
                for (i in 0 until images.length()) {
                    val userdata1 = images.getJSONObject(i)
                    val image = Image(
                        userdata1.optString(Constants.GALLERY),
                        "",
                        "",
                        userdata1.optInt(Constants.TYPE),
                        userdata1.optString(Constants.THUMBNAIL)
                    )
                    imageList.add(image)
                }
                imageSliderAdapter?.notifyDataSetChanged()
            }
        } catch (ignored: Exception) {
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val db = ObjectsDBSQLiteHelper(requireContext())
        val selectionArgs = arrayOf(idProperty, table)
        val db1 = db.readableDatabase
        val cursor = db1.query(
            Constants.DATABASE_NAME,
            Constants.PROJECTION,
            Constants.SELECTED_QUERY,
            selectionArgs,
            null,
            null,
            null
        )
        val favorite1 = menu.getItem(2)
        favorite1.icon = resources.getDrawable(
            if (cursor.count > 0) R.drawable.ic_favorite_red else R.drawable.ic_like,
            null
        )
        cursor.close()
        db1.close()
        favorite = favorite1
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                share()
                return true
            }
            R.id.action_like -> {
                favoriteId(idProperty, table, favorite)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun share() {
        val targets: MutableList<Intent> = ArrayList()
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = Constants.MIME_TYPE_PLAIN
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mess))
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        val candidates = requireActivity().packageManager.queryIntentActivities(shareIntent, 0)
        val namePackage = requireActivity().packageName
        for (i in 0 until candidates.size) {
            val packageName = candidates[i].activityInfo.packageName
            if (packageName != namePackage) {
                val target = Intent(Intent.ACTION_SEND)
                target.type = Constants.MIME_TYPE_PLAIN
                target.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mess))
                target.putExtra(Intent.EXTRA_TEXT, url)
                target.setPackage(packageName)
                targets.add(target)
            }
        }
        val chooserIntent = Intent.createChooser(Intent(), getString(R.string.share))
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            targets.toTypedArray<Parcelable>()
        )
        try {
            startActivity(chooserIntent)
        } catch (e: Throwable) {
        }
    }


    private fun favoriteId(id: String, table: String, favorite: MenuItem?) {
        val db = ObjectsDBSQLiteHelper(requireContext())
        val selectionArgs = arrayOf(id, table)
        val resources1 = resources
        val db1 = db.readableDatabase
        val cursor = db1.query(
            Constants.DATABASE_NAME,
            Constants.PROJECTION,
            Constants.SELECTED_QUERY,
            selectionArgs,
            null,
            null,
            null
        )
        if (cursor.count > 0) {
            val deleteArgs = arrayOf(id)
            val success = db1.delete(Constants.DATABASE_NAME, Constants.DELETE_QUERY, deleteArgs)
            if (success != -1) {
                favorite?.icon = resources1.getDrawable(R.drawable.ic_like, null)
            }
        } else {
            val values = ContentValues()
            values.put(Constants.INTERNAL_ID, id)
            values.put(Constants.INTERNAL_TABLE, table)
            val success = db1.insert(Constants.DATABASE_NAME, null, values)
            if (success.toInt() != -1) {
                favorite?.icon = resources1.getDrawable(R.drawable.ic_favorite_red, null)
            }
        }
        db1.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageSliderAdapter = null
        imageSlider = null
        progress = null
        noConnection = null
        toolbar = null
        favorite = null
        bottomNavigationView = null
    }
}
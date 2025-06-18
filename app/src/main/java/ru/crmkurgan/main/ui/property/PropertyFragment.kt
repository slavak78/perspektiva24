package ru.crmkurgan.main.ui.property

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.SimilarAdapter
import ru.crmkurgan.main.adapters.ReviewAdapter
import ru.crmkurgan.main.adapters.TopImageSliderAdapter
import ru.crmkurgan.main.items.Image
import ru.crmkurgan.main.items.Property
import ru.crmkurgan.main.items.Review
import ru.crmkurgan.main.utils.NetworkUtils
import ru.crmkurgan.main.utils.ObjectsDBSQLiteHelper
import java.io.IOException

class PropertyFragment : Fragment() {
    var table = ""
    private var idProperty = ""
    private var textTitle: TextView? = null
    private var textSubTitle: TextView? = null
    private val imageList = ArrayList<Image>()
    private var imageSliderAdapter: TopImageSliderAdapter? = null
    private var imageSlider: ViewPager2? = null
    private var featured: CardView? = null
    private var price: TextView? = null
    private var textDesc: TextView? = null
    private var secondary: LinearLayout? = null
    private var houses: LinearLayout? = null
    private var commercial: LinearLayout? = null
    private var viewMap: LinearLayout? = null
    private var roomSize: TextView? = null
    private var totalArea: TextView? = null
    private var typeOfLayout: TextView? = null
    private var repair: TextView? = null
    private var bathroom: TextView? = null
    private var balcony: TextView? = null
    private var floors: TextView? = null
    private var yearOfConstruction: TextView? = null
    private var wallMaterials: TextView? = null
    private var areaHouse: TextView? = null
    private var numberOfRooms: TextView? = null
    private var numberOfFloors: TextView? = null
    private var yearOfConstruction1: TextView? = null
    private var wallMaterials1: TextView? = null
    private var powerSupply: TextView? = null
    private var heating: TextView? = null
    private var gasSupply: TextView? = null
    private var sewage: TextView? = null
    private var plotArea: TextView? = null
    private var square: TextView? = null
    private var floors1: TextView? = null
    private var mView: ImageView? = null
    private var progress: RelativeLayout? = null
    private var noConnection: RelativeLayout? = null
    private var viewRatingDialog: TextView? = null
    private var agentRate: TextView? = null
    private var rating: RatingBar? = null
    private var agentTotalRates: TextView? = null
    private var reviewList = java.util.ArrayList<Review>()
    private var reviewAdapter: ReviewAdapter? = null
    private var reviewRecycler: RecyclerView? = null
    private var viewMore: MaterialButton? = null
    private var photoAgent: ImageView? = null
    private var name: TextView? = null
    var agent: String = ""
    private var propertyList = java.util.ArrayList<Property>()
    private var similarAdapter: SimilarAdapter? = null
    private var propertyRecycler: RecyclerView? = null
    private var related: TextView? = null
    private var avatar: ImageView? = null
    private var nameAgent: TextView? = null
    private lateinit var contentView: View
    private var url: String = ""
    private var layoutButton: RelativeLayout? = null
    private var buttonCall: MaterialButton? = null
    private var buttonChat: MaterialButton? = null
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.variantSecondary)
        window.decorView.systemUiVisibility = 0
        if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
        val favorite: ImageView
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.fragment_property_detail, container, false)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
            activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity1.title = ""
            setHasOptionsMenu(true)
            textTitle = view.findViewById(R.id.text_title)
            textSubTitle = view.findViewById(R.id.text_subtitle)
            featured = view.findViewById(R.id.featured)
            price = view.findViewById(R.id.price)
            textDesc = view.findViewById(R.id.text_desc)
            secondary = view.findViewById(R.id.secondary)
            houses = view.findViewById(R.id.houses)
            commercial = view.findViewById(R.id.commercial)
            viewMap = view.findViewById(R.id.viewMap)
            roomSize = view.findViewById(R.id.roomSize)
            totalArea = view.findViewById(R.id.totalArea)
            typeOfLayout = view.findViewById(R.id.typeOfLayout)
            repair = view.findViewById(R.id.repair)
            bathroom = view.findViewById(R.id.bathroom)
            balcony = view.findViewById(R.id.balcony)
            floors = view.findViewById(R.id.floors)
            yearOfConstruction = view.findViewById(R.id.yearOfConstruction)
            wallMaterials = view.findViewById(R.id.wallMaterials)
            areaHouse = view.findViewById(R.id.areaHouse)
            numberOfRooms = view.findViewById(R.id.numberOfRooms)
            numberOfFloors = view.findViewById(R.id.numberOfFloors)
            yearOfConstruction1 = view.findViewById(R.id.yearOfConstruction1)
            wallMaterials1 = view.findViewById(R.id.wallMaterials1)
            powerSupply = view.findViewById(R.id.powerSupply)
            heating = view.findViewById(R.id.heating)
            gasSupply = view.findViewById(R.id.gasSupply)
            sewage = view.findViewById(R.id.sewage)
            plotArea = view.findViewById(R.id.plotArea)
            square = view.findViewById(R.id.square)
            floors1 = view.findViewById(R.id.floors1)
            mView = view.findViewById(R.id.mapview)
            progress = view.findViewById(R.id.progress)
            noConnection = view.findViewById(R.id.noConnection)
            agentRate = view.findViewById(R.id.agentRate)
            rating = view.findViewById(R.id.rating_package)
            agentTotalRates = view.findViewById(R.id.agentTotalRates)
            photoAgent = view.findViewById(R.id.photoAgent)
            name = view.findViewById(R.id.name)
            related = view.findViewById(R.id.related)
            avatar = view.findViewById(R.id.avatar)
            nameAgent = view.findViewById(R.id.nameAgent)
            buttonCall = view.findViewById(R.id.button_call)
            buttonChat = view.findViewById(R.id.button_chat)
            val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1?.visibility = View.GONE
            bottomNavigationView = bottomNavigationView1
            val imageSlider1 = view.findViewById<ViewPager2>(R.id.recycler_view_header_banner)
            val imageSliderAdapter1 = TopImageSliderAdapter(imageList, requireContext())
            imageSliderAdapter = imageSliderAdapter1
            imageSlider1.adapter = imageSliderAdapter1
            imageSlider = imageSlider1
            val reviewRecycler1 = view.findViewById<RecyclerView>(R.id.recycler_view_review)
            val reviewAdapter1 = ReviewAdapter(reviewList, requireContext())
            reviewRecycler1.adapter = reviewAdapter1
            reviewRecycler1.layoutManager = LinearLayoutManager(requireContext())
            reviewRecycler = reviewRecycler1
            reviewAdapter = reviewAdapter1
            val propertyRecycler1 =
                view.findViewById<RecyclerView>(R.id.recycler_view_related_property)
            val similarAdapter1 = SimilarAdapter(propertyList, requireContext())
            propertyRecycler1.adapter = similarAdapter1
            propertyRecycler1.layoutManager = LinearLayoutManager(requireContext())
            propertyRecycler = propertyRecycler1
            similarAdapter = similarAdapter1
            viewMore = view.findViewById(R.id.viewMore)
            val bundle1 = arguments
            bundle1?.let {
                val idObject1 = it.getString(Constants.ID)
                val table11 = it.getString(Constants.TABLE)
                getPropertyDetail(idObject1, table11)
                table = table11.toString()
                idProperty = idObject1.toString()
                val repeat = view.findViewById<TextView>(R.id.repeat)
                repeat.setOnClickListener {
                    getPropertyDetail(idObject1, table11)
                }
                viewRatingDialog = view.findViewById(R.id.viewRatingDialog)
                layoutButton = view.findViewById(R.id.layout_button)
            }
            favorite = view.findViewById(R.id.favorite)

            val share = view.findViewById<ImageView>(R.id.share)
            share.setOnClickListener {
                share()
            }
            contentView = view
        } else {
            favorite = contentView.findViewById(R.id.favorite)
        }
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
        favorite.setImageDrawable(
            resources.getDrawable(
                if (cursor.count > 0) R.drawable.ic_favorite_red else R.drawable.ic_favorite,
                null
            )
        )
        favorite.setOnClickListener {
            favoriteId(idProperty, table, favorite)
        }
        cursor.close()
        db1.close()
        return contentView
    }

    private fun getPropertyDetail(id: String?, table: String?) {
        imageList.clear()
        reviewList.clear()
        propertyList.clear()
        val progress1 = progress
        if (!NetworkUtils.isConnected(requireContext())) {
            progress1?.visibility = View.GONE
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            id?.let { formBodyBuilder.add(Constants.ID, it) }
            table?.let { formBodyBuilder.add(Constants.TABLE, it) }
            requestBuilder.url(Constants.PROPERTY_DETAIL)
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
                                    processPropertyDetail(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    @Suppress("DEPRECATION")
    private fun processPropertyDetail(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val msg = jsonObject.getJSONArray(Constants.MSG)
                val userdata = msg.getJSONObject(0)
                val textTitle1 = textTitle
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textTitle1?.text = Html.fromHtml(
                        userdata.optString(Constants.NAME),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    textTitle1?.text = Html.fromHtml(
                        userdata.optString(Constants.NAME)
                    )
                }
                textSubTitle?.text = userdata.optString(Constants.ADDRESS)
                val images = msg.getJSONObject(0).getJSONArray(Constants.GALLERY_IMAGE)
                for (i in 0 until images.length()) {
                    val userdata1 = images.getJSONObject(i)
                    val image = Image(
                        userdata1.optString(Constants.GALLERY),
                        userdata1.optString(Constants.TABLE),
                        userdata1.optString(Constants.ID),
                        userdata1.optInt(Constants.TYPE),
                        userdata1.optString(Constants.THUMBNAIL)
                    )
                    imageList.add(image)
                    imageSliderAdapter?.notifyItemInserted(i)
                }
                val featuredText = userdata.optString(Constants.FEATURED)
                if (featuredText != "1") {
                    featured?.visibility = View.INVISIBLE
                }
                price?.text = userdata.optString(Constants.PRICE)
                val textDesc1 = textDesc
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textDesc1?.text = Html.fromHtml(
                        userdata.optString(Constants.COMMENTS),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    textDesc1?.text = Html.fromHtml(
                        userdata.optString(Constants.COMMENTS)
                    )
                }
                url = userdata.optString(Constants.URL)
                when (table) {
                    "1" -> {
                        secondary?.visibility = View.VISIBLE
                        roomSize?.text = userdata.optString(Constants.ROOM_SIZE)
                        val totalArea1 = totalArea
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            totalArea1?.text = Html.fromHtml(
                                userdata.optString(Constants.TOTAL_AREA),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        } else {
                            totalArea1?.text = Html.fromHtml(
                                userdata.optString(Constants.TOTAL_AREA)
                            )
                        }
                        typeOfLayout?.text = userdata.optString(Constants.TYPE_OF_LAYOUT)
                        repair?.text = userdata.optString(Constants.REPAIR)
                        bathroom?.text = userdata.optString(Constants.BATHROOM)
                        balcony?.text = userdata.optString(Constants.BALCONY)
                        floors?.text = userdata.optString(Constants.FLOORS)
                        yearOfConstruction?.text =
                            userdata.optString(Constants.YEAR_OF_CONSTRUCTION)
                        wallMaterials?.text = userdata.optString(Constants.WALL_MATERIALS)
                    }
                    "2" -> {
                        houses?.visibility = View.VISIBLE
                        val areaHouse1 = areaHouse
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            areaHouse1?.text = Html.fromHtml(
                                userdata.optString(Constants.AREA_HOUSE),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        } else {
                            areaHouse1?.text = Html.fromHtml(
                                userdata.optString(Constants.AREA_HOUSE)
                            )
                        }
                        numberOfRooms?.text = userdata.optString(Constants.NUMBER_OF_ROOMS)
                        numberOfFloors?.text = userdata.optString(Constants.NUMBER_OF_FLOORS)
                        yearOfConstruction1?.text =
                            userdata.optString(Constants.YEAR_OF_CONSTRUCTION)
                        wallMaterials1?.text = userdata.optString(Constants.WALL_MATERIALS)
                        powerSupply?.text = userdata.optString(Constants.POWER_SUPPLY)
                        heating?.text = userdata.optString(Constants.HEATING)
                        gasSupply?.text = userdata.optString(Constants.GAS_SUPPLY)
                        sewage?.text = userdata.optString(Constants.SEWAGE)
                        plotArea?.text = userdata.optString(Constants.PLOT_AREA)
                    }
                    else -> {
                        commercial?.visibility = View.VISIBLE
                        val square1 = square
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            square1?.text = Html.fromHtml(
                                userdata.optString(Constants.SQUARE),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        } else {
                            square1?.text = Html.fromHtml(
                                userdata.optString(Constants.SQUARE)
                            )
                        }
                        floors1?.text = userdata.optString(Constants.FLOORS)
                    }
                }
                val lat = userdata.optString(Constants.LAT)
                val lng = userdata.optString(Constants.LNG)
                val url =
                    Constants.STATIC_MAP + lng + Constants.SIGN + lat + Constants.SIGN + Constants.ROUND
                mView?.let {
                    Glide.with(requireContext()).load(url).into(it)
                    it.setOnClickListener {
                        val bundle = bundleOf(
                            Pair(Constants.LAT, lat),
                            Pair(Constants.LNG, lng)
                        )
                        findNavController().navigate(
                            R.id.action_propertyFragment_to_mapFragment,
                            bundle
                        )
                    }
                    val agent1 = userdata.optString(Constants.AGENT)
                    val sharedPref =
                        requireActivity().getSharedPreferences(
                            Constants.PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                    buttonCall?.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:" + userdata.optString(Constants.PHONE))
                        startActivity(callIntent)
                    }
                    val buttonChat1 = buttonChat
                    buttonChat1?.setOnClickListener {
                        val bundle = bundleOf(
                            Pair(Constants.AGENT, agent1)
                        )
                        findNavController().navigate(
                            R.id.goto_chat,
                            bundle
                        )
                    }
                    val uid = sharedPref.getInt(Constants.UID, 0)
                    if (uid != 0) {
                        buttonChat1?.visibility = View.VISIBLE
                        viewRatingDialog?.let { it2 ->
                            it2.visibility = View.VISIBLE
                            it2.setOnClickListener {
                                RatingDialogFragment.agent = agent1
                                RatingDialogFragment().show(
                                    parentFragmentManager,
                                    RatingDialogFragment.TAG
                                )
                            }
                        }
                    }
                    layoutButton?.setOnClickListener {
                        val bundle = bundleOf(
                            Pair(Constants.AGENT, agent1)
                        )
                        findNavController().navigate(
                            R.id.goToAgentFragment,
                            bundle
                        )
                    }
                    agent = agent1
                    val agentRate1 = userdata.optString(Constants.AGENT_RATE)
                    agentRate?.text = agentRate1
                    val rating1 = rating
                    try {
                        val agentRate2 = agentRate1.toFloat()
                        rating1?.rating = agentRate2
                    } catch (e: Exception) {
                        rating1?.rating = 0.0F
                    }
                    val agentTotalRates1 = userdata.optString(Constants.AGENT_TOTAL_RATES)
                    agentTotalRates?.text = agentTotalRates1
                }
                val reviews = msg.getJSONObject(0).getJSONArray(Constants.REVIEWS)
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
                try {
                    val totalReviews = userdata.optString(Constants.TOTAL_REVIEWS).toInt()
                    if (totalReviews > 2) {
                        val viewMore1 = viewMore
                        viewMore1?.let {
                            it.visibility = View.VISIBLE
                            it.setOnClickListener {
                                val bundle = bundleOf(
                                    Pair(Constants.AGENT, agent)
                                )
                                findNavController().navigate(
                                    R.id.action_propertyFragment_to_reviewFragment,
                                    bundle
                                )
                            }
                        }
                    }
                    photoAgent?.let {
                        Glide.with(requireContext())
                            .load(userdata.optString(Constants.PHOTO_AGENT))
                            .error(R.drawable.ic_profile)
                            .placeholder(R.drawable.ic_profile)
                            .centerCrop()
                            .into(it)
                    }
                    avatar?.let {
                        Glide.with(requireContext())
                            .load(userdata.optString(Constants.PHOTO_AGENT))
                            .error(R.drawable.ic_profile)
                            .placeholder(R.drawable.ic_profile)
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
                    nameAgent?.text = userdata.optString(Constants.NAME_AGENT_STROKE)
                    val relatedArr = msg.getJSONObject(0).getJSONArray(Constants.RELATED)
                    if (relatedArr.length() > 0) {
                        for (i in 0 until relatedArr.length()) {
                            val userdata1 = relatedArr.getJSONObject(i)
                            val relate = Property(
                                userdata1.optString(Constants.ID),
                                userdata1.optString(Constants.IMAGE),
                                userdata1.optString(Constants.TYPE),
                                userdata1.optString(Constants.PRICE),
                                userdata1.optString(Constants.ADDRESS),
                                userdata1.optString(Constants.TABLE)
                            )
                            propertyList.add(relate)
                            similarAdapter?.notifyItemInserted(i)
                        }
                    } else {
                        related?.visibility = View.GONE
                    }
                } catch (ignored: Exception) {
                }
            }
            progress?.visibility = View.GONE
            noConnection?.visibility = View.GONE
        } catch (ignored: Exception) {
        }
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

    private fun favoriteId(id: String, table: String, favorite: ImageView?) {
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
                favorite?.setImageDrawable(resources1.getDrawable(R.drawable.ic_favorite, null))
            }
        } else {
            val values = ContentValues()
            values.put(Constants.INTERNAL_ID, id)
            values.put(Constants.INTERNAL_TABLE, table)
            val success = db1.insert(Constants.DATABASE_NAME, null, values)
            if (success.toInt() != -1) {
                favorite?.setImageDrawable(resources1.getDrawable(R.drawable.ic_favorite_red, null))
            }
        }
        db1.close()
    }


    override fun onDestroy() {
        super.onDestroy()
        textTitle = null
        textSubTitle = null
        imageSliderAdapter = null
        imageSlider = null
        featured = null
        price = null
        textDesc = null
        secondary = null
        houses = null
        commercial = null
        viewMap = null
        roomSize = null
        totalArea = null
        typeOfLayout = null
        repair = null
        bathroom = null
        balcony = null
        floors = null
        yearOfConstruction = null
        wallMaterials = null
        areaHouse = null
        numberOfRooms = null
        numberOfFloors = null
        yearOfConstruction1 = null
        wallMaterials1 = null
        powerSupply = null
        heating = null
        gasSupply = null
        sewage = null
        plotArea = null
        square = null
        floors1 = null
        mView = null
        progress = null
        noConnection = null
        viewRatingDialog = null
        agentRate = null
        rating = null
        agentTotalRates = null
        reviewAdapter = null
        reviewRecycler = null
        viewMore = null
        photoAgent = null
        name = null
        similarAdapter = null
        propertyRecycler = null
        related = null
        avatar = null
        nameAgent = null
        buttonCall = null
        buttonChat = null
        bottomNavigationView = null
    }
}
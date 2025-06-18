package ru.crmkurgan.main.ui.property

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.runtime.image.ImageProvider
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.Functions


class MapFragment : Fragment() {
    private var mView: MapView? = null
    private lateinit var contentView: View
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::contentView.isInitialized) {
            val view = inflater.inflate(R.layout.fragment_map, container, false)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val window = requireActivity().window
            val lp: ViewGroup.MarginLayoutParams =
                toolbar.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = Functions.getStatusBarHeight(resources, window)
            val activity1 = activity
            (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
            val actionBar = activity1.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setHomeAsUpIndicator(R.drawable.close)
            activity1.title = ""
            window.statusBarColor = Color.TRANSPARENT
            val decor = window.decorView
            if (Build.VERSION.SDK_INT < 30) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                decor.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else if (Build.VERSION.SDK_INT >= 30) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val lp1: WindowManager.LayoutParams = window.attributes
                lp1.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = lp1
            }
            val mapView = view.findViewById<MapView>(R.id.mapview)
            val panorama = view.findViewById<ImageButton>(R.id.panorama)
            val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
            bottomNavigationView1?.visibility = View.GONE
            bottomNavigationView = bottomNavigationView1
            val bundle1 = arguments
            bundle1?.let {
                try {
                    val lat = it.getString(Constants.LAT)
                    val lng = it.getString(Constants.LNG)
                    lat?.let { it1 ->
                        lng?.let { it2 ->
                            val location = Point(it1.toDouble(), it2.toDouble())
                            mapView?.map?.let { it3 ->
                                val mapObjects = it3.mapObjects
                                mapObjects.addCollection()
                                val iconStyle = IconStyle()
                                iconStyle.scale = 2.0F
                                val mark: PlacemarkMapObject =
                                    mapObjects.addPlacemark(location)
                                mark.setIcon(
                                    ImageProvider.fromResource(
                                        requireContext(),
                                        R.drawable.mark
                                    )
                                )
                                //mark.setIconStyle(iconStyle)
                                it3.move(
                                    CameraPosition(location, 17.0f, 0.0f, 0.0f)
                                )
                                panorama.setOnClickListener {
                                    val bundle = bundleOf(
                                        Pair(Constants.LAT, it1),
                                        Pair(Constants.LNG, it2)
                                    )
                                    findNavController().navigate(
                                        R.id.action_mapFragment_to_panoramaFragment,
                                        bundle
                                    )
                                }
                                val instance = PlacesFactory.getInstance()
                                val panoramaService = instance.createPanoramaService()
                                panoramaService.findNearest(
                                    location,
                                    object : PanoramaService.SearchListener {
                                        override fun onPanoramaSearchResult(p0: String) {
                                        }

                                        override fun onPanoramaSearchError(p0: com.yandex.runtime.Error) {
                                            panorama?.visibility = View.GONE
                                        }
                                    })
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
                mView = mapView
            }
            contentView = view
        }
        return contentView
    }

    override fun onStop() {
        mView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mView?.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mView = null
        bottomNavigationView = null
    }
}
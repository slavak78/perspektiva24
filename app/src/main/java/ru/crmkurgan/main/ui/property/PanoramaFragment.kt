package ru.crmkurgan.main.ui.property

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.places.panorama.PanoramaView
import com.yandex.runtime.Error
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.Functions


class PanoramaFragment : Fragment() {
    private var panorama: PanoramaView? = null
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_panorama, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = activity1.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.close)
        activity1.title = ""
        val window = requireActivity().window
        val decor = window.decorView
        val lp: ViewGroup.MarginLayoutParams =
            toolbar.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = Functions.getStatusBarHeight(resources, window)
        window.statusBarColor = Color.TRANSPARENT
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
        val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1?.visibility = View.GONE
        bottomNavigationView = bottomNavigationView1
        val panorama1 = view.findViewById<PanoramaView>(R.id.panorama)
        val bundle1 = arguments
        bundle1?.let {
            try {
                val lat = it.getString(Constants.LAT)
                val lng = it.getString(Constants.LNG)
                lat?.let { it1 ->
                    lng?.let { it2 ->
                        val location = Point(it1.toDouble(), it2.toDouble())
                        val instance = PlacesFactory.getInstance()
                        val panoramaService = instance.createPanoramaService()
                        try {
                            panoramaService.findNearest(
                                location,
                                object : PanoramaService.SearchListener {
                                    override fun onPanoramaSearchResult(p0: String) {
                                        val player = panorama1.player
                                        player.openPanorama(p0)
                                        player.enableMove()
                                        player.enableRotation()
                                        player.enableZoom()
                                        player.enableMarkers()
                                    }

                                    override fun onPanoramaSearchError(p0: Error) {
                                        val toast = Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.errorPanorama),
                                            Toast.LENGTH_LONG
                                        )
                                        toast.show()
                                    }

                                })
                        } catch (e: Exception) {
                            val toast = Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.errorPanorama),
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        panorama = panorama1
        return view
    }

    override fun onStop() {
        panorama?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        panorama?.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        panorama = null
        bottomNavigationView = null
    }
}
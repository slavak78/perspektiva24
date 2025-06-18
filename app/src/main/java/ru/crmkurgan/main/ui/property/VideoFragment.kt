package ru.crmkurgan.main.ui.property

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import player.Jzvd
import player.JzvdStd
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R

class VideoFragment : Fragment() {
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        window.decorView.systemUiVisibility = 0
        val view = inflater.inflate(R.layout.video, container, false)
        val toolbar1 = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar1.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar1)
        val actionBar = activity1.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.close_white)
        activity1.title = ""
        val video = view.findViewById<JzvdStd>(R.id.video)
        val bottomNavigationView1 = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1?.visibility = View.GONE
        bottomNavigationView = bottomNavigationView1
        val bundle1 = arguments
        bundle1?.let {
            val videoURL = it.getString(Constants.VIDEO)
            val thumbnail = it.getString(Constants.THUMBNAIL)
            Jzvd.WIFI_TIP_DIALOG_SHOWED = true
            video.setUp(videoURL, "")
            Glide.with(requireContext())
                .load(thumbnail)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(video.posterImageView)
            video.startVideo()
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomNavigationView = null
    }
}
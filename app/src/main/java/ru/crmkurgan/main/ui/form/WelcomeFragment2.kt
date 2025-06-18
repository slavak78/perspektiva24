package ru.crmkurgan.main.ui.form

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity

class WelcomeFragment2 : Fragment() {

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPref: SharedPreferences =
            requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val uid = sharedPref.getInt(Constants.UID, 0)
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_welcome2, container, false)
        val start = view.findViewById<MaterialButton>(R.id.start)
        start.setOnClickListener {
            if (uid == 0) {
                findNavController().navigate(R.id.action_welcomeFragment2_to_startUpFragment)
            } else {
                val intent = Intent(context, DashboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        return view
    }
}
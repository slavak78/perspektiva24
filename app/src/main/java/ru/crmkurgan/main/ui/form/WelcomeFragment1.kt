package ru.crmkurgan.main.ui.form

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.ui.dashboard.DashboardActivity

class WelcomeFragment1 : Fragment() {

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPref: SharedPreferences =
            requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(Constants.WELCOME, 1)
        editor.apply()
        val uid = sharedPref.getInt(Constants.UID, 0)
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_welcome1, container, false)
        val skip = view.findViewById<TextView>(R.id.skip)
        skip.setOnClickListener {
            if (uid == 0) {
                findNavController().navigate(R.id.action_welcomeFragment1_to_startUpFragment)
            } else {
                val intent = Intent(context, DashboardActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        val next = view.findViewById<MaterialButton>(R.id.next)
        next.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment1_to_welcomeFragment3)
        }
        return view
    }
}
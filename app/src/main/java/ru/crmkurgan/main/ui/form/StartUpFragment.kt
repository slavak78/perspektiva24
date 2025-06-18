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

class StartUpFragment : Fragment() {
    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_start_up, container, false)
        val signOut = view.findViewById<MaterialButton>(R.id.signOut)
        signOut.setOnClickListener {
            findNavController().navigate(R.id.action_startUpFragment_to_signUpFragment)
        }
        val skip = view.findViewById<TextView>(R.id.skip)
        skip.setOnClickListener {
            val sharedPref =
                requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt(Constants.SKIPPING, 1)
            editor.apply()
            val intent = Intent(requireActivity(), DashboardActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return view
    }
}
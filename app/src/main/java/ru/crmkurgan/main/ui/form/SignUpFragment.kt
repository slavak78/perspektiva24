package ru.crmkurgan.main.ui.form

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils


class SignUpFragment : Fragment() {

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = requireActivity().window
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity1.setTitle("")
        val signOut = view.findViewById<MaterialButton>(R.id.button_sign_up)
        val phone = view.findViewById<EditText>(R.id.phone)
        signOut.setOnClickListener {
            val phoneText = phone.text
            if (phoneText.toString().length < 18) {
                Functions.viewDialog(R.string.errorPhone, requireContext())
            } else {
                if (!NetworkUtils.isConnected(requireContext())) {
                    Functions.viewDialog(R.string.nointernet, requireContext())
                } else {
                    val bundle = bundleOf(
                        Pair(Constants.PHONE, phoneText.toString())
                    )
                    findNavController().navigate(
                        R.id.action_signUpFragment_to_verifyAccountFragment,
                        bundle
                    )
                }
            }
        }
        val policy = view.findViewById<TextView>(R.id.policy)
        val ss = SpannableString(resources.getString(R.string.policy))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                findNavController().navigate(
                    R.id.action_signUpFragment_to_policyFragment
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 45, 64, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        policy.text = ss
        policy.movementMethod = LinkMovementMethod.getInstance()
        return view
    }
}

/*    private fun isValidEmail(target: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }*/

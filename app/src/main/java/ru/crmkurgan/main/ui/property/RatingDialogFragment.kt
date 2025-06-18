package ru.crmkurgan.main.ui.property

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import java.io.IOException

class RatingDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "RatingDialogFragment"
        var uid: Int = 0
        var agent: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_write_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val close = view.findViewById<ImageView>(R.id.image_close)
        close.setOnClickListener {
            dismiss()
        }
        val sharedPref =
            requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        uid = sharedPref.getInt(Constants.UID, 0)
        val submit = view.findViewById<MaterialButton>(R.id.button_submit)
        submit.setOnClickListener {
            val ratingReview = view.findViewById<RatingBar>(R.id.rating_package)
            val rating = ratingReview.rating
            val textReview = view.findViewById<AppCompatEditText>(R.id.textReview)
            if ((rating == 0.0F) or (textReview.text.toString() == "")) {
                Functions.viewDialog(R.string.noReview, requireContext())
            } else {
                postReview(rating, textReview.text.toString())
            }

        }
    }

    private fun postReview(rating: Float, textReview: String) {
        if (!NetworkUtils.isConnected(requireContext())) {
            Functions.viewDialog(R.string.nointernet, requireContext())
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            formBodyBuilder.add(Constants.RATING_VALUE, rating.toString())
            formBodyBuilder.add(Constants.RATING_TEXT, textReview)
            formBodyBuilder.add(Constants.UID, uid.toString())
            formBodyBuilder.add(Constants.AGENT, agent)
            requestBuilder.url(Constants.RATING)
            requestBuilder.post(formBodyBuilder.build())
            NetworkUtils.httpClient().newCall(requestBuilder.build())
                .enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                            resp.body?.let { body ->
                                val content = body.string()
                                requireActivity().runOnUiThread {
                                    processReview(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processReview(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            dismiss()
            Functions.viewDialog(code, requireContext())
        } catch (ignored: Exception) {
        }
    }
}
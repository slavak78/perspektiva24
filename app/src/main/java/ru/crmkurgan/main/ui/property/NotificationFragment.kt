package ru.crmkurgan.main.ui.property

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.adapters.NotificationAdapter
import ru.crmkurgan.main.items.Notification
import ru.crmkurgan.main.utils.Functions
import ru.crmkurgan.main.utils.NetworkUtils
import ru.crmkurgan.main.utils.NotificationsDiffUtilCallback
import java.io.IOException
import java.util.*

class NotificationFragment : Fragment() {
    private var notificationList = ArrayList<Notification>()
    private var oldNotificationList = ArrayList<Notification>()
    private var notificationAdapter: NotificationAdapter? = null
    private var notificationRecycler: RecyclerView? = null
    private var new1: TextView? = null
    private var clearAll: TextView? = null
    private val timer = Timer()
    private var uid = 0
    private var bottomNavigationView: BottomNavigationView? = null

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        val activity1 = activity
        (activity1 as AppCompatActivity).setSupportActionBar(toolbar)
        activity1.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity1.title = resources.getString(R.string.notification)
        setHasOptionsMenu(true)
        new1 = view.findViewById(R.id.new1)
        clearAll = view.findViewById(R.id.text_clear_all)
        val bottomNavigationView1 =
            requireActivity().findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        bottomNavigationView1?.visibility = View.GONE
        bottomNavigationView = bottomNavigationView1
        val notificationRecycler1 = view.findViewById<RecyclerView>(R.id.recycler_view_notification)
        val notificationAdapter1 = NotificationAdapter(notificationList, requireContext())
        notificationRecycler1?.adapter = notificationAdapter1
        notificationRecycler1.layoutManager = GridLayoutManager(requireContext(), 1)
        notificationRecycler1.setHasFixedSize(true)
        notificationRecycler = notificationRecycler1
        val sharedPref: SharedPreferences =
            requireActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
        val uid1 = sharedPref.getInt(Constants.UID, 0)
        if (uid1 != 0) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    requireActivity().runOnUiThread {
                        getNotifications(uid1)
                    }
                }
            }, 0, 60000)
        }
        notificationAdapter = notificationAdapter1
        uid = uid1
        clearAll?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.confirmation)
            builder.setMessage(R.string.confirmDelNotifications)
            builder.setPositiveButton(R.string.yes) { _, _ ->
                deleteNotifications(uid)
            }
            builder.setNegativeButton(R.string.no) { _, _ -> }
            builder.show()
        }
        return view
    }

    private fun deleteNotifications(uid: Int) {
        if (!NetworkUtils.isConnected(requireContext())) {
            Functions.viewDialog(R.string.nointernet, requireContext())
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            formBodyBuilder.add(Constants.UID, uid.toString())
            requestBuilder.url(Constants.DELETE_NOTIFICATIONS)
            requestBuilder.post(formBodyBuilder.build())
            NetworkUtils.httpClient().newCall(requestBuilder.build())
                .enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        requireActivity().runOnUiThread {
                            Functions.viewDialog(R.string.noserver, requireContext())
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                            resp.body?.let {
                                requireActivity().runOnUiThread {
                                    notificationList.clear()
                                    notificationAdapter?.notifyDataSetChanged()
                                    new1?.text = resources.getString(R.string._5_new_notification)
                                    clearAll?.visibility = View.INVISIBLE
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun getNotifications(uid: Int) {
        if (!NetworkUtils.isConnected(requireContext())) {
            Functions.viewDialog(R.string.nointernet, requireContext())
        } else {
            val requestBuilder = Request.Builder()
            val formBodyBuilder = FormBody.Builder()
            formBodyBuilder.add(Constants.UID, uid.toString())
            requestBuilder.url(Constants.NOTIFICATIONS)
            requestBuilder.post(formBodyBuilder.build())
            NetworkUtils.httpClient().newCall(requestBuilder.build())
                .enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        requireActivity().runOnUiThread {
                            Functions.viewDialog(R.string.noserver, requireContext())
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) throw IOException("Unexpected code $response")
                            resp.body?.let { body ->
                                val content = body.string()
                                requireActivity().runOnUiThread {
                                    processNotifications(content)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun processNotifications(content: String) {
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.optString(Constants.CODE)
            if (code == "200") {
                val notificationList1 = notificationList
                notificationList1.clear()
                val oldNotificationList1 = oldNotificationList
                val new = jsonObject.optString(Constants.NEW)
                new1?.text = new
                val msg = jsonObject.getJSONArray(Constants.MSG)
                if (msg.length() > 0) {
                    clearAll?.visibility = View.VISIBLE
                }
                for (i in 0 until msg.length()) {
                    val userdata = msg.getJSONObject(i)
                    val notification = Notification(
                        userdata.optLong(Constants.ID),
                        userdata.optString(Constants.IMAGE),
                        userdata.optString(Constants.NOTIFICATION),
                        userdata.optString(Constants.DATE),
                        userdata.optString(Constants.NAME)
                    )
                    notificationList1.add(notification)
                }
                val notificationsDiffUtilCallback =
                    NotificationsDiffUtilCallback(oldNotificationList1, notificationList1)
                val notificationsDiffResult = DiffUtil.calculateDiff(notificationsDiffUtilCallback)
                notificationAdapter?.let { notificationsDiffResult.dispatchUpdatesTo(it) }
                oldNotificationList1.clear()
                for (i in 0 until notificationList1.size) {
                    oldNotificationList1.add(notificationList1[i])
                }
            }
        } catch (ignored: Exception) {
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        val item: MenuItem = menu.findItem(R.id.action_notification)
        item.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
        bottomNavigationView = null
    }
}
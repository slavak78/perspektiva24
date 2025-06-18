package ru.crmkurgan.main.adapters

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Property
import ru.crmkurgan.main.utils.ObjectsDBSQLiteHelper

class AgentListingAdapter(private val propertyList: ArrayList<Property>, context: Context) :
    RecyclerView.Adapter<AgentListingAdapter.ViewHolder>() {
    private val context1 = context
    private val db = ObjectsDBSQLiteHelper(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_featured_property, parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return propertyList.size
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = propertyList[position]
        val id = item.id
        val table = item.table
        Glide.with(context1)
            .load(item.image)
            .error(R.drawable.ic_logo)
            .placeholder(R.drawable.ic_logo)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.image)
        val type = item.type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.type.text = Html.fromHtml(
                type,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        } else {
            holder.type.text = Html.fromHtml(
                type
            )
        }
        holder.price.text = item.price
        holder.address.text = item.address
        val selectionArgs = arrayOf(item.id, item.table)
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
        val favorite = holder.favorite
        val context2 = context1
        favorite.setImageDrawable(context2.getDrawable(if (cursor.count > 0) R.drawable.ic_favorite_red else R.drawable.ic_favorite))
        favorite.setOnClickListener {
            favoriteId(id, table, favorite)
        }
        cursor.close()
        db1.close()
        val bundle = bundleOf(
            Pair(Constants.ID, item.id),
            Pair(Constants.TABLE, item.table)
        )
        holder.v.setOnClickListener {
            it.findNavController().navigate(
                R.id.propertyFragment,
                bundle
            )
        }
    }

    private fun favoriteId(id: String, table: String, favorite: ImageView) {
        val selectionArgs = arrayOf(id, table)
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
                favorite.setImageDrawable(context1.getDrawable(R.drawable.ic_favorite))
            }
        } else {
            val values = ContentValues()
            values.put(Constants.INTERNAL_ID, id)
            values.put(Constants.INTERNAL_TABLE, table)
            val success = db1.insert(Constants.DATABASE_NAME, null, values)
            if (success.toInt() != -1) {
                favorite.setImageDrawable(context1.getDrawable(R.drawable.ic_favorite_red))
            }
        }
        db1.close()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val type: TextView = itemView.findViewById(R.id.type)
        val price: TextView = itemView.findViewById(R.id.price)
        val address: TextView = itemView.findViewById(R.id.address)
        val favorite: ImageView = itemView.findViewById(R.id.image_favorite)
        val v: View = itemView
    }
}
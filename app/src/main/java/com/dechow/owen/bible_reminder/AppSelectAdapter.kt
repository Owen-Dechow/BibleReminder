package com.dechow.owen.bible_reminder

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    var isChecked: Boolean,
    var onToggle: (Boolean) -> Unit
)

class AppSelectAdapter(private val apps: List<AppItem>) :
    RecyclerView.Adapter<AppSelectAdapter.AppViewHolder>() {

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val switch: Switch = view.findViewById(R.id.app_switch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_row, parent, false)
        return AppViewHolder(view)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        holder.icon.setImageDrawable(app.icon)
        holder.switch.text = app.label

        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.isChecked = app.isChecked

        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            app.isChecked = isChecked
            app.onToggle(isChecked)
        }
    }
}

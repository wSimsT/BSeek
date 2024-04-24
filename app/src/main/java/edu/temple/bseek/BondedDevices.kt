package edu.temple.bseek

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// custom adapter class to generate textviews using the colorName array
class BondedDevices(private val context: Context, private val deviceNames: Array<String>): RecyclerView.Adapter<BondedDevices.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView2)
    }
    fun getCount() = deviceNames.size;
    fun getItem(position: Int) = deviceNames[position]
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BondedDevices.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.bonded_devices, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BondedDevices.MyViewHolder, position: Int) {
        holder.textView.text = deviceNames[position]
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount(): Int {
        return deviceNames.size
    }

    fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getDropDownView(position, convertView, parent).apply {
            (this as TextView).textSize = 22f
        }
    }

    private fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // convertView stores references to view we will recycle
        val textView: TextView

        // *late instantiation of a val
        if (convertView != null) {
            textView = convertView as TextView
        } else {
            textView = TextView(context)
            textView.setPadding(5, 10, 0, 10)
        }

        // set the text, padding and backgroundColor of each textView in the adapter
        textView.text = deviceNames[position]
        textView.setPadding(5, 10, 0, 10)
        textView.setBackgroundColor(Color.parseColor(deviceNames[position]))

        return textView
    }
}
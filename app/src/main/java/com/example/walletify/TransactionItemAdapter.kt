package com.example.walletify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionItemAdapter(val data: List<TransactionItem>): RecyclerView.Adapter<TransactionItemAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.item_image)
        val category = view.findViewById<TextView>(R.id.item_category)
        val cost = view.findViewById<TextView>(R.id.item_cost)
        val itemNote = view.findViewById<TextView>(R.id.item_note)
        val datetime = view.findViewById<TextView>(R.id.item_datetime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.image.setImageResource(item.imageId)
        holder.category.text = item.category
        holder.cost.text = String.format("$" + item.cost.toString())
        holder.itemNote.text = item.note
        holder.datetime.text = item.datetime
    }

}
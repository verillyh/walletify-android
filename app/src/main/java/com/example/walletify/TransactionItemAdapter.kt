package com.example.walletify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.walletify.data.Transaction
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionItemAdapter(): RecyclerView.Adapter<TransactionItemAdapter.ViewHolder>() {
    private var transactionList = emptyList<Transaction>()
    private val datetimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("en", "AU"))

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.transaction_image)
        val category = view.findViewById<TextView>(R.id.transaction_category)
        val cost = view.findViewById<TextView>(R.id.transaction_total)
        val itemNote = view.findViewById<TextView>(R.id.transaction_note)
        val datetime = view.findViewById<TextView>(R.id.transaction_datetime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = transactionList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transactionList[position]

        holder.image.setImageResource(item.category.imageResId)
        holder.category.text = item.category.displayName
        holder.cost.text = String.format(Locale("en", "AU"), "\$%.2f", item.amount)
        holder.itemNote.text = item.note
        holder.datetime.text = item.datetime.format(datetimeFormatter)
    }

    fun setData(transactions: List<Transaction>) {
        this.transactionList = transactions
        notifyDataSetChanged()
    }
}
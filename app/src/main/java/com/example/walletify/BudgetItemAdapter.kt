package com.example.walletify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class BudgetItemAdapter(val data: List<BudgetItem>): RecyclerView.Adapter<BudgetItemAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.budget_image)
        val category = view.findViewById<TextView>(R.id.budget_category)
        val budgetTotal = view.findViewById<TextView>(R.id.budget_total)
        val budgetProgress = view.findViewById<LinearProgressIndicator>(R.id.budget_progress)
        val budgetPercent = view.findViewById<TextView>(R.id.budget_progress_percent)
        val budgetLeft = view.findViewById<TextView>(R.id.budget_left)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val budgetPercent = item.budgetLeft / item.totalBudget * 100

        holder.image.setImageResource(item.imageId)
        holder.category.text = item.category
        holder.budgetTotal.text = String.format("$%.0f", item.totalBudget)
        holder.budgetLeft.text = String.format("$%.0f", item.budgetLeft)


        holder.budgetPercent.text = String.format("%.0f%%", budgetPercent)
        holder.budgetProgress.max = item.totalBudget.toInt()
        holder.budgetProgress.progress = item.budgetLeft.toInt()
    }

}
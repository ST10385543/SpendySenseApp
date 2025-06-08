package com.example.spendysenseapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Categories
import com.google.android.material.button.MaterialButton

class CategoryAdapter(
    private val categories: List<Categories>,
    private val onDelete: (Categories) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.categoryIconIv)
        val name: TextView = itemView.findViewById(R.id.categoryName)
        val deleteBtn: MaterialButton = itemView.findViewById(R.id.deleteCategoryBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_list_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.name.text = category.CategoryName

        // Load icon from resource id stored as string
        val iconResId = category.iconImgPath.toIntOrNull()
        if (iconResId != null && iconResId != 0) {
            holder.icon.setImageResource(iconResId)
        } else {
            holder.icon.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.deleteBtn.setOnClickListener { onDelete(category) }
    }

    override fun getItemCount(): Int = categories.size
}
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

//this method is used for the category recycler view in the
//filter by category view
//it contains a button which passes the delete
//action to the view
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

        // Set icon using drawable name (string), fallback to default
        val imgPath = category.iconImgPath
        if (!imgPath.isNullOrEmpty()) {
            val context = holder.itemView.context
            val resId = context.resources.getIdentifier(imgPath, "drawable", context.packageName)
            holder.icon.setImageResource(if (resId != 0) resId else R.drawable.ic_launcher_foreground)
        } else {
            holder.icon.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.deleteBtn.setOnClickListener { onDelete(category) }
    }

    override fun getItemCount(): Int = categories.size
}

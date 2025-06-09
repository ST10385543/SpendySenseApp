package com.example.spendysenseapp.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Achievements

//This method is the adapter for the achievements recycler view
//it gets the achievement icon, its name, difficulty, description and other important
//information
class AchievementAdapter(
    private val achievements: List<Achievements>,
    private val isLockedList: Boolean = false
    ) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.achievementIcon)
        val name: TextView = view.findViewById(R.id.achievementName)
        val description: TextView = view.findViewById(R.id.achievementDescription)
        val difficulty: TextView = view.findViewById(R.id.achievementDifficulty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievement_list_item, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.name.text = achievement.achievementName
        holder.description.text = achievement.achievementDescription
        holder.difficulty.text = achievement.achievementDifficulty

        //this code checks the difficulty of the achievement, and assigns the text view
        //a color depending
        when (achievement.achievementDifficulty) {
            "easy" -> {
                holder.difficulty.setTextColor(Color.parseColor("#5fbb37"))
            }
            "medium" -> {
                holder.difficulty.setTextColor(Color.parseColor("#FFC300"))
            }
            "hard" -> {
                holder.difficulty.setTextColor(Color.parseColor("#FF0000"))
            }
            else -> {
                holder.difficulty.setTextColor(Color.parseColor("#9EB1CD"))
            }
        }

        // Get icon resource by name from achievementIconPath
        val context = holder.icon.context
        val resourceId = context.resources.getIdentifier(
            achievement.achievementIconPath,
            "drawable",
            context.packageName
        )
        if (resourceId != 0) {
            holder.icon.setImageResource(resourceId)
        } else {
            //this is a fallback icon
            holder.icon.setImageResource(R.drawable.ic_launcher_background)
        }

        //this list checks the state of the list of locked achievements, and
        //assigns different colors for the ui element
        if (isLockedList) {
            // Grey out the icon and text
            holder.itemView.alpha = 0.4f
            holder.name.setTextColor(Color.GRAY)
            holder.description.setTextColor(Color.GRAY)
        } else {
            holder.itemView.alpha = 1.0f
        }
    }
    override fun getItemCount(): Int = achievements.size
}

package com.example.spendysenseapp

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

//This will randomly give icons a color for user friendliness

class SelectIconActivity : AppCompatActivity() {

    //Random color list for icons
    private val colorList = listOf(
        "#FF9800", // Orange
        "#4CAF50", // Green
        "#03A9F4", // Light Blue
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#3F51B5", // Indigo
        "#009688", // Teal
        "#F44336", // Red
        "#8BC34A", // Light Green
        "#FFEB3B", // Yellow
        "#795548", // Brown
        "#607D8B", // Blue Grey
        "#CDDC39", // Lime
        "#FFC107", // Amber
        "#00BCD4", // Cyan
        "#FF5722", // Deep Orange
        "#673AB7", // Deep Purple
        "#00E5FF", // Bright Cyan
        "#C51162", // Magenta
        "#AEEA00"  // Yellow Green
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_icon) // Update if needed

        // IDs of the icon ImageViews
        val iconViews = listOf(
            R.id.imgPhone,
            R.id.imgGroceries,
            R.id.imgGiveGift,
            R.id.imageView20,
            R.id.imageView21,
            R.id.imageView22,
            R.id.imgRecGift,
            R.id.imgSalary,
            R.id.imgHome,
            R.id.imgcar,
            R.id.imgHealth,
            R.id.imgEdu
        )

        for (id in iconViews) {
            val imageView = findViewById<ImageView>(id)
            val randomColor = colorList.random()

            val drawable = ContextCompat.getDrawable(this, R.drawable.circle_icon_select)
            val wrappedDrawable: Drawable = DrawableCompat.wrap(drawable!!)
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(randomColor))

            // Developers, n.d. , DrawableWrapper [Online]
            // Available at: https://developer.android.com/reference/android/graphics/drawable/DrawableWrapper
            imageView.background = wrappedDrawable
        }
    }
}

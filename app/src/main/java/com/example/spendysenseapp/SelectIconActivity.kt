package com.example.spendysenseapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class SelectIconActivity : AppCompatActivity() {

    private val colorList = listOf(
        "#FF9800", "#4CAF50", "#03A9F4", "#E91E63", "#9C27B0", "#3F51B5", "#009688",
        "#F44336", "#8BC34A", "#FFEB3B", "#795548", "#607D8B", "#CDDC39", "#FFC107",
        "#00BCD4", "#FF5722", "#673AB7", "#00E5FF", "#C51162", "#AEEA00"
    )

    // Map each ImageView ID to its drawable icon resource
    private val iconMap = mapOf(
        R.id.imgPhone to R.drawable.smartphone,
        R.id.imgGroceries to R.drawable.groceries,
        R.id.imgGiveGift to R.drawable.recieve_a_gift,  //mix up in names
        R.id.imgGym to R.drawable.dumbbell,
        R.id.imgGame to R.drawable.game,
        R.id.imgMountains to R.drawable.mountains,
        R.id.imgRecGift to R.drawable.give_a_gift,      //mix up in names
        R.id.imgSalary to R.drawable.salary,
        R.id.imgHome to R.drawable.home,
        R.id.imgcar to R.drawable.sedan,
        R.id.imgHealth to R.drawable.healthcare,
        R.id.imgEdu to R.drawable.graduation_cap,
        R.id.imgSports to R.drawable.sports,
        R.id.imgFastFood to R.drawable.fastfood,
        R.id.imgBaby to R.drawable.baby,
        R.id.imgRepairs to R.drawable.repairs,
        R.id.imgFlight to R.drawable.flight,
        R.id.imgHoliday to R.drawable.holidays,
        R.id.imgShopping to R.drawable.shopping,
        R.id.imgPets to R.drawable.pets,
        R.id.imgWork to R.drawable.work,
        R.id.imgTelevision to R.drawable.television,
        R.id.imgCinema to R.drawable.cinema,
        R.id.imgElectronics to R.drawable.electronics,


    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_icon)

        for ((viewId, iconResId) in iconMap) {
            val imageView = findViewById<ImageView>(viewId)
            val randomColor = colorList.random()

            val drawable = ContextCompat.getDrawable(this, R.drawable.circle_icon_select)
            val wrappedDrawable: Drawable = DrawableCompat.wrap(drawable!!)
            DrawableCompat.setTint(wrappedDrawable, Color.parseColor(randomColor))
            imageView.background = wrappedDrawable

            imageView.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra("icon_res_id", iconResId)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}

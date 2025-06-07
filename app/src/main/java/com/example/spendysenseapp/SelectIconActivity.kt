package com.example.spendysenseapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class SelectIconActivity : AppCompatActivity() {

    private val colorList = listOf(
        "#FF9800", // Orange
        "#4CAF50", // Green
        "#03A9F4", // Light Blue
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#3F51B5", // Indigo
        "#009688", // Teal
        "#F44336", // Red
        "#8BC34A", // Lime
        "#FFEB3B", // Yellow
        "#795548", // Brown
        "#607D8B", // Blue Grey
        "#CDDC39", // Light Green
        "#FFC107", // Amber
        "#00BCD4", // Cyan
        "#FF5722", // Deep Orange
        "#673AB7", // Deep Purple
        "#00E5FF", // Bright Cyan
        "#C51162", // Magenta
        "#AEEA00", // Light Lime
        "#001F54", // Navy
        "#FFD700", // Gold
        "#808000", // Olive
        "#FF7F50", // Coral
        "#87CEEB", // Sky Blue
        "#FF6347", // Tomato
        "#2E8B57", // Sea Green
        "#DC143C", // Crimson
        "#40E0D0", // Turquoise
        "#DDA0DD"  // Plum
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
        R.id.imgBirthday to R.drawable.birthday,
        R.id.imgDessert to R.drawable.dessert,
        R.id.imgCooking to R.drawable.cooking,
        R.id.imgAquarium to R.drawable.aquarium,
        R.id.imgWaterPark to R.drawable.waterpark,
        R.id.imgZoo to R.drawable.zoo,
        R.id.imgCruise to R.drawable.cruise,
        R.id.imgParty to R.drawable.party,
        R.id.imgRollerCoaster to R.drawable.rollercoaster,
        R.id.imgLights to R.drawable.lights,
        R.id.imgWater to R.drawable.water,
        R.id.imgSchool to R.drawable.school
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_icon)

        val backButton = findViewById<Button>(R.id.Backtwobtn)
        backButton.setOnClickListener {
            finish()
        }

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

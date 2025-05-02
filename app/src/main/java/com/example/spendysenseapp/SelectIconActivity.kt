package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SelectIconActivity : AppCompatActivity() {

    private val iconMap = mapOf(
        R.id.imgPhone to R.drawable.smartphone,
        R.id.imgGroceries to R.drawable.groceries,
        R.id.imgGiveGift to R.drawable.recieve_a_gift,
        R.id.imageView20 to R.drawable.dumbbell,
        R.id.imageView22 to R.drawable.game,
        R.id.imageView21 to R.drawable.mountains,
        R.id.imgRecGift to R.drawable.give_a_gift,
        R.id.imgSalary to R.drawable.salary,
        R.id.imgHome to R.drawable.home,
        R.id.imgcar to R.drawable.sedan,
        R.id.imgHealth to R.drawable.healthcare,
        R.id.imgEdu to R.drawable.graduation_cap
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_icon)

        for ((viewId, drawableRes) in iconMap) {
            findViewById<ImageView>(viewId).setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("ICON_RES_ID", drawableRes)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}

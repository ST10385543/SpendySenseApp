package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var imgIcon: ImageView

    private val selectIconLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val iconResId = result.data?.getIntExtra("ICON_RES_ID", -1)
            if (iconResId != null && iconResId != -1) {
                imgIcon.setImageResource(iconResId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        imgIcon = findViewById(R.id.imgIcon)

        val btnSelectIcon = findViewById<Button>(R.id.btnSelectIcon)
        btnSelectIcon.setOnClickListener {
            val intent = Intent(this, SelectIconActivity::class.java)
            selectIconLauncher.launch(intent)
        }
    }
}

package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.CategoriesDao
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var imgIcon: ImageView
    private lateinit var edtCategoryName: EditText
    private lateinit var db: SpendySenseDatabase
    private lateinit var categoryDao: CategoriesDao

    private var selectedIconResId: Int = -1

    private val selectIconLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val iconResId = result.data?.getIntExtra("ICON_RES_ID", -1)
            if (iconResId != null && iconResId != -1) {
                selectedIconResId = iconResId
                imgIcon.setImageResource(iconResId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        imgIcon = findViewById(R.id.imgIcon)
        edtCategoryName = findViewById(R.id.edtCategoryName)

        val btnSelectIcon = findViewById<Button>(R.id.btnSelectIcon)
        val btnSaveCategory = findViewById<Button>(R.id.btnCreateCategory)

        db = SpendySenseDatabase.getDatabase(this)
        categoryDao = db.categoryDao()

        btnSelectIcon.setOnClickListener {
            val intent = Intent(this, SelectIconActivity::class.java)
            selectIconLauncher.launch(intent)
        }

        btnSaveCategory.setOnClickListener {
            val categoryName = edtCategoryName.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIconResId == -1) {
                Toast.makeText(this, "Please select an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val iconPath = selectedIconResId.toString()

            val newCategory = Categories(
                CategoryName = categoryName,
                iconImgPath = iconPath
            )

            lifecycleScope.launch(Dispatchers.IO) {
                categoryDao.insertCategory(newCategory)
                runOnUiThread {
                    Toast.makeText(this@CreateCategoryActivity, "Category saved", Toast.LENGTH_SHORT).show()
                    finish() // Optionally close the activity
                }
            }
        }
    }
}

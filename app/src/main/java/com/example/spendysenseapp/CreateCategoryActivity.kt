package com.example.spendysenseapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.CategoriesDao
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.AchievementManager
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
            val iconResId = result.data?.getIntExtra("icon_res_id", -1)
            if (iconResId != null && iconResId != -1) {
                selectedIconResId = iconResId
                imgIcon.setImageResource(iconResId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        val sessionManager = SessionManager.getInstance(this)
        val currentUser = sessionManager.getCurrentUser()

        imgIcon = findViewById(R.id.imgIcon)
        edtCategoryName = findViewById(R.id.edtCategoryName)

        val btnBack = findViewById<Button>(R.id.BackCategorybtn)
        val btnSelectIcon = findViewById<Button>(R.id.btnSelectIcon)
        val btnSaveCategory = findViewById<Button>(R.id.btnCreateCategory)

        btnBack.setOnClickListener {
            finish()
        }

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

            val categoryId = "Category_${
                UUID.randomUUID().toString().substring(0, 8)
            }_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"

            val newCategory = Categories(
                id = categoryId,
                CategoryName = categoryName,
                iconImgPath = iconPath,
                userId = currentUser?.uid ?: ""
            )
            //this is the example of a counter variable achievement
            if (currentUser != null) {
                AchievementManager.checkAndUnlock(
                    //pass the id as normal
                    currentUser.uid,
                    //then the event name, again gotten from the achievementManager class
                    "make_3_categories",
                    //then an onUnlocked event
                    //now you SHOULD be able to do it, but i can understand if its still a bit
                    //confusing, ill try to do when i get back
                    onUnlocked = { achievement ->
                        //play easy sound effect
                        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.easy_sound)
                        mediaPlayer?.start()
                        mediaPlayer?.setOnCompletionListener {
                            it.release()
                        }

                        Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}!", Toast.LENGTH_SHORT).show()
                    }
                )
            }


            val firestoreService = FirestoreService("categories", Categories::class.java)
            lifecycleScope.launch {
                try {
                    firestoreService.add(categoryId, newCategory)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Category saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.w("CategoryAddingFailed", "Error creating category", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Failed to create category", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

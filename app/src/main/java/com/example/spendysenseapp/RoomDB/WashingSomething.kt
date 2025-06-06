package com.example.spendysenseapp.RoomDB

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import android.view.View
import com.example.spendysenseapp.R
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class Bubble(
    var x: Float,
    var y: Float,
    val radius: Float,
    val paint: Paint,
    val vy: Float
)
class WashingSomething(context: Context) : View(context) {

    private val bubbles: MutableList<Bubble> = mutableListOf()
    private var screenWidth = 0
    private var screenHeight = 0
    private var score = 0
    private val scorePaint = Paint().apply {
        color = Color.BLACK
        textSize = 80f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // SoundPool setup
    private val soundPool: SoundPool
    private val popSoundId: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
        popSoundId = soundPool.load(context, R.raw.pop_sound_effect, 1)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        soundPool.release()
    }

    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        screenWidth = width
        screenHeight = height
        bubbles.clear()
        repeat(10) {
            bubbles.add(createBubbleAtBottom())
        }
    }

    private fun createBubbleAtBottom(): Bubble {
        val radius = Random.nextInt(50, 100).toFloat()
        val x = Random.nextFloat() * (screenWidth - 2 * radius) + radius
        val y = screenHeight + radius
        val paint = Paint().apply {
            color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            isAntiAlias = true
        }
        val vy = -Random.nextFloat() * 8f - 4f
        return Bubble(x, y, radius, paint, vy)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (bubble in bubbles) {
            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubble.paint)
        }
        canvas.drawText("Score: $score", (screenWidth / 2).toFloat(), 100f, scorePaint)
        updateBubbles()
        postInvalidateOnAnimation()
    }

    private fun updateBubbles() {
        for (i in bubbles.indices) {
            val bubble = bubbles[i]
            bubble.y += bubble.vy
            if (bubble.y + bubble.radius < 0) {
                bubbles[i] = createBubbleAtBottom()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y
            val iterator = bubbles.iterator()
            while (iterator.hasNext()) {
                val bubble = iterator.next()
                val distance = sqrt((bubble.x - touchX).pow(2) + (bubble.y - touchY).pow(2))
                if (distance <= bubble.radius) {
                    iterator.remove()
                    bubbles.add(createBubbleAtBottom())
                    score++
                    soundPool.play(popSoundId, 1f, 1f, 0, 0, 1f)
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
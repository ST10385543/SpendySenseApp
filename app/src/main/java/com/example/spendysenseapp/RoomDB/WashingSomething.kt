package com.example.spendysenseapp.RoomDB

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.example.spendysenseapp.R
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

//wait, youre not supposed to see this
//NOOOOOOOOOO

//anyway, read the names, dont want to spoil the surprise
//okay, but for a brief overview, its code to create a game
data class Bubble(
    var x: Float,
    var y: Float,
    val radius: Float,
    val paint: Paint,
    val vy: Float
)
class WashingSomething(
    context: Context,
    val onTimerFinish: (score: Int) -> Unit,
    val onBackPressed: (() -> Unit)? = null
) : View(context) {

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
    private val backBtnSize = 100f
    private val backBtnPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val backBtnArrowPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
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
        handler.removeCallbacks(timerRunnable)
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
        // Center of the back button circle
        val centerX = backBtnSize / 2 + 20
        val centerY = backBtnSize / 2 + 20
        canvas.drawCircle(centerX, centerY, backBtnSize / 2, backBtnPaint)

        // Centralize the arrow
        val arrowLength = 40f
        val arrowWidth = 24f
        val startX = centerX + arrowLength / 2
        val startY = centerY
        val endX = centerX - arrowLength / 2
        val endY = centerY

        val path = Path()
        // Main line
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        // Upper arrow head
        path.moveTo(endX, endY)
        path.lineTo(endX + arrowWidth / 2, endY - arrowWidth / 2)
        // Lower arrow head
        path.moveTo(endX, endY)
        path.lineTo(endX + arrowWidth / 2, endY + arrowWidth / 2)

        canvas.drawPath(path, backBtnArrowPaint)
        //bubbles
        for (bubble in bubbles) {
            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubble.paint)
        }
        canvas.drawText("Score: $score", (screenWidth / 2).toFloat(), 100f, scorePaint)
        canvas.drawText("Time: $timeLeft", (screenWidth / 2).toFloat(), 180f, timerPaint)
        updateBubbles()
        postInvalidateOnAnimation()
        if (!timerStarted) {
            timerStarted = true
            handler.post(timerRunnable)
        }
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
    private var timeLeft = 15 // seconds
    private val timerPaint = Paint().apply {
        color = Color.RED
        textSize = 60f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private var timerStarted = false

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (timeLeft > 0) {
                timeLeft--
                invalidate()
                handler.postDelayed(this, 1000)
            } else {
                onTimerFinish(score)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y
            // Check if touch is within back button area
            val centerX = backBtnSize / 2 + 20
            val centerY = backBtnSize / 2 + 20
            val dist = sqrt((touchX - centerX).pow(2) + (touchY - centerY).pow(2))
            if (dist <= backBtnSize / 2) {
                onBackPressed?.invoke()
                return true
            }
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
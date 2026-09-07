package com.kuzey.minecraftkomutcu

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var bubble: TextView
    private var panel: View? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    private fun showBubble() {
        bubble = TextView(this).apply {
            text = "AI"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(60,133,39))
                setStroke(dp(2), Color.WHITE)
            }
        }

        val p = WindowManager.LayoutParams(
            dp(58), dp(58),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 180
        }

        var sx = 0; var sy = 0; var tx = 0f; var ty = 0f; var moved = false
        bubble.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = p.x; sy = p.y; tx = e.rawX; ty = e.rawY; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - tx).toInt(); val dy = (e.rawY - ty).toInt()
                    if (abs(dx) > 8 || abs(dy) > 8) moved = true
                    p.x = sx + dx; p.y = sy + dy
                    wm.updateViewLayout(bubble, p); true
                }
                MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); true }
                else -> false
            }
        }
        wm.addView(bubble, p)
    }

    private fun togglePanel() {
        panel?.let { wm.removeView(it); panel = null; return }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.rgb(30,30,30))
                setStroke(dp(1), Color.DKGRAY)
            }
        }

        box.addView(TextView(this).apply {
            text = "Minecraft Komutçu AI"
            textSize = 18f
            setTextColor(Color.WHITE)
        })

        val input = EditText(this).apply {
            hint = "Örn: 5 zombi halay çeksin"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            minLines = 2
        }
        box.addView(input)

        val result = TextView(this).apply {
            text = "Sonuç burada görünecek."
            setTextColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setTextIsSelectable(true)
        }

        val aiButton = Button(this).apply {
            text = "AI ile üret"
            setOnClickListener {
                val request = input.text.toString().trim()
                if (request.isBlank()) return@setOnClickListener
                isEnabled = false
                result.text = "AI düşünüyor..."
                thread {
                    val answer = GroqClient.askMinecraft(this@OverlayService, request)
                    result.post {
                        result.text = answer
                        isEnabled = true
                    }
                }
            }
        }
        box.addView(aiButton)

        box.addView(Button(this).apply {
            text = "Hızlı yerel komut"
            setOnClickListener { result.text = CommandEngine.generate(input.text.toString()) }
        })

        box.addView(result)
        box.addView(Button(this).apply {
            text = "Kopyala"
            setOnClickListener {
                val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cb.setPrimaryClip(ClipData.newPlainText("Minecraft AI sonucu", result.text))
                Toast.makeText(this@OverlayService, "Kopyalandı", Toast.LENGTH_SHORT).show()
            }
        })
        box.addView(Button(this).apply {
            text = "Kapat"
            setOnClickListener { stopSelf() }
        })

        val p = WindowManager.LayoutParams(
            dp(330), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        panel = box
        wm.addView(box, p)
    }

    override fun onDestroy() {
        if (::bubble.isInitialized) runCatching { wm.removeView(bubble) }
        panel?.let { runCatching { wm.removeView(it) } }
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

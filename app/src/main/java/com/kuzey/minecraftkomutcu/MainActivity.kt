package com.kuzey.minecraftkomutcu

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var input: EditText
    private lateinit var result: TextView
    private lateinit var apiKeyInput: EditText
    private lateinit var aiButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(18, 18, 18)
        window.navigationBarColor = Color.rgb(18, 18, 18)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(Color.rgb(18,18,18))
        }

        root.addView(TextView(this).apply {
            text = "Minecraft Komutçu AI"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = "Groq ile gerçek AI: komut, command block sistemi veya gerektiğinde Bedrock addon/script çözümü üretir."
            textSize = 16f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(8), 0, dp(16))
        })

        apiKeyInput = EditText(this).apply {
            hint = "Groq API anahtarı (gsk_...)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(40,40,40))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(GroqClient.getApiKey(this@MainActivity))
        }
        root.addView(apiKeyInput, LinearLayout.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "API anahtarını kaydet"
            setOnClickListener {
                val key = apiKeyInput.text.toString().trim()
                if (!key.startsWith("gsk_")) {
                    Toast.makeText(this@MainActivity, "Groq anahtarı gsk_ ile başlamalı.", Toast.LENGTH_SHORT).show()
                } else {
                    GroqClient.saveApiKey(this@MainActivity, key)
                    Toast.makeText(this@MainActivity, "API anahtarı cihazda kaydedildi.", Toast.LENGTH_SHORT).show()
                }
            }
        })

        input = EditText(this).apply {
            hint = "Örn: 5 zombi yan yana halay çeksin"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(40,40,40))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minLines = 2
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))

        aiButton = Button(this).apply {
            text = "AI ile üret"
            setOnClickListener { runAi() }
        }
        root.addView(aiButton)

        root.addView(Button(this).apply {
            text = "Hızlı yerel komut üret"
            setOnClickListener { result.text = CommandEngine.generate(input.text.toString()) }
        })

        result = TextView(this).apply {
            text = "Sonuç burada görünecek."
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(31,31,31))
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setTextIsSelectable(true)
        }
        root.addView(result, LinearLayout.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "Sonucu kopyala"
            setOnClickListener { copy(result.text.toString()) }
        })

        root.addView(Button(this).apply {
            text = "Yüzen AI penceresini aç"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    startService(Intent(this@MainActivity, OverlayService::class.java))
                    Toast.makeText(this@MainActivity, "Yüzen pencere açıldı", Toast.LENGTH_SHORT).show()
                }
            }
        })

        root.addView(TextView(this).apply {
            text = "Not: Groq internet ister ve hesabının rate limitleri geçerlidir. API anahtarı GitHub'a veya APK kaynak koduna gömülmez."
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(16), 0, 0)
        })

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun runAi() {
        val request = input.text.toString().trim()
        if (request.isBlank()) {
            Toast.makeText(this, "Önce ne istediğini yaz.", Toast.LENGTH_SHORT).show()
            return
        }
        val key = apiKeyInput.text.toString().trim()
        if (key.isNotBlank()) GroqClient.saveApiKey(this, key)
        if (GroqClient.getApiKey(this).isBlank()) {
            result.text = "Önce Groq API anahtarını gir ve kaydet."
            return
        }

        aiButton.isEnabled = false
        result.text = "AI düşünüyor..."
        thread {
            val answer = GroqClient.askMinecraft(this, request)
            runOnUiThread {
                result.text = answer
                aiButton.isEnabled = true
            }
        }
    }

    private fun copy(text: String) {
        val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("Minecraft AI sonucu", text))
        Toast.makeText(this, "Kopyalandı", Toast.LENGTH_SHORT).show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

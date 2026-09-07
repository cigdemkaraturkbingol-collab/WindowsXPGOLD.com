package com.kuzey.minecraftkomutcu

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var input: EditText
    private lateinit var result: TextView

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
            text = "Minecraft Komutçu"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = "Bedrock komutlarını üret, kopyala ve Minecraft üstünde yüzen pencereden kullan."
            textSize = 16f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(8), 0, dp(16))
        })

        input = EditText(this).apply {
            hint = "Örn: beni 100 blok yukarı ışınla"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(40,40,40))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "Komut üret"
            setOnClickListener { result.text = CommandEngine.generate(input.text.toString()) }
        })

        result = TextView(this).apply {
            text = "/time set day"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(31,31,31))
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setTextIsSelectable(true)
        }
        root.addView(result, LinearLayout.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "Komutu kopyala"
            setOnClickListener { copy(result.text.toString()) }
        })

        root.addView(Button(this).apply {
            text = "Yüzen pencereyi aç"
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
            text = "Örnekler: gece yap • gündüz yap • yağmuru kapat • elmas kılıç ver • yaratıcı moda geç"
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(16), 0, 0)
        })

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun copy(text: String) {
        val cb = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("Minecraft komutu", text))
        Toast.makeText(this, "Komut kopyalandı", Toast.LENGTH_SHORT).show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

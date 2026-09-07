package com.kuzey.minecraftkomutcu

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GroqClient {
    private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"

    fun saveApiKey(context: Context, key: String) {
        context.getSharedPreferences("groq", Context.MODE_PRIVATE)
            .edit().putString("api_key", key.trim()).apply()
    }

    fun getApiKey(context: Context): String =
        context.getSharedPreferences("groq", Context.MODE_PRIVATE)
            .getString("api_key", "").orEmpty()

    fun askMinecraft(context: Context, request: String): String {
        val key = getApiKey(context)
        if (key.isBlank()) return "API anahtarı yok. Ana ekrandan Groq API anahtarını kaydet."

        val systemPrompt = """
            Sen Minecraft Bedrock Edition için uzman bir komut ve addon yardımcısısın.
            Kullanıcı Türkçe yazar. İsteği gerçekten analiz et ve en uygulanabilir çözümü üret.
            Sadece tek vanilla komut yetiyorsa yalnız o komutu ver.
            Birden fazla komut gerekiyorsa sırayla numaralandır ve command block türünü belirt.
            Vanilla komutla mümkün değilse bunu kısa söyle ve ardından Bedrock behavior pack / script / animation_controller çözümü üret.
            Kullanıcı 'halay çekme animasyonu', 'dans eden zombiler', 'meteor yağmuru' gibi yaratıcı sistemler isteyebilir; sabit örnek döndürme, isteğe göre çözüm oluştur.
            Java Edition komutlarını Bedrock diye verme. Uydurma komut kullanma.
            Çıktı kısa ama uygulanabilir olsun. Gereksiz sohbet yapma.
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", MODEL)
            put("temperature", 0.25)
            put("max_completion_tokens", 1600)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", request))
            })
        }

        val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 45000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $key")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(text).getJSONObject("error").optString("message") }.getOrNull()
                return "Groq hatası ($code): ${msg ?: text.take(300)}"
            }
            val json = JSONObject(text)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } catch (e: Exception) {
            "Bağlantı hatası: ${e.message ?: e.javaClass.simpleName}"
        } finally {
            conn.disconnect()
        }
    }
}

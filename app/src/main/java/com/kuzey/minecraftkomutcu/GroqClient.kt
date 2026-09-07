package com.kuzey.minecraftkomutcu

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GroqClient {
    private const val CHAT_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODELS_ENDPOINT = "https://api.groq.com/openai/v1/models"
    private val preferredModels = listOf(
        "openai/gpt-oss-20b",
        "openai/gpt-oss-120b",
        "llama-3.1-8b-instant"
    )

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
        if (request.isBlank()) return "Ne yapmak istediğini yaz."

        val model = chooseAvailableModel(key)
            ?: return "Groq hesabında uygulamanın desteklediği uygun bir metin modeli bulunamadı. Groq model izinlerini kontrol et."

        val systemPrompt = """
            Sen Minecraft Bedrock Edition için uzman bir komut, command block ve addon yardımcısısın.
            Kullanıcı Türkçe yazar. İsteği gerçekten analiz et ve en uygulanabilir çözümü üret.
            Tek vanilla komut yetiyorsa yalnız doğru Bedrock komutunu ver.
            Birden fazla komut gerekiyorsa sırayla ver ve Repeat/Chain/Impulse ile Always Active/Needs Redstone ayarlarını belirt.
            Vanilla komutla mümkün olmayan özel animasyon, davranış veya sistemlerde bunu kısa belirt; sonra Bedrock behavior pack, Script API, animation veya animation_controller çözümünü uygulanabilir dosya/kod parçalarıyla üret.
            'Halay çeken zombiler', 'meteor yağmuru', 'oyuncu yaklaşınca açılan kapı' gibi yaratıcı isteklerde sabit örnek döndürme.
            Java Edition sözdizimini Bedrock diye verme ve var olmayan Minecraft komutları uydurma.
            Kullanıcının istediğini mümkün olan en kısa uygulanabilir biçimde cevapla.
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", model)
            put("temperature", 0.25)
            put("max_completion_tokens", 1800)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", request))
            })
        }

        return postChat(key, body)
    }

    private fun chooseAvailableModel(key: String): String? {
        val conn = URL(MODELS_ENDPOINT).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 12000
            conn.readTimeout = 20000
            conn.setRequestProperty("Authorization", "Bearer $key")
            conn.setRequestProperty("Accept", "application/json")
            val code = conn.responseCode
            if (code !in 200..299) return preferredModels.first()
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val data = JSONObject(text).optJSONArray("data") ?: return preferredModels.first()
            val ids = mutableSetOf<String>()
            for (i in 0 until data.length()) {
                val id = data.optJSONObject(i)?.optString("id").orEmpty()
                if (id.isNotBlank()) ids.add(id)
            }
            preferredModels.firstOrNull { it in ids }
        } catch (_: Exception) {
            preferredModels.first()
        } finally {
            conn.disconnect()
        }
    }

    private fun postChat(key: String, body: JSONObject): String {
        val conn = URL(CHAT_ENDPOINT).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $key")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(text).getJSONObject("error").optString("message") }.getOrNull()
                return "Groq hatası ($code): ${msg ?: text.take(300)}"
            }
            JSONObject(text).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message")
                .getString("content").trim()
        } catch (e: Exception) {
            "Bağlantı hatası: ${e.message ?: e.javaClass.simpleName}"
        } finally {
            conn.disconnect()
        }
    }
}

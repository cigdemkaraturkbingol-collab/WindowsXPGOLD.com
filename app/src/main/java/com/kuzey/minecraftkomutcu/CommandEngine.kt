package com.kuzey.minecraftkomutcu

import java.util.Locale

object CommandEngine {
    fun generate(raw: String): String {
        val t = raw.lowercase(Locale("tr", "TR")).trim()
        if (t.isBlank()) return "/help"

        return when {
            t.startsWith("/") -> t
            "gündüz" in t || "gunduz" in t || "sabah" in t -> "/time set day"
            "gece" in t -> "/time set night"
            "yağmuru kapat" in t || "yagmuru kapat" in t || "hava açık" in t || "hava acik" in t -> "/weather clear"
            "yağmur" in t || "yagmur" in t -> "/weather rain"
            "şimşek" in t || "simsek" in t || "fırtına" in t || "firtina" in t -> "/weather thunder"
            "yaratıcı" in t || "yaratici" in t || "creative" in t -> "/gamemode creative @s"
            "hayatta kalma" in t || "survival" in t -> "/gamemode survival @s"
            "macera" in t || "adventure" in t -> "/gamemode adventure @s"
            "seyirci" in t || "spectator" in t -> "/gamemode spectator @s"
            "elmas kılıç" in t || "elmas kilic" in t -> "/give @s diamond_sword 1"
            "elmas kazma" in t -> "/give @s diamond_pickaxe 1"
            "64 elmas" in t || ("elmas" in t && "64" in t) -> "/give @s diamond 64"
            "tüm zombileri" in t || "tum zombileri" in t -> "/kill @e[type=zombie]"
            "zombiyi öldür" in t || "zombiyi oldur" in t -> "/kill @e[type=zombie,c=1]"
            "ışınla" in t || "isinla" in t -> teleport(t)
            "doğma noktası" in t || "dogma noktasi" in t || "spawnpoint" in t -> "/spawnpoint @s"
            "gece görüşü" in t || "gece gorusu" in t -> "/effect @s night_vision 999999 1 true"
            "hız ver" in t || "hiz ver" in t -> "/effect @s speed 60 2 true"
            "efektleri temizle" in t -> "/effect @s clear"
            "öldür beni" in t || "oldur beni" in t -> "/kill @s"
            else -> "# Anlaşılamadı. Örnek: beni 100 blok yukarı ışınla"
        }
    }

    private fun teleport(t: String): String {
        val n = Regex("(\\d+)").find(t)?.value ?: "100"
        return when {
            "yukarı" in t || "yukari" in t -> "/tp @s ~ ~$n ~"
            "aşağı" in t || "asagi" in t -> "/tp @s ~ ~-$n ~"
            else -> "/tp @s ~ ~ ~"
        }
    }
}

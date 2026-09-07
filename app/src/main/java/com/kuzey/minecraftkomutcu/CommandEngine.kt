package com.kuzey.minecraftkomutcu

import java.util.Locale

object CommandEngine {

    private val items = linkedMapOf(
        "elmas kılıç" to "diamond_sword",
        "elmas kilic" to "diamond_sword",
        "netherit kılıç" to "netherite_sword",
        "netherit kilic" to "netherite_sword",
        "demir kılıç" to "iron_sword",
        "demir kilic" to "iron_sword",
        "taş kılıç" to "stone_sword",
        "tas kilic" to "stone_sword",
        "tahta kılıç" to "wooden_sword",
        "elmas kazma" to "diamond_pickaxe",
        "netherit kazma" to "netherite_pickaxe",
        "demir kazma" to "iron_pickaxe",
        "elmas balta" to "diamond_axe",
        "elmas kürek" to "diamond_shovel",
        "elmas kurek" to "diamond_shovel",
        "yay" to "bow",
        "ok" to "arrow",
        "kalkan" to "shield",
        "totem" to "totem_of_undying",
        "ölümsüzlük totemi" to "totem_of_undying",
        "elitra" to "elytra",
        "elytra" to "elytra",
        "elmas" to "diamond",
        "zümrüt" to "emerald",
        "zumrut" to "emerald",
        "altın külçesi" to "gold_ingot",
        "altin kulcesi" to "gold_ingot",
        "demir külçesi" to "iron_ingot",
        "demir kulcesi" to "iron_ingot",
        "ekmek" to "bread",
        "altın elma" to "golden_apple",
        "altin elma" to "golden_apple",
        "büyülü altın elma" to "enchanted_golden_apple",
        "buyulu altin elma" to "enchanted_golden_apple",
        "tnt" to "tnt",
        "komut bloğu" to "command_block",
        "komut blogu" to "command_block",
        "bariyer" to "barrier",
        "meşale" to "torch",
        "mesale" to "torch",
        "su kovası" to "water_bucket",
        "su kovasi" to "water_bucket",
        "lav kovası" to "lava_bucket",
        "lav kovasi" to "lava_bucket"
    )

    private val entities = linkedMapOf(
        "zombi" to "zombie",
        "iskelet" to "skeleton",
        "creeper" to "creeper",
        "örümcek" to "spider",
        "orumcek" to "spider",
        "enderman" to "enderman",
        "warden" to "warden",
        "with er" to "wither",
        "wither" to "wither",
        "ender dragon" to "ender_dragon",
        "ender ejderhası" to "ender_dragon",
        "ender ejderhasi" to "ender_dragon",
        "köylü" to "villager",
        "koylu" to "villager",
        "inek" to "cow",
        "koyun" to "sheep",
        "domuz" to "pig",
        "tavuk" to "chicken",
        "kurt" to "wolf",
        "arı" to "bee",
        "ari" to "bee",
        "iron golem" to "iron_golem",
        "demir golem" to "iron_golem"
    )

    private val effects = linkedMapOf(
        "gece görüşü" to "night_vision",
        "gece gorusu" to "night_vision",
        "hız" to "speed",
        "hiz" to "speed",
        "yavaşlık" to "slowness",
        "yavaslik" to "slowness",
        "güç" to "strength",
        "guc" to "strength",
        "zıplama" to "jump_boost",
        "ziplama" to "jump_boost",
        "yenilenme" to "regeneration",
        "direnç" to "resistance",
        "direnc" to "resistance",
        "görünmezlik" to "invisibility",
        "gorunmezlik" to "invisibility",
        "su altında nefes" to "water_breathing",
        "su altinda nefes" to "water_breathing",
        "ateş direnci" to "fire_resistance",
        "ates direnci" to "fire_resistance"
    )

    fun generate(raw: String): String {
        val t = normalize(raw)
        if (t.isBlank()) return "Bir şey yaz: örn. 32 elmas ver"
        if (t.startsWith("/")) return t

        parseTimeWeather(t)?.let { return it }
        parseGameMode(t)?.let { return it }
        parseGive(t)?.let { return it }
        parseSummon(t)?.let { return it }
        parseKill(t)?.let { return it }
        parseTeleport(t)?.let { return it }
        parseEffect(t)?.let { return it }
        parseEnchant(t)?.let { return it }
        parseSetBlock(t)?.let { return it }
        parseFill(t)?.let { return it }
        parseDifficulty(t)?.let { return it }
        parseGameRule(t)?.let { return it }

        return "Bunu henüz komuta çeviremedim: “${raw.trim()}”"
    }

    private fun parseTimeWeather(t: String): String? = when {
        containsAny(t, "gündüz", "gunduz", "sabah yap", "zamanı gündüz", "zamani gunduz") -> "/time set day"
        containsAny(t, "gece yap", "zamanı gece", "zamani gece") -> "/time set night"
        containsAny(t, "öğlen", "oglen") -> "/time set noon"
        containsAny(t, "gece yarısı", "gece yarisi") -> "/time set midnight"
        containsAny(t, "yağmuru kapat", "yagmuru kapat", "havayı aç", "havayi ac", "hava açık", "hava acik") -> "/weather clear"
        containsAny(t, "yağmur yağdır", "yagmur yagdir", "yağmur yap", "yagmur yap") -> "/weather rain"
        containsAny(t, "şimşek", "simsek", "fırtına", "firtina") -> "/weather thunder"
        else -> null
    }

    private fun parseGameMode(t: String): String? {
        val target = targetSelector(t)
        return when {
            containsAny(t, "yaratıcı", "yaratici", "creative") -> "/gamemode creative $target"
            containsAny(t, "hayatta kalma", "survival") -> "/gamemode survival $target"
            containsAny(t, "macera", "adventure") -> "/gamemode adventure $target"
            containsAny(t, "seyirci", "spectator") -> "/gamemode spectator $target"
            else -> null
        }
    }

    private fun parseGive(t: String): String? {
        if (!containsAny(t, "ver", "give", "almak istiyorum")) return null
        val item = findMapped(t, items) ?: return null
        val amount = firstNumber(t)?.coerceIn(1, 32767) ?: 1
        return "/give ${targetSelector(t)} $item $amount"
    }

    private fun parseSummon(t: String): String? {
        if (!containsAny(t, "çağır", "cagir", "spawnla", "doğur", "dogur", "summon")) return null
        val entity = findMapped(t, entities) ?: return null
        val coords = coordinates(t)
        return if (coords != null) "/summon $entity ${coords.joinToString(" ")}" else "/summon $entity ~ ~ ~"
    }

    private fun parseKill(t: String): String? {
        if (!containsAny(t, "öldür", "oldur", "kill")) return null
        if (containsAny(t, "beni", "kendimi")) return "/kill @s"
        if (containsAny(t, "herkesi", "tüm oyuncuları", "tum oyunculari")) return "/kill @a"
        val entity = findMapped(t, entities)
        if (entity != null) {
            val all = containsAny(t, "tüm", "tum", "hepsini", "bütün", "butun")
            return if (all) "/kill @e[type=$entity]" else "/kill @e[type=$entity,c=1]"
        }
        return null
    }

    private fun parseTeleport(t: String): String? {
        if (!containsAny(t, "ışınla", "isinla", "teleport", "tp ")) return null
        val target = targetSelector(t)
        val coords = coordinates(t)
        if (coords != null && coords.size >= 3) return "/tp $target ${coords.take(3).joinToString(" ")}"

        val n = firstNumber(t) ?: 100
        return when {
            containsAny(t, "yukarı", "yukari") -> "/tp $target ~ ~$n ~"
            containsAny(t, "aşağı", "asagi") -> "/tp $target ~ ~-$n ~"
            containsAny(t, "ileri") -> "/tp $target ^ ^ ^$n"
            containsAny(t, "geri") -> "/tp $target ^ ^ ^-${n}"
            else -> null
        }
    }

    private fun parseEffect(t: String): String? {
        if (containsAny(t, "efektleri temizle", "efekti temizle", "efektleri sil")) return "/effect ${targetSelector(t)} clear"
        if (!containsAny(t, "efekt", "ver", "olsun")) return null
        val effect = findMapped(t, effects) ?: return null
        val nums = numbers(t)
        val seconds = nums.getOrNull(0)?.coerceIn(1, 1_000_000) ?: 60
        val levelHuman = nums.getOrNull(1)?.coerceIn(1, 255) ?: 1
        val amplifier = levelHuman - 1
        return "/effect ${targetSelector(t)} $effect $seconds $amplifier true"
    }

    private fun parseEnchant(t: String): String? {
        if (!containsAny(t, "büyü", "buyu", "enchant")) return null
        val enchant = when {
            containsAny(t, "keskinlik", "sharpness") -> "sharpness"
            containsAny(t, "verimlilik", "efficiency") -> "efficiency"
            containsAny(t, "kırılmazlık", "kirilmazlik", "unbreaking") -> "unbreaking"
            containsAny(t, "servet", "fortune") -> "fortune"
            containsAny(t, "ipeksi dokunuş", "ipeksi dokunus", "silk touch") -> "silk_touch"
            containsAny(t, "koruma", "protection") -> "protection"
            containsAny(t, "tamir", "mending") -> "mending"
            else -> return null
        }
        val level = firstNumber(t)?.coerceIn(1, 255) ?: 1
        return "/enchant ${targetSelector(t)} $enchant $level"
    }

    private fun parseSetBlock(t: String): String? {
        if (!containsAny(t, "blok koy", "bloğu koy", "blogu koy", "setblock")) return null
        val block = findMapped(t, items) ?: blockNameFromText(t) ?: return null
        val coords = coordinates(t)
        return if (coords != null) "/setblock ${coords.take(3).joinToString(" ")} $block" else "/setblock ~ ~-1 ~ $block"
    }

    private fun parseFill(t: String): String? {
        if (!containsAny(t, "doldur", "fill")) return null
        val nums = numbers(t)
        if (nums.size < 6) return null
        val block = findMapped(t, items) ?: blockNameFromText(t) ?: "stone"
        return "/fill ${nums.take(6).joinToString(" ")} $block"
    }

    private fun parseDifficulty(t: String): String? = when {
        containsAny(t, "zorluk barışçıl", "zorluk bariscil", "peaceful") -> "/difficulty peaceful"
        containsAny(t, "zorluk kolay", "easy") -> "/difficulty easy"
        containsAny(t, "zorluk normal", "normal zorluk") -> "/difficulty normal"
        containsAny(t, "zorluk zor", "hard") -> "/difficulty hard"
        else -> null
    }

    private fun parseGameRule(t: String): String? = when {
        containsAny(t, "eşya düşmesin", "esya dusmesin", "keep inventory aç", "keepinventory aç") -> "/gamerule keepinventory true"
        containsAny(t, "eşya düşsün", "esya dussun", "keep inventory kapat", "keepinventory kapat") -> "/gamerule keepinventory false"
        containsAny(t, "mob doğmasın", "mob dogmasin") -> "/gamerule domobspawning false"
        containsAny(t, "mob doğsun", "mob dogsun") -> "/gamerule domobspawning true"
        containsAny(t, "gün döngüsünü durdur", "gun dongusunu durdur") -> "/gamerule dodaylightcycle false"
        containsAny(t, "gün döngüsünü aç", "gun dongusunu ac") -> "/gamerule dodaylightcycle true"
        else -> null
    }

    private fun targetSelector(t: String): String = when {
        containsAny(t, "herkese", "tüm oyunculara", "tum oyunculara") -> "@a"
        containsAny(t, "en yakın oyuncuya", "en yakin oyuncuya") -> "@p"
        else -> "@s"
    }

    private fun coordinates(t: String): List<String>? {
        val coordinatePattern = Regex("(?<![a-zA-Z0-9_~^.-])([~^]?-?\\d+(?:\\.\\d+)?|[~^])(?![a-zA-Z0-9_])")
        val found = coordinatePattern.findAll(t).map { it.groupValues[1] }.toList()
        return if (found.size >= 3) found else null
    }

    private fun numbers(t: String): List<Int> = Regex("-?\\d+").findAll(t).mapNotNull { it.value.toIntOrNull() }.toList()
    private fun firstNumber(t: String): Int? = numbers(t).firstOrNull()

    private fun <T> findMapped(t: String, map: Map<String, T>): T? =
        map.entries.sortedByDescending { it.key.length }.firstOrNull { it.key in t }?.value

    private fun blockNameFromText(t: String): String? = when {
        "taş" in t || "tas" in t -> "stone"
        "toprak" in t -> "dirt"
        "çimen" in t || "cimen" in t -> "grass_block"
        "cam" in t -> "glass"
        "obsidyen" in t -> "obsidian"
        "bedrock" in t -> "bedrock"
        "kum" in t -> "sand"
        "su" in t -> "water"
        "lav" in t -> "lava"
        else -> null
    }

    private fun normalize(s: String): String = s.lowercase(Locale("tr", "TR")).trim()
    private fun containsAny(t: String, vararg words: String): Boolean = words.any { it in t }
}

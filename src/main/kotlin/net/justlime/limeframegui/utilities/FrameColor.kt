package net.justlime.limeframegui.utilities

import me.clip.placeholderapi.PlaceholderAPI
import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.enums.ColorType
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.regex.Pattern

object FrameColor {
    var colorType: ColorType = ColorType.LEGACY

    private val legacy = LegacyComponentSerializer.legacySection()
    private val hexPattern = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})")
    private val mini by lazy { MiniMessage.miniMessage() }
    private val isPlaceholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    /**
     * Apply color formatting based on the current ColorType.
     * Always returns a String — suitable for GUI APIs (1.8+ safe).
     */
    fun applyColor(text: String, player: Player? = null, offlinePlayer: OfflinePlayer? = null, smallCaps: Boolean? = false, customPlaceholders: Map<String, String>? = null): String {
        var newText = text

        val playerName = player?.name ?: offlinePlayer?.name
        newText = newText.customPlaceholder(playerName, customPlaceholders)

        if (isPlaceholderAPIEnabled) {
            newText = when {
                player != null -> PlaceholderAPI.setPlaceholders(player, newText)
                offlinePlayer != null -> PlaceholderAPI.setPlaceholders(offlinePlayer, newText)
                else -> newText
            }
        }

        newText = newText.toSmallCaps(smallCaps)

        return when (colorType) {
            ColorType.LEGACY -> ChatColor.translateAlternateColorCodes('&', newText)
            ColorType.MINI_MESSAGE -> toLegacyMini(newText)
        }
    }

    fun applyColor(text: List<String>,player: Player? = null ,offlinePlayer: OfflinePlayer? = null,smallCaps: Boolean? = false, customPlaceholders: Map<String, String>? = null): List<String> {
        return text.map { applyColor(it,player ,offlinePlayer,smallCaps, customPlaceholders) }
    }

    private fun String.replaceLegacyToMini(): String {
        return this.replace("§0", "<black>").replace("§1", "<dark_blue>").replace("§2", "<dark_green>").replace("§3", "<dark_aqua>").replace("§4", "<dark_red>").replace("§5", "<dark_purple>").replace("§6", "<gold>").replace("§7", "<gray>")
            .replace("§8", "<dark_gray>").replace("§9", "<blue>").replace("§a", "<green>").replace("§b", "<aqua>").replace("§c", "<red>").replace("§d", "<light_purple>").replace("§e", "<yellow>").replace("§f", "<white>").replace("§l", "<bold>")
            .replace("§m", "<strikethrough>").replace("§n", "<underlined>").replace("§o", "<italic>").replace("§r", "<reset>")
    }

    private fun String.customPlaceholder(name: String?, customPlaceholders: Map<String, String>?): String {
        var result = this
        if (name != null) {
            result = result.replace("{player}", name)
        }
        customPlaceholders?.forEach { (key, value) -> result = result.replace(key, value) }
        return result
    }

    private fun String.toSmallCaps(smallCaps: Boolean?): String {
        if (smallCaps != true) return this

        val result = StringBuilder()
        var inMiniTag = false
        var skipNext = false

        for (i in this.indices) {
            val c = this[i].toString()

            // Handle MiniMessage tags: <...>
            if (c == "<") {
                inMiniTag = true
            }

            if (inMiniTag) {
                result.append(c)
                if (c == ">") inMiniTag = false
                continue
            }

            // Handle legacy color codes: &a or §a
            if (skipNext) {
                result.append(c)
                skipNext = false
                continue
            }
            if (c == "&" || c == "§") {
                result.append(c)
                skipNext = true
                continue
            }

            // Apply small caps
            val mapped = LimeFrameAPI.keys.smallCapsFont[c.lowercase()] ?: c
            result.append(mapped)
        }

        return result.toString()
    }

    private fun toLegacyMini(text: String): String {
        return try {
            legacy.serialize(mini.deserialize(text))
        } catch (e: Exception) {
            println(e.message)
            ChatColor.translateAlternateColorCodes('&', text)
        }
    }
}
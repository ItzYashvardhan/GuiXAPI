package net.justlime.limeframegui.manager

import net.justlime.limeframegui.enums.ColorType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import java.util.regex.Pattern

object ColorFormat {
    var colorType: ColorType = ColorType.LEGACY

    private val legacy = LegacyComponentSerializer.legacySection()
    private val hexPattern = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})")
    private val mini by lazy { MiniMessage.miniMessage() }

    /**
     * Apply color formatting based on the current ColorType.
     * Always returns a String — suitable for GUI APIs (1.8+ safe).
     */
    fun applyColor(text: String): String {
        return when (colorType) {
            ColorType.LEGACY -> ChatColor.translateAlternateColorCodes('&', text)
            ColorType.HEX -> translateHexToLegacy(text)
            ColorType.MINI_MESSAGE -> toLegacyMini(text)
        }
    }

    fun applyColor(text: List<String>): List<String>{
        return text.map { applyColor(it) }
    }

    /**
     * Only used if the developer explicitly wants a Component for modern APIs.
     */
    fun toComponent(text: String): Component {
        return mini.deserialize(text)
    }

    private fun translateHexToLegacy(input: String): String {
        val matcher = hexPattern.matcher(input)
        val result = StringBuffer()

        while (matcher.find()) {
            val hex = matcher.group(1)
            val chatColor = try {
                net.md_5.bungee.api.ChatColor.of("#$hex").toString()
            } catch (e: Exception) {
                "&r"
            }
            matcher.appendReplacement(result, chatColor)
        }

        matcher.appendTail(result)
        return ChatColor.translateAlternateColorCodes('&', result.toString())
    }

    private fun toLegacyMini(text: String): String {
        return try {
            legacy.serialize(mini.deserialize(text))
        } catch (_: Exception) {
            ChatColor.translateAlternateColorCodes('&', text)
        }
    }
}



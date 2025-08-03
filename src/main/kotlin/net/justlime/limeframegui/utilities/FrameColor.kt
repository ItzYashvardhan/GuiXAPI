package net.justlime.limeframegui.utilities

import net.justlime.limeframegui.enums.ColorType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import java.util.regex.Pattern

object FrameColor {
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
            ColorType.MINI_MESSAGE -> {
                val legacyText = ChatColor.translateAlternateColorCodes('&', text)
                val miniText = text.replaceLegacyToMini()
                toLegacyMini(miniText)
            }
        }
    }

    private fun String.replaceLegacyToMini(): String {
        return this.replace("§0", "<black>").replace("§1", "<dark_blue>").replace("§2", "<dark_green>").replace("§3", "<dark_aqua>").replace("§4", "<dark_red>").replace("§5", "<dark_purple>").replace("§6", "<gold>").replace("§7", "<gray>")
            .replace("§8", "<dark_gray>").replace("§9", "<blue>").replace("§a", "<green>").replace("§b", "<aqua>").replace("§c", "<red>").replace("§d", "<light_purple>").replace("§e", "<yellow>").replace("§f", "<white>").replace("§l", "<bold>")
            .replace("§m", "<strikethrough>").replace("§n", "<underlined>").replace("§o", "<italic>").replace("§r", "<reset>")
    }

    fun applyColor(text: List<String>): List<String> {
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
        } catch (e: Exception) {
            println(e.message)
            ChatColor.translateAlternateColorCodes('&', text)
        }
    }
}
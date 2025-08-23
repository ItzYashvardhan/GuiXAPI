package net.justlime.limeframegui.color

import me.clip.placeholderapi.PlaceholderAPI
import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.enums.CapsState
import net.justlime.limeframegui.enums.ColorType
import net.justlime.limeframegui.utilities.VersionHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object FrameColor {
    var miniMessage: IMiniMessage? = null
    private lateinit var colorType: ColorType

    fun setColorType(color: ColorType){
        colorType = color
    }

    fun initMiniMessage() {
        val mini = KyoriMiniMessage()
        try {
            miniMessage = mini
            println("Initialized Mini Message")
        } catch (e: Exception) {
            Bukkit.getLogger().warning(e.message)
        }
    }

    private val isPlaceholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    /**
     * - Apply color formatting based on the current ColorType.
     * Always returns a String.
     * - Priority to [OfflinePlayer] if both player type given
     */
    fun applyColor(text: String, player: Player? = null, offlinePlayer: OfflinePlayer? = null, smallCaps: Boolean? = false, customPlaceholders: Map<String, String>? = null): String {
        var newText = text

        val playerName = player?.name ?: offlinePlayer?.name
        newText = newText.customPlaceholder(playerName, customPlaceholders)

        if (isPlaceholderAPIEnabled) {
            newText = when {
                offlinePlayer != null -> PlaceholderAPI.setPlaceholders(offlinePlayer, newText)
                player != null -> PlaceholderAPI.setPlaceholders(player, newText)
                else -> newText
            }
        }


       val text = when (colorType) {
            ColorType.LEGACY -> ChatColor.translateAlternateColorCodes('&', newText)
            ColorType.MINI_MESSAGE -> {
                newText = newText.replaceLegacyToMini()
                try {
                    miniMessage?.legacyToMini(newText) ?: newText
                } catch (e: Exception) {
                    println(e.message)
                    newText
                }
            }
        }

        return text.toSmallCaps(player,smallCaps)
    }

    fun applyColor(text: List<String>, player: Player? = null, offlinePlayer: OfflinePlayer? = null, smallCaps: Boolean? = false, customPlaceholders: Map<String, String>? = null): List<String> {
        return text.map { applyColor(it, player, offlinePlayer, smallCaps, customPlaceholders) }
    }

    private fun String.replaceLegacyToMini(): String {
        return this.replace("§0", "<black>").replace("§1", "<dark_blue>").replace("§2", "<dark_green>").replace("§3", "<dark_aqua>").replace("§4", "<dark_red>").replace("§5", "<dark_purple>").replace("§6", "<gold>").replace("§7", "<gray>")
            .replace("§8", "<dark_gray>").replace("§9", "<blue>").replace("§a", "<green>").replace("§b", "<aqua>").replace("§c", "<red>").replace("§d", "<light_purple>").replace("§e", "<yellow>").replace("§f", "<white>").replace("§l", "<bold>")
            .replace("§m", "<strikethrough>").replace("§n", "<underlined>").replace("§o", "<italic>").replace("§r", "<reset>").replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
            .replace("&4", "<dark_red>").replace("&5", "<dark_purple>").replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>").replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>").replace("&c", "<red>")
            .replace("&d", "<light_purple>").replace("&e", "<yellow>").replace("&f", "<white>").replace("&l", "<bold>").replace("&m", "<strikethrough>").replace("&n", "<underlined>").replace("&o", "<italic>").replace("&r", "<reset>")

    }

    private fun String.customPlaceholder(name: String?, customPlaceholders: Map<String, String>?): String {
        var result = this
        if (name != null) {
            result = result.replace("{player}", name)
        }
        customPlaceholders?.forEach { (key, value) -> result = result.replace(key, value) }
        return result
    }


    /**
     * Converts a string to small caps with advanced tag support.
     *
     * - Automatically selects the best font map for the viewer's version.
     * - Falls back to the server version if the viewer is null.
     * - Obeys <caps> and <no-caps> tags to override the default behavior.
     */
    fun String.toSmallCaps(viewer: Player?, useSmallCaps: Boolean?): String {
        val fontMaps: Map<String, Map<String, String>> = LimeFrameAPI.keys.smallCapsFont
        if (fontMaps.isEmpty() && useSmallCaps != true) return this

        val bestVersionKey = fontMaps.keys
            .sortedWith { v1, v2 -> VersionHandler.compareVersions(VersionHandler.parseVersion(v2), VersionHandler.parseVersion(v1)) }
            .firstOrNull { versionKey ->
                if (viewer != null) VersionHandler.isVersionSupported(viewer, versionKey)
                else {
                    val serverVersion = VersionHandler.parseVersion(VersionHandler.getNativeServerVersion())
                    val keyVersion = VersionHandler.parseVersion(versionKey)
                    VersionHandler.compareVersions(serverVersion, keyVersion) >= 0
                }
            }

        val selectedFontMap = fontMaps[bestVersionKey]

        val result = StringBuilder()
        var i = 0
        var currentCapsState = CapsState.DEFAULT

        while (i < this.length) {
            val char = this[i]
            if (char == '<') {
                val closingIndex = this.indexOf('>', startIndex = i)
                if (closingIndex != -1) {
                    val tag = this.substring(i + 1, closingIndex)
                    when (tag.lowercase()) {
                        "caps" -> currentCapsState = CapsState.FORCE_ON
                        "/caps" -> currentCapsState = CapsState.DEFAULT
                        "no-caps" -> currentCapsState = CapsState.FORCE_OFF
                        "/no-caps" -> currentCapsState = CapsState.DEFAULT
                        else -> result.append(this, i, closingIndex + 1)
                    }
                    i = closingIndex + 1
                    continue
                }
            }

            // Determine if the character should be converted
            val shouldConvert = when (currentCapsState) {
                CapsState.FORCE_ON -> true
                CapsState.FORCE_OFF -> false
                CapsState.DEFAULT -> useSmallCaps == true
            }

            // Append character, converting if necessary
            if (shouldConvert && selectedFontMap != null) {
                when (char) {
                    '&', '§' -> {
                        result.append(char)
                        if (i + 1 < this.length) {
                            result.append(this[i + 1])
                            i++
                        }
                    }
                    else -> result.append(selectedFontMap[char.toString().lowercase()] ?: char)
                }
            } else {
                result.append(char)
            }
            i++
        }

        return result.toString()
    }
}
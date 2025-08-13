package net.justlime.limeframegui.color

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.ChatColor

class KyoriMiniMessage : IMiniMessage {
    private val legacy = LegacyComponentSerializer.builder().hexCharacter('§').hexColors().useUnusualXRepeatedCharacterHexFormat().build()
    private val mini = MiniMessage.miniMessage()

    override fun legacyToMini(text: String): String {
        return try {
            val component: Component = mini.deserialize(text)
            val legacy = legacy.serialize(component.compact())
            legacy
        } catch (e: Exception) {
            println(e.message)
            ChatColor.translateAlternateColorCodes('&', text)
        }
    }
}


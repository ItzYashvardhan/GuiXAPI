package net.justlime.limeframegui.color

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class KyoriMiniMessage : IMiniMessage {
    override val legacySerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build()
    private val mini = MiniMessage.miniMessage()

    override fun legacyToMini(text: String): String {
        val component: Component = legacySerializer.deserialize(text)
        return mini.serialize(component)
    }
}


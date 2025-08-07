package net.justlime.limeframegui.models

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

data class GUISetting(
    var rows: Int,
    var title: String,
    var type: InventoryType = InventoryType.CHEST,
    var placeholderPlayer: Player? = null,
)
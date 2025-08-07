package net.justlime.limeframegui.models

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

data class GUISetting(
    var rows: Int,
    var title: String,
    var placeholderPlayer: Player? = null,
    var type: InventoryType = InventoryType.CHEST,
)
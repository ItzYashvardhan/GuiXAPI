package net.justlime.limeframegui.models

import org.bukkit.event.inventory.InventoryType

data class GUISetting(
    var rows: Int,
    var title: String,
    var type: InventoryType = InventoryType.CHEST
)
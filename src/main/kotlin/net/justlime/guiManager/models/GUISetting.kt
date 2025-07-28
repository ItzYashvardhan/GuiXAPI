package net.justlime.guiManager.models

import org.bukkit.event.inventory.InventoryType

data class GUISetting(
    var title: String,
    var rows: Int,
    var type: InventoryType = InventoryType.CHEST
)
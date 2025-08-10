package net.justlime.limeframegui.models

import net.justlime.limeframegui.api.LimeFrameAPI
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

data class GUISetting(
    var rows: Int,
    var title: String,
    var placeholderPlayer: Player? = null,
    var placeholderOfflinePlayer: Player? = null,
    var smallCaps: Boolean = LimeFrameAPI.keys.smallCaps,
    var type: InventoryType = InventoryType.CHEST,
)
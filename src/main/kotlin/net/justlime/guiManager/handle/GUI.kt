package net.justlime.guiManager.handle

import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

interface GUI : InventoryHolder {
    // The inventory associated with this GUI
    override fun getInventory(): Inventory

    // --- Methods for the LISTENER to call ---
    fun onEvent(event: InventoryClickEvent)
    fun onEvent(event: InventoryOpenEvent)
    fun onEvent(event: InventoryCloseEvent)

    fun load(inventory: Inventory): GUI

}
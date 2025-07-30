package net.justlime.guiManager.handle

import net.justlime.guiManager.models.GuiItem
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

interface GUIPage {

    fun getInventory(): Inventory

    fun addItem(item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)? = null): GUIPage
    fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)? = null)
    fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)? = null): GUIPage

    fun onClick(handler: (InventoryClickEvent) -> Unit): Boolean
    fun onOpen(handler: (InventoryOpenEvent) -> Unit): Boolean
    fun onClose(handler: (InventoryCloseEvent) -> Unit): Boolean

    fun handleClick(event: InventoryClickEvent)
    fun handleOpen(event: InventoryOpenEvent)
    fun handleClose(event: InventoryCloseEvent)

    fun inventoryTrim()
}

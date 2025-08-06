package net.justlime.limeframegui.handle

import net.justlime.limeframegui.impl.Navigation
import net.justlime.limeframegui.impl.NestedPageBuilder
import net.justlime.limeframegui.models.GuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

interface GUIPage {
    val currentPage: Int
    var inventory: Inventory

    fun getItems(): Map<Int, GuiItem>
    fun addItem(item: GuiItem, onClick: ((InventoryClickEvent) -> Unit) = {}): Int
    fun addItem(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} })
    fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit) = {}): Int
    fun removeItem(item: GuiItem): GUIPage
    fun removeItem(slot: Int): GUIPage
    fun onOpen(handler: (InventoryOpenEvent) -> Unit)
    fun onClose(handler: (InventoryCloseEvent) -> Unit)
    fun onClick(handler: (InventoryClickEvent) -> Unit)
    fun addPage(id: Int, rows: Int, title: String, block: GUIPage.() -> Unit)
    fun addPage(rows: Int, title: String, block: GUIPage.() -> Unit)
    fun nav(block: Navigation.() -> Unit)
    fun openPage(player: Player,id: Int)

}

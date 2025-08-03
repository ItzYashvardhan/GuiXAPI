package net.justlime.guiManager.impl

import net.justlime.guiManager.handle.GUIPage
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.utilities.toGuiItem
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

class GuiPageImpl(override val currentPage: Int, private val handler: GuiHandler, setting: GUISetting) : GUIPage {
    private var inventory = Bukkit.createInventory(handler.inventory.holder, setting.rows * 9, setting.title.replace("{page}", currentPage.toString()))

    override fun getInventory(): Inventory {
        return inventory
    }

    override fun getItems(): Map<Int, GuiItem> {
        val items = mutableMapOf<Int, GuiItem>()
        for (i in 0 until inventory.contents.size) {
            val guiItem = inventory.getItem(i)?.toGuiItem() ?: continue
            items[i] = guiItem
        }
        return items
    }

    override fun addItem(item: GuiItem, onClick: (InventoryClickEvent) -> Unit): Int {
        val slot = inventory.firstEmpty()
        if (slot != -1) {
            inventory.setItem(slot, item.toItemStack())
            handler.itemClickHandler.computeIfAbsent(currentPage) { mutableMapOf() }[slot] = onClick

        }
        return slot
    }

    override fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)) {
        items.forEach { guiItem ->
            val slot = inventory.firstEmpty()
            if (slot != -1) {
                inventory.setItem(slot, guiItem.toItemStack())
                handler.itemClickHandler[currentPage] = mutableMapOf(slot to { event -> onClick.invoke(guiItem, event) })
            }
        }
    }

    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)): Int {
        if (index < inventory.size) {
            inventory.setItem(index, item.toItemStack())
            handler.itemClickHandler.computeIfAbsent(currentPage) { mutableMapOf() }[index] = onClick

            return index
        }
        return -1

    }

    override fun removeItem(item: GuiItem): GUIPage {
        val slot = inventory.first(item.toItemStack())
        if (slot != -1) {
            inventory.setItem(slot, null)
            handler.itemClickHandler[currentPage]?.remove(slot)
        }
        return this
    }

    override fun removeItem(slot: Int): GUIPage {
        if (slot >= -1 && slot < inventory.size) {
            inventory.setItem(slot, null)
            handler.itemClickHandler[currentPage]?.remove(slot)
        }
        return this
    }

    override fun onOpen(handler: (InventoryOpenEvent) -> Unit) {
        this.handler.pageOpenHandlers[currentPage] = handler

    }

    override fun onClose(handler: (InventoryCloseEvent) -> Unit) {
        this.handler.pageCloseHandlers[currentPage] = handler
    }

    override fun onClick(handler: (InventoryClickEvent) -> Unit) {
        this.handler.pageClickHandlers[currentPage] = handler
    }

}

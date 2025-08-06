package net.justlime.limeframegui.impl

import net.justlime.limeframegui.handle.GUIEventHandler
import net.justlime.limeframegui.handle.GUIPage
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class GuiPageImpl(override val currentPage: Int, private val handler: GUIEventHandler, setting: GUISetting,private val builder: ChestGUIBuilder) : GUIPage {
    override var inventory = handler.createPageInventory(currentPage, setting)

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

    override fun addItem(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)) {
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

    override fun addPage(id: Int, rows: Int, title: String, block: GUIPage.() -> Unit) {
        builder.addPage(id, rows, title, block)
    }

    override fun addPage(rows: Int, title: String, block: GUIPage.() -> Unit) {
        builder.addPage(rows, title, block)
    }


    override fun nav(block: Navigation.() -> Unit) {
        builder.nav(block)
    }

    override fun openPage(player: Player, id: Int) {
       handler.open(player,id)
    }

}

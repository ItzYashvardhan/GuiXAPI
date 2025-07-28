package net.justlime.guiManager.handle

import net.justlime.guiManager.impl.GuiPageImpl
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

class GuiImpl(setting: GUISetting) : GUI {
    private val pages = mutableMapOf<Int, GUIPage>()
    private val inventory: Inventory = Bukkit.createInventory(this, setting.rows * 9, setting.title)

    private val clickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()
    private val openHandlers = mutableMapOf<Int, (InventoryOpenEvent) -> Unit>()
    private val closeHandlers = mutableMapOf<Int, (InventoryCloseEvent) -> Unit>()
    private val itemClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    override fun getInventory(): Inventory = inventory
    override fun createPage(setting: GUISetting): GUIPage {
        return GuiPageImpl(this, setting.title, setting.rows, inventory.contents)
    }

    override fun get(index: Int): GUIPage? {
        return pages[index]
    }

    override fun set(index: Int, setting: GUISetting): GUIPage {
        pages[index] = GuiPageImpl(this, setting.title, setting.rows, inventory.contents)
        return pages[index]!!
    }

    override fun set(index: Int, guiPage: GUIPage): GUIPage {
        pages[index] = guiPage
        return pages[index]!!
    }

    override fun minusAssign(index: Int) {
        pages.remove(index)
    }

    override fun openPage(player: Player, index: Int) {
        if (index <= pages.size && pages[index] != null) {
            pages[index]?.let { player.openInventory(it.getInventory()) }
        } else {
            player.sendMessage("Page $index does not exist.")
        }
    }

    override fun addItem(item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)?): GUI {

        //Add Item to all GUI Pages
        val slot = inventory.firstEmpty()
        if (slot != -1) {
            inventory.setItem(slot, item.toItemStack())
            if (onClick != null) {
                itemClickHandlers[slot] = onClick
            }
        }
        pages.forEach {
            val pageItem = it.value.getInventory().getItem(slot)
            if (pageItem == null) it.value.addItem(item, onClick)
        }
        return this
    }

    override fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)?): GUI {
        items.forEach { guiItem ->
            addItem(guiItem) { event ->
                onClick?.invoke(guiItem, event)
            }
        }
        return this
    }

    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)?): GUI {
        inventory.setItem(index, item.toItemStack())
        if (onClick != null) {
            itemClickHandlers[index] = onClick
        } else {
            itemClickHandlers.remove(index)
        }

        //Add Item to all GUI Pages
        plugin.logger.info("Setting Item1")
        pages.forEach {
            if (it.value.getInventory().size <= index) {
                val pageItem = it.value.getInventory().getItem(index)
                if (pageItem == null) {
                    it.value.setItem(index, item, onClick)
                }
            }
        }
        plugin.logger.info("Setting Item2")
        return this
    }

    override fun removeItem(item: GuiItem): GUI {
        inventory.removeItem(item.toItemStack())
        return this
    }

    override fun onClick(handler: (InventoryClickEvent) -> Unit) {
        clickHandlers[-1] = handler
    }

    override fun onOpen(handler: (InventoryOpenEvent) -> Unit) {
        openHandlers[-1] = handler

    }

    override fun onClose(handler: (InventoryCloseEvent) -> Unit) {
        closeHandlers[-1] = handler
    }

    override fun onEvent(event: InventoryClickEvent) {
        val slot = event.slot
        pages.forEach { (_, page) ->
            if (event.inventory == page.getInventory()) {
                page.handleClick(event)
            }
        }

        itemClickHandlers[slot]?.invoke(event)

        clickHandlers.forEach { it.value.invoke(event) }
    }

    override fun onEvent(event: InventoryOpenEvent) {
        openHandlers.forEach { it.value.invoke(event) }

    }

    override fun onEvent(event: InventoryCloseEvent) {
        closeHandlers.forEach { it.value.invoke(event) }
    }

}
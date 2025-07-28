package net.justlime.guiManager.impl

import net.justlime.guiManager.handle.GUIPage
import net.justlime.guiManager.handle.GuiImpl
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GuiPageImpl(mainGui: GuiImpl, title: String, rows: Int, contents: Array<ItemStack>) : GUIPage {
    private var inventory = Bukkit.createInventory(mainGui, rows * 9, title)
    private val openHandlers = mutableListOf<(InventoryOpenEvent) -> Unit>()
    private val clickHandlers = mutableListOf<(InventoryClickEvent) -> Unit>()
    private val closeHandlers = mutableListOf<(InventoryCloseEvent) -> Unit>()
    private val itemClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    override fun getInventory(): Inventory {
        return inventory
    }

    init {
        val oldContents = contents.toList()
        val newContents = oldContents.toMutableList()
        val oldToNewMap = mutableMapOf<Int, Int>()

        val maxSlots = rows * 9
        var rowsToRemove = (oldContents.size / 9) - rows

        if (rowsToRemove > 0) {
            var removedCount = 0
            var row = 1 // start from second row (keep first and last)
            while (removedCount < rowsToRemove && row < (newContents.size / 9) - 1) {
                val start = row * 9
                val end = start + 9
                val isEmptyRow = (start until end).all { newContents[it] == null || newContents[it].type == Material.AIR }

                if (isEmptyRow) {
                    repeat(9) { newContents.removeAt(start) }
                    removedCount++
                } else {
                    row++
                }
            }
        }

        // Now trim if still too large (cut extra from top but keep last row)
        if (newContents.size > maxSlots) {
            while (newContents.size > maxSlots) {
                newContents.removeAt(0)
            }
        }

        // Build mapping (old slot -> new slot)
        var newIndex = 0
        for ((oldIndex, item) in oldContents.withIndex()) {
            if (newIndex < newContents.size && newContents[newIndex] == item) {
                oldToNewMap[oldIndex] = newIndex
                newIndex++
            }
        }

        // Fill inventory
        for (i in newContents.indices) {
            if (i < inventory.size) {
                inventory.setItem(i, newContents[i])
            }
        }

        // Remap handlers
        oldToNewMap.forEach { (oldSlot, newSlot) ->
            mainGui.itemClickHandlers[oldSlot]?.let { handler ->
                itemClickHandlers[newSlot] = handler
            }
        }
    }




    override fun addItem(item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)?): GUIPage {

        val slot = inventory.firstEmpty()
        if (slot != -1) {
            inventory.setItem(slot, item.toItemStack())
            if (onClick != null) {
                itemClickHandlers[slot] = onClick
            }
        }
        return this
    }

    override fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)?) {
        items.forEach { guiItem ->
            val slot = inventory.firstEmpty()
            if (slot != -1) {
                inventory.setItem(slot, guiItem.toItemStack())
                if (onClick != null) {
                    itemClickHandlers[slot] = { event -> onClick.invoke(guiItem, event) }
                }
            }
        }
    }

    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)?): GUIPage {
        if (index >= inventory.size) {
            inventory.setItem(index, item.toItemStack())

            if (onClick != null) {
                itemClickHandlers[index] = onClick
            } else {
                itemClickHandlers.remove(index)
            }
        }
        return this
    }

    override fun onClick(handler: (InventoryClickEvent) -> Unit) = clickHandlers.add(handler)
    override fun onOpen(handler: (InventoryOpenEvent) -> Unit) = openHandlers.add(handler)
    override fun onClose(handler: (InventoryCloseEvent) -> Unit) = closeHandlers.add(handler)

    override fun handleClick(event: InventoryClickEvent) {
        plugin.logger.info("Click event ${event.whoClicked}")
        val slot = event.slot
        itemClickHandlers[slot]?.invoke(event) // Call per-item handler if exists
        clickHandlers.forEach { it.invoke(event) } // Call global handlers
    }

    override fun handleOpen(event: InventoryOpenEvent) {
        openHandlers.forEach { it.invoke(event) }
    }

    override fun handleClose(event: InventoryCloseEvent) {
        closeHandlers.forEach { it.invoke(event) }
    }
}

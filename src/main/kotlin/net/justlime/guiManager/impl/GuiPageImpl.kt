package net.justlime.guiManager.impl

import net.justlime.guiManager.handle.GUIPage
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.utilities.toGuiItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class GuiPageImpl(holder: InventoryHolder, setting: GUISetting) : GUIPage {
    private var inventory = Bukkit.createInventory(holder, setting.rows * 9, setting.title)
    private val itemClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()
    private val openHandlers = mutableListOf<(InventoryOpenEvent) -> Unit>()
    private val clickHandlers = mutableListOf<(InventoryClickEvent) -> Unit>()
    private val closeHandlers = mutableListOf<(InventoryCloseEvent) -> Unit>()

    override fun getInventory(): Inventory {
        return inventory
    }

    override fun getItems(): Map<Int, GuiItem> {
        val items = mutableMapOf<Int, GuiItem>()
        for (i in 0 until inventory.contents.size) {
            val guiItem = inventory.getItem(i)?.toGuiItem() ?: continue
            itemClickHandlers[i]?.let { guiItem.onClickBlock = it }
            items[i] = guiItem
        }
        return items
    }

    override fun addItem(item: GuiItem, onClick: (InventoryClickEvent) -> Unit): Int {
        val slot = inventory.firstEmpty()
        if (slot != -1) {
            inventory.setItem(slot, item.toItemStack())
            itemClickHandlers[slot] = onClick
            item.onClickBlock = onClick
        }
        return slot
    }
    override fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)) {
        items.forEach { guiItem ->
            val slot = inventory.firstEmpty()
            if (slot != -1) {
                inventory.setItem(slot, guiItem.toItemStack())
                itemClickHandlers[slot] = { event -> onClick.invoke(guiItem, event) }
            }
        }
    }
    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)): Int {
        if (index < inventory.size) {
            inventory.setItem(index, item.toItemStack())
            itemClickHandlers[index] = onClick
            return index
        }
        return -1

    }

    override fun removeItem(item: GuiItem): GUIPage {
        val slot = inventory.first(item.toItemStack())
        if (slot != -1) {
            inventory.setItem(slot, null)
            itemClickHandlers.remove(slot)
        }
        return this
    }
    override fun removeItem(slot: Int): GUIPage {
        if (slot >= -1 && slot < inventory.size) {
            inventory.setItem(slot, null)
            itemClickHandlers.remove(slot)
        }
        return this
    }

    override fun onClick(handler: (InventoryClickEvent) -> Unit) = clickHandlers.add(handler)
    override fun onOpen(handler: (InventoryOpenEvent) -> Unit) = openHandlers.add(handler)
    override fun onClose(handler: (InventoryCloseEvent) -> Unit) = closeHandlers.add(handler)

    override fun handleClick(event: InventoryClickEvent) {
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

    override fun inventoryTrim() {

    }

    //    override fun inventoryTrim() {
//        val mainContents = mainGui.inventory.contents
//        val totalRows = mainContents.size / 9
//
//        // Find all non-empty row indexes
//        val nonEmptyRowIndices = (0 until totalRows).filter { !isRowEmpty(mainContents, it) }
//
//        // Determine the number of rows to keep (at most `rows` from bottom)
//        val rowsToKeep = nonEmptyRowIndices.takeLast(maxOf(setting.rows, nonEmptyRowIndices.size))
//
//
//        // Recreate inventory with only the visible rows
//        val newInventory = Bukkit.createInventory(mainGui, rowsToKeep.size * 9, setting.title)
//        inventory.contents.forEachIndexed {index, stack ->
//            if(stack!=null) newInventory.setItem(index, stack)
//        }
//        itemClickHandlers.clear()
//
//        // Copy items and handlers for the kept rows
//        rowsToKeep.forEachIndexed { index, rowIndex ->
//            val start = rowIndex * 9
//            val destStart = index * 9
//            for (i in 0 until 9) {
//                val sourceSlot = start + i
//                val destSlot = destStart + i
//
//                val item = mainContents.getOrNull(sourceSlot)
//                inventory.setItem(destSlot, item)
//
//                mainGui.itemClickHandlers[sourceSlot]?.let { handler ->
//                    itemClickHandlers[destSlot] = handler
//                }
//            }
//        }
//    }
    private fun isRowEmpty(contents: Array<ItemStack?>, rowIndex: Int): Boolean {
        val start = if (rowIndex == 0) 0 else (rowIndex * 9)
        for (i in 0 until 9) {
            val item = contents.getOrNull(start + i)
            if (item != null && item.type != Material.AIR) {
                return false
            }
        }
        return true
    }

}

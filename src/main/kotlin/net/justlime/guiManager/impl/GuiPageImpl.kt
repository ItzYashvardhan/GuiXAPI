package net.justlime.guiManager.impl

import net.justlime.guiManager.handle.GUIPage
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class GuiPageImpl(holder: InventoryHolder, title: String, rows: Int, contents: Array<ItemStack>) : GUIPage {
    private var inventory = Bukkit.createInventory(holder, rows * 9, title)
    private val openHandlers = mutableListOf<(InventoryOpenEvent) -> Unit>()
    private val clickHandlers = mutableListOf<(InventoryClickEvent) -> Unit>()
    private val closeHandlers = mutableListOf<(InventoryCloseEvent) -> Unit>()
    private val itemClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    override fun getInventory(): Inventory {
        return inventory
    }

    init {
        // Copy existing contents first
        val currentContents = contents.toMutableList()

        // Remove empty rows from the middle if necessary
        val maxSlots = rows * 9
        if (currentContents.size > maxSlots) {
            // Trim rows starting from the top (keep bottom rows with items)
            while (currentContents.size > maxSlots) {
                // Check if there is an empty row (completely AIR)
                var rowRemoved = false
                for (row in 1 until (currentContents.size / 9) - 1) { // Skip first and last row
                    val start = row * 9
                    val end = start + 9
                    if ((start until end).all { currentContents[it] == null || currentContents[it].type == Material.AIR }) {
                        // Remove this empty row
                        repeat(9) { currentContents.removeAt(start) }
                        rowRemoved = true
                        break
                    }
                }
                // If no empty row was found, break to avoid infinite loop
                if (!rowRemoved) break
            }
        }

        // Now copy trimmed contents into the inventory
        for (i in currentContents.indices) {
            if (i < inventory.size) {
                inventory.setItem(i, currentContents[i])
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

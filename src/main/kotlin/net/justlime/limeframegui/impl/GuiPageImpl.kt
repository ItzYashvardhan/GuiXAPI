package net.justlime.limeframegui.impl

import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.handle.GUIEventHandler
import net.justlime.limeframegui.handle.GUIPage
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.utilities.setItem
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

class GuiPageImpl(override val currentPage: Int, private val handler: GUIEventHandler, val setting: GUISetting, private val builder: ChestGUIBuilder) : GUIPage {
    override var inventory = handler.createPageInventory(currentPage, setting)

    override fun getItems(): Map<Int, GuiItem> {
        val items = mutableMapOf<Int, GuiItem>()
        for (i in 0 until inventory.contents.size) {
            val guiItem = inventory.getItem(i)?.toGuiItem() ?: continue
            items[i] = guiItem
        }
        return items
    }

    private var trackPageId = currentPage
    override fun addItem(item: GuiItem, onClick: (InventoryClickEvent) -> Unit): Int {
        fun findFreeSlot(inv: Inventory): Int =
            (0 until inv.size).firstOrNull { it !in getReservedSlots(inv) && inv.getItem(it) == null } ?: -1

        // Apply placeholders from setting if not set
        if (item.placeholderPlayer == null) item.placeholderPlayer = setting.placeholderPlayer
        if (item.placeholderOfflinePlayer == null) item.placeholderOfflinePlayer = setting.placeholderOfflinePlayer

        // Try current page
        findFreeSlot(inventory).takeIf { it != -1 }?.let { slot ->
            inventory.setItem(slot, item)
            handler.itemClickHandler.computeIfAbsent(currentPage) { mutableMapOf() }[slot] = onClick
            return slot
        }


        // Find an unused page ID greater than current
        fun findNextPageId(start: Int): Int {
            var id = start
            while (handler.pageInventories.containsKey(id)) {
                id++
            }
            return id
        }

        val nextPageId = trackPageId

        // Try next existing page if available
        handler.pageInventories[nextPageId]?.let { inv ->
            findFreeSlot(inv).takeIf { it != -1 }?.let { slot ->
                inv.setItem(slot, item)
                handler.itemClickHandler.computeIfAbsent(nextPageId) { mutableMapOf() }[slot] = onClick
                return slot
            }
        }

        // Create new page and add the item there
        var newSlot = -1
        val newPageId = findNextPageId(nextPageId)
        trackPageId = newPageId
        if (LimeFrameAPI.debugging) println("Creating NewPage $trackPageId")
        addPage(trackPageId, setting.rows, setting.title) {
            newSlot = addItem(item, onClick)
        }
        return newSlot
    }


    private fun getReservedSlots(inventory: Inventory): Set<Int> {
        val lastSlot = inventory.size - 1
        val lastRowFirstSlot = lastSlot - 8
        val margin = builder.reservedSlot.navMargin

        return buildSet {
            // Handle next page slot
            if (builder.reservedSlot.nextPageSlot != -1) {
                add(builder.reservedSlot.nextPageSlot)
            } else if (builder.reservedSlot.enableNavSlotReservation){
                add(lastSlot - margin)
                builder.reservedSlot.otherSlot.addAll(lastRowFirstSlot..lastSlot)
            }

            // Handle previous page slot
            if (builder.reservedSlot.prevPageSlot != -1) {
                add(builder.reservedSlot.prevPageSlot)
            } else if(builder.reservedSlot.enableNavSlotReservation) {
                add(lastRowFirstSlot + margin)
                builder.reservedSlot.otherSlot.addAll(lastRowFirstSlot..lastSlot)
            }

            // Add other reserved slots like rows
            addAll(builder.reservedSlot.otherSlot)
        }
    }


    override fun addItem(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)) {
        items.forEach { item ->
            val slot = inventory.firstEmpty()
            if (slot != -1) {
                if (item.placeholderPlayer == null) item.placeholderPlayer = setting.placeholderPlayer
                if (item.placeholderOfflinePlayer == null) item.placeholderOfflinePlayer = setting.placeholderPlayer
                inventory.setItem(slot, item)
                handler.itemClickHandler[currentPage] = mutableMapOf(slot to { event -> onClick.invoke(item, event) })
            }
        }
    }

    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)): Int {
        if (index < inventory.size) {
            if (item.placeholderPlayer == null) item.placeholderPlayer = setting.placeholderPlayer
            if (item.placeholderOfflinePlayer == null) item.placeholderOfflinePlayer = setting.placeholderPlayer
            inventory.setItem(index, item)
            handler.itemClickHandler.computeIfAbsent(currentPage) { mutableMapOf() }[index] = onClick

            return index
        }
        return -1

    }

    override fun removeItem(item: GuiItem): GUIPage {
        val slot = item.slot
        val slots = item.slotList
        if (slot != null) {
            removeItem(slot)
        }
        if (slots.isNotEmpty()) {
            slots.forEach { removeItem(it) }
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

    override fun removeItem(slotList: List<Int>): GUIPage {
        slotList.forEach { removeItem(it) }
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
        throw IllegalStateException("Navigation can only be configured at the top-level GUI builder. Its not ideal to be used in nested pages")
    }

    override fun openPage(player: Player, id: Int) {
        handler.open(player, id)
    }

}

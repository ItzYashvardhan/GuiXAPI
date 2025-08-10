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

class GuiPageImpl(override val currentPage: Int, override val handler: GUIEventHandler, val setting: GUISetting, private val builder: ChestGUIBuilder) : GUIPage {
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
    override var trackAddItemSlot = mutableMapOf<Int, Pair<GuiItem, (InventoryClickEvent) -> Unit>>()

    override fun addItem(item: GuiItem, onClick: (InventoryClickEvent) -> Unit): Int {
        fun findFreeSlot(inv: Inventory): Int = (0 until inv.size).firstOrNull { it !in getReservedSlots(inv) && inv.getItem(it) == null } ?: -1

        // Apply placeholders from setting if not set
        if (item.placeholderPlayer == null) item.placeholderPlayer = setting.placeholderPlayer
        if (item.placeholderOfflinePlayer == null) item.placeholderOfflinePlayer = setting.placeholderOfflinePlayer
        if (item.smallCaps == null) item.smallCaps = setting.smallCaps

        // Try current page
        findFreeSlot(inventory).takeIf { it != -1 }?.let { slot ->
            inventory.setItem(slot, item)
            trackAddItemSlot[slot] =item to onClick
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
                builder.pages[nextPageId]?.trackAddItemSlot[slot] = item to onClick

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
            } else if (builder.reservedSlot.enableNavSlotReservation) {
                add(lastSlot - margin)
                builder.reservedSlot.otherSlot.addAll(lastRowFirstSlot..lastSlot)
            }

            // Handle previous page slot
            if (builder.reservedSlot.prevPageSlot != -1) {
                add(builder.reservedSlot.prevPageSlot)
            } else if (builder.reservedSlot.enableNavSlotReservation) {
                add(lastRowFirstSlot + margin)
                builder.reservedSlot.otherSlot.addAll(lastRowFirstSlot..lastSlot)
            }

            // Add other reserved slots like rows
            addAll(builder.reservedSlot.otherSlot)
        }
    }

    override fun addItem(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)) {
        items.forEach { guiItem ->
            addItem(guiItem) { event -> onClick.invoke(guiItem, event) }
        }
    }

    override fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)): Int {
        if (index < inventory.size) {
            if (item.placeholderPlayer == null) item.placeholderPlayer = setting.placeholderPlayer
            if (item.placeholderOfflinePlayer == null) item.placeholderOfflinePlayer = setting.placeholderPlayer
            if (item.smallCaps == null) item.smallCaps = setting.smallCaps
            inventory.setItem(index, item)
            handler.itemClickHandler.computeIfAbsent(currentPage) { mutableMapOf() }[index] = onClick

            return index
        }
        return -1

    }

    override fun remove(slot: Int): GUIPage {

        // Ensure the item being removed is a dynamically added one on the current page.
        if (builder.pages[currentPage]?.trackAddItemSlot?.containsKey(slot) != true) {
            // If not, just clear the slot and do nothing else.
            inventory.setItem(slot, null)
            handler.itemClickHandler[currentPage]?.remove(slot)
            return this
        }

        // 1. Collect all dynamically added items from all pages into a single, ordered list.
        // This list will represent the continuous space that items occupy.
        val dynamicItems = mutableListOf<Triple<Int, Int, Pair<GuiItem, (InventoryClickEvent) -> Unit>>>()
        builder.pages.toSortedMap().forEach { (pageId, guiPage) ->
            // Sort by slot to ensure items on the same page are in order.
            guiPage.trackAddItemSlot.toSortedMap().forEach { (itemSlot, itemData) ->
                dynamicItems.add(Triple(pageId, itemSlot, itemData))
            }
        }

        // 2. Find the linear index of the item we need to remove.
        val removalIndex = dynamicItems.indexOfFirst { (pageId, itemSlot, _) ->
            pageId == currentPage && itemSlot == slot
        }

        // This should always be found due to the initial check, but as a safeguard:
        if (removalIndex == -1) return this

        // 3. Shift all subsequent items forward by one position.
        // We iterate from the removal index to the second-to-last item.
        for (i in removalIndex until dynamicItems.size - 1) {
            val targetLocation = dynamicItems[i]
            val sourceItem = dynamicItems[i + 1]

            val targetPageId = targetLocation.first
            val targetSlot = targetLocation.second
            val (sourceItemData, sourceClickHandler) = sourceItem.third

            val targetPage = builder.pages[targetPageId] ?: continue

            // Move the source item to the target slot.
            targetPage.setItem(targetSlot, sourceItemData, sourceClickHandler)
            targetPage.trackAddItemSlot[targetSlot] = sourceItem.third
        }

        // 4. Clear the last item's original slot, as it has now been moved.
        dynamicItems.lastOrNull()?.let { lastItemLocation ->
            val (lastItemPageId, lastItemSlot) = lastItemLocation
            val lastItemPage = builder.pages[lastItemPageId]
            lastItemPage?.inventory?.setItem(lastItemSlot, null)
            handler.itemClickHandler[lastItemPageId]?.remove(lastItemSlot)
            lastItemPage?.trackAddItemSlot?.remove(lastItemSlot)
        }
        return this
    }

    override fun remove(slotList: List<Int>): GUIPage {
        // Sort descending to avoid index shifting issues when removing multiple items.
        slotList.sortedDescending().forEach { remove(it) }
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

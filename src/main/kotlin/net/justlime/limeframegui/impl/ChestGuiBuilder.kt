package net.justlime.limeframegui.impl

import net.justlime.limeframegui.enums.ChestGuiActions
import net.justlime.limeframegui.handle.GUIPage
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.type.ChestGUI
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

/**
 * The Builder is a Blueprint: ChestGuiBuilder is a configuration object.
 * Its job is to exist before any player interacts with the GUI.
 * Use it to define the layout, the pages, and the rules. It's like an architect's blueprint for a house.
 */
class ChestGuiBuilder(rows: Int = 6, title: String = "Inventory") {

    val setting = GUISetting(rows, title)

    // Pages are temporarily stored here before being moved to the handler.
    val pages = mutableMapOf<Int, GUIPage>()

    //Main Handler for Registering Events
    private val guiHandler = GuiHandler(setting)

    // All configuration steps are queued as prioritized actions to be executed in order during build().
    private val actions = mutableListOf<Pair<ChestGuiActions, () -> Unit>>()

    init {
        // The global page (ID 0) is created immediately to hold shared items.
        // It uses the GuiImpl as its holder, which is a key part of the design.
        pages[ChestGUI.GLOBAL_PAGE] = GuiPageImpl(ChestGUI.GLOBAL_PAGE, guiHandler, setting)
    }

    // --- Global Event Handlers ---

    fun onOpen(handler: (InventoryOpenEvent) -> Unit) {
        actions.add(ChestGuiActions.GLOBAL_EVENT to {
            guiHandler.globalOpenHandler = handler
        })
    }

    fun onPageOpen(handler: (InventoryOpenEvent) -> Unit) {
        actions.add(ChestGuiActions.PAGE_EVENT to {
            // Iterate over the pages defined in the builder, not the handler's (likely empty) map.
            pages.keys.forEach { pageId ->
                guiHandler.pageOpenHandlers[pageId] = handler
            }
        })
    }

    fun onClose(handler: (InventoryCloseEvent) -> Unit) {
        actions.add(ChestGuiActions.GLOBAL_EVENT to { guiHandler.globalCloseHandler = handler })
    }

    fun onPageClose(handler: (InventoryCloseEvent) -> Unit) {
        actions.add(ChestGuiActions.PAGE_EVENT to {
            pages.keys.forEach { pageId ->
                guiHandler.pageCloseHandlers[pageId] = handler
            }
        })
    }

    fun onClick(handler: (InventoryClickEvent) -> Unit) {
        actions.add(ChestGuiActions.GLOBAL_EVENT to { guiHandler.globalClickHandler = handler })
    }

    // --- Page Management ---
    /**
     * Adds a page with a specific, unique ID.
     * Throws an error if the ID is already in use or is the reserved global ID.
     */
    fun addPage(id: Int, rows: Int = this.setting.rows, title: String = this.setting.title, block: GUIPage.() -> Unit) {
        actions.add(ChestGuiActions.PAGE_MANAGEMENT to {
            if (id == ChestGUI.GLOBAL_PAGE) throw IllegalArgumentException("Cannot overwrite the global page (ID 0).")
            if (pages.containsKey(id)) throw IllegalArgumentException("A page with ID $id already exists.")

            val newPage = createPage(id, GUISetting(rows, title))
            pages[id] = newPage
            newPage.apply(block)
        })
    }

    /**
     * Adds a page with an automatically assigned, incremental ID. This is the recommended approach.
     */
    fun addPage(rows: Int = this.setting.rows, title: String = this.setting.title, block: GUIPage.() -> Unit) {
        actions.add(ChestGuiActions.PAGE_MANAGEMENT to {
            // This logic correctly finds the next available ID, avoiding conflicts with GLOBAL_PAGE.
            val newId = (pages.keys.maxOrNull() ?: 0) + 1
            val newPage = createPage(newId, GUISetting(rows, title))
            pages[newId] = newPage
            newPage.apply(block)
        })
    }

    /**
     * Creates a new page, correctly copying all items and handlers from the global page.
     */
    private fun createPage(pageId: Int, setting: GUISetting): GUIPage {
        val newPage = GuiPageImpl(pageId, guiHandler, setting)

        // Copy items from the global page's inventory to the new page.
        pages[ChestGUI.GLOBAL_PAGE]?.inventory?.contents?.forEachIndexed { index, itemStack ->
            if (itemStack != null) newPage.setItem(index, itemStack.toGuiItem())
        }

        // Copy item click handlers from the global page to the new page.
        guiHandler.itemClickHandler[ChestGUI.GLOBAL_PAGE]?.forEach { (slot, handler) ->
            val pageHandlers = guiHandler.itemClickHandler.computeIfAbsent(pageId) { mutableMapOf() }
            pageHandlers[slot] = handler
        }

        return newPage
    }

    // --- Item Management ---

    fun addItem(item: GuiItem?, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                // Get the map for the global page, or create it if it doesn't exist.
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }
                globalPage.addItem(item, onClick).let { addedSlot ->
                    // Add the handler for the slot that was automatically found.
                    clickHandlers[addedSlot] = onClick
                }
            }
        }
    }

    fun addItem(items: List<GuiItem>, visibleCondition: () -> Boolean = { true }, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (items.isEmpty() || !visibleCondition()) return@to
            val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
            // Get the map for the global page, or create it if it doesn't exist.
            val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }

            items.forEach { guiItem ->
                globalPage.addItem(guiItem) { event -> onClick.invoke(guiItem, event) }.let { addedSlot ->
                    clickHandlers[addedSlot] = { event -> onClick.invoke(guiItem, event) }
                }
            }
        }
    }

    fun setItem(item: GuiItem?, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }

                if (item.slot != null) {
                    globalPage.setItem(item.slot!!, item, onClick)
                    clickHandlers[item.slot!!] = onClick
                }

                if (item.slotList.isNotEmpty()) {
                    item.slotList.forEach { slot ->
                        globalPage.setItem(slot, item, onClick)
                        clickHandlers[slot] = onClick
                    }
                }
            }
        }
    }

    fun setItems(item: GuiItem?, visibleCondition: () -> Boolean = { true }, onClick: (GuiItem, InventoryClickEvent) -> Unit = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }

                if (item.slot != null) {
                    globalPage.setItem(item.slot!!, item)
                    clickHandlers[item.slot!!] = { event -> onClick.invoke(item, event) }
                }

                if (item.slotList.isNotEmpty()) {
                    item.slotList.forEach { slot ->
                        globalPage.setItem(slot, item)
                        clickHandlers[slot] = { event -> onClick.invoke(item, event) }
                    }
                }

            }
        }
    }

    fun setItem(item: GuiItem?, slot: Int?, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && slot != null && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                // Get the map for the global page, or create it if it doesn't exist.
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }
                globalPage.setItem(slot, item, onClick)
                // Add the handler for this specific slot.
                clickHandlers[slot] = onClick
            }
        }
    }

    fun setItem(items: GuiItem?, slot: List<Int>, visibleCondition: () -> Boolean = { true }, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && items != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(ChestGUI.GLOBAL_PAGE) { mutableMapOf() }

                slot.forEach { currentSlot ->
                    globalPage.setItem(currentSlot, items)
                    clickHandlers[currentSlot] = { event -> onClick.invoke(items, event) }
                }
            }
        }
    }

    fun nav(block: Navigation.() -> Unit) {
        actions += ChestGuiActions.NAVIGATION to { Navigation(this, guiHandler).apply(block).build() }
    }

    fun loadInventoryContents(inventory: Inventory) {
        for (i in 0 until inventory.size) {
            val itemStack = inventory.getItem(i) ?: continue
            val guiItem = itemStack.toGuiItem()
            setItem(guiItem, i)
        }
    }

    /**
     * Executes all queued actions in their prioritized order and returns the fully configured GuiImpl handler.
     */
    fun build(): GuiHandler {
        // Run all the queued configuration actions, sorted by priority.
        actions.sortedBy { it.first.priority }.forEach { it.second.invoke() }

        // After all pages are configured, transfer their final inventories to the handler.
        // This is the final step that makes the GUI "live".
        pages.forEach { (id, page) ->
            guiHandler.pageInventories[id] = page.inventory
        }

        return guiHandler
    }

}
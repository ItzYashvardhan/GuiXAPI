package net.justlime.limeframegui.builder

import net.justlime.limeframegui.enums.ChestGuiActions
import net.justlime.limeframegui.handle.GUIPage
import net.justlime.limeframegui.impl.GuiHandler
import net.justlime.limeframegui.impl.GuiPageImpl
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.type.ChestGUI.Companion.GLOBAL_PAGE
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
class ChestGuiBuilder(title: String = "Inventory", rows: Int = 6) {

    // The builder now configures the GuiImpl handler directly.
    val setting = GUISetting(title, rows)
    private val guiHandler = GuiHandler(setting)

    // Pages are temporarily stored here before being moved to the handler.
    val pages = mutableMapOf<Int, GUIPage>()

    // All configuration steps are queued as prioritized actions to be executed in order during build().
    private val actions = mutableListOf<Pair<ChestGuiActions, () -> Unit>>()

    init {
        // The global page (ID 0) is created immediately to hold shared items.
        // It uses the GuiImpl as its holder, which is a key part of the design.
        pages[GLOBAL_PAGE] = GuiPageImpl(GLOBAL_PAGE, guiHandler, setting)
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
    fun addPage(id: Int, title: String = this.setting.title, rows: Int = this.setting.rows, block: GUIPage.() -> Unit) {
        actions.add(ChestGuiActions.PAGE_MANAGEMENT to {
            if (id == GLOBAL_PAGE) throw IllegalArgumentException("Cannot overwrite the global page (ID 0).")
            if (pages.containsKey(id)) throw IllegalArgumentException("A page with ID $id already exists.")

            val newPage = createPage(id, GUISetting(title, rows))
            pages[id] = newPage
            newPage.apply(block)
        })
    }

    /**
     * Adds a page with an automatically assigned, incremental ID. This is the recommended approach.
     */
    fun addPage(title: String = this.setting.title, rows: Int = this.setting.rows, block: GUIPage.() -> Unit) {
        actions.add(ChestGuiActions.PAGE_MANAGEMENT to {
            // This logic correctly finds the next available ID, avoiding conflicts with GLOBAL_PAGE.
            val newId = (pages.keys.maxOrNull() ?: 0) + 1
            val newPage = createPage(newId, GUISetting(title, rows))
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
        pages[GLOBAL_PAGE]?.getInventory()?.contents?.forEachIndexed { index, itemStack ->
            if (itemStack != null) newPage.setItem(index, itemStack.toGuiItem())
        }

        // Copy item click handlers from the global page to the new page.
        guiHandler.itemClickHandler[GLOBAL_PAGE]?.forEach { (slot, handler) ->
            val pageHandlers = guiHandler.itemClickHandler.computeIfAbsent(pageId) { mutableMapOf() }
            if (pageHandlers.isNotEmpty()) println("Found handler for slot $slot in page $pageId") else println("No handler found for slot $slot in page $pageId")
            pageHandlers[slot] = handler
        }

        return newPage
    }

    // --- Item Management ---

    fun addItem(item: GuiItem, slot: Int = -1, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition()) {
                val globalPage = pages[GLOBAL_PAGE] ?: return@to
                // Get the map for the global page, or create it if it doesn't exist.
                val clickHandlers = guiHandler.itemClickHandler.getOrPut(GLOBAL_PAGE) { mutableMapOf() }

                if (slot != -1) {
                    globalPage.setItem(slot, item, onClick)
                    // Add the handler for this specific slot.
                    clickHandlers[slot] = onClick
                } else {
                    globalPage.addItem(item, onClick).let { addedSlot ->
                        // Add the handler for the slot that was automatically found.
                        clickHandlers[addedSlot] = onClick
                    }
                }
            }
        }
    }

    fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            val globalPage = pages[GLOBAL_PAGE] ?: return@to
            // Get the map for the global page, or create it if it doesn't exist.
            val clickHandlers = guiHandler.itemClickHandler.getOrPut(GLOBAL_PAGE) { mutableMapOf() }

            items.forEach { guiItem ->
                globalPage.addItem(guiItem).let { slot ->
                    // Add a handler for each item in the list.
                    clickHandlers[slot] = { event -> onClick.invoke(guiItem, event) }
                }
            }
        }
    }

    fun nav(block: Navigation.() -> Unit) {
        actions += ChestGuiActions.NAVIGATION to { Navigation(this, guiHandler).apply(block).build() }
    }

    fun loadInventoryContents(inventory: Inventory){
        for (i in 0 until inventory.size) {
            val itemStack = inventory.getItem(i) ?: continue
            val guiItem = itemStack.toGuiItem()
            addItem(guiItem, i)
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
            guiHandler.pageInventories[id] = page.getInventory()
        }

        return guiHandler
    }

}

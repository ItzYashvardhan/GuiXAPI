package net.justlime.limeframegui.impl

import jdk.nashorn.internal.objects.Global
import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.enums.ChestGuiActions
import net.justlime.limeframegui.handle.GUIPage
import net.justlime.limeframegui.models.FrameReservedSlotPage
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.type.ChestGUI
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import kotlin.to

/**
 * The Builder is a Blueprint: ChestGuiBuilder is a configuration object.
 * Its job is to exist before any player interacts with the GUI.
 * Use it to define the layout, the pages, and the rules. It's like an architect's blueprint for a house.
 */
class ChestGUIBuilder(val setting: GUISetting) {

    // Pages are temporarily stored here before being moved to the handler.
    val pages = mutableMapOf<Int, GUIPage>()

    //Main Handler for Registering Events
    private val guiHandler = GUIEventImpl(setting)

    // All configuration steps are queued as prioritized actions to be executed in order during build().
    private val actions = mutableListOf<Pair<ChestGuiActions, () -> Unit>>()
    private var currentExecutingAction: ChestGuiActions? = null

    val reservedSlot = FrameReservedSlotPage()

    init {
        // The global page (ID 0) is created immediately to hold shared items.
        pages[ChestGUI.GLOBAL_PAGE] = createPage(ChestGUI.GLOBAL_PAGE, setting)
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
        val runBlock = {
            if (LimeFrameAPI.debugging) println("Starting Execution of Page $id")
            if (id == ChestGUI.GLOBAL_PAGE) throw IllegalArgumentException("Cannot overwrite the global page (ID 0).")
            if (pages.containsKey(id)) throw IllegalArgumentException("A page with ID $id already exists.")
            val newPage = createPage(id, GUISetting(rows, title))
            pages[id] = newPage
            newPage.apply(block)
            if (LimeFrameAPI.debugging) println("Finished Execution of Page $id")
        }

        if (currentExecutingAction == ChestGuiActions.PAGE_ITEMS) {
            runBlock() // We are inside PAGE_ITEMS, run immediately
        } else {
            actions += ChestGuiActions.PAGE_ITEMS to runBlock // Otherwise queue
            if (LimeFrameAPI.debugging) println("Queued Page $id")
        }
    }

    /**
     * Adds a page with an automatically assigned, incremental ID. This is the recommended approach.
     */
    fun addPage(rows: Int = this.setting.rows, title: String = this.setting.title, block: GUIPage.() -> Unit) {

        val runBlock = {
            val newId = (pages.keys.maxOrNull() ?: ChestGUI.GLOBAL_PAGE) + 1
            if (LimeFrameAPI.debugging) println("Starting Execution of Page $newId")
            val newPage = createPage(newId, GUISetting(rows, title))
            pages[newId] = newPage
            newPage.apply(block)
            if (LimeFrameAPI.debugging) println("Finished Execution of Page $newId")
        }

        if (currentExecutingAction == ChestGuiActions.PAGE_ITEMS) {
            runBlock()
        } else {
            actions += ChestGuiActions.PAGE_ITEMS to runBlock
            if (LimeFrameAPI.debugging) println("Queued Page ${(pages.keys.maxOrNull() ?: ChestGUI.GLOBAL_PAGE) + 1}")
        }
    }

    /**
     * Creates a new page, correctly copying all items and handlers from the global page.
     */
    private fun createPage(pageId: Int, setting: GUISetting): GUIPage {
        val newPage = GuiPageImpl(pageId, guiHandler, setting, this)

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
        val runBlock = to@{
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                globalPage.addItem(item, onClick)
            }
        }
        if (currentExecutingAction == ChestGuiActions.GLOBAL_ITEMS) runBlock()
        else actions += ChestGuiActions.GLOBAL_ITEMS to runBlock
    }

    fun addItem(items: List<GuiItem>, visibleCondition: () -> Boolean = { true }, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }) {
        val runBlock = to@{
            if (items.isEmpty() || !visibleCondition()) return@to
            val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to

            items.forEach { guiItem ->
                globalPage.addItem(guiItem) { event -> onClick.invoke(guiItem, event) }
            }
        }
        if (currentExecutingAction == ChestGuiActions.GLOBAL_ITEMS) runBlock
        else actions += ChestGuiActions.GLOBAL_ITEMS to runBlock
    }

    fun setItem(item: GuiItem?, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to

                if (item.slot != null) {
                    globalPage.setItem(item.slot!!, item, onClick)
                }

                if (item.slotList.isNotEmpty()) {
                    item.slotList.forEach { slot ->
                        globalPage.setItem(slot, item, onClick)
                    }
                }
            }
        }
    }

    fun setItems(item: GuiItem?, visibleCondition: () -> Boolean = { true }, onClick: (GuiItem, InventoryClickEvent) -> Unit = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to

                if (item.slot != null) {
                    globalPage.setItem(item.slot!!, item) { event -> onClick.invoke(item, event) }
                }

                if (item.slotList.isNotEmpty()) {
                    item.slotList.forEach { slot ->
                        globalPage.setItem(slot, item) { event -> onClick.invoke(item, event) }
                    }
                }

            }
        }
    }

    fun setItem(item: GuiItem?, slot: Int?, visibleCondition: () -> Boolean = { true }, onClick: (InventoryClickEvent) -> Unit = {}) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && slot != null && item != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to
                globalPage.setItem(slot, item, onClick)
            }
        }
    }

    fun setItem(items: GuiItem?, slot: List<Int>, visibleCondition: () -> Boolean = { true }, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }) {
        actions += ChestGuiActions.GLOBAL_ITEMS to {
            if (visibleCondition() && items != null) {
                val globalPage = pages[ChestGUI.GLOBAL_PAGE] ?: return@to

                slot.forEach { currentSlot ->
                    globalPage.setItem(currentSlot, items) { event -> onClick.invoke(items, event) }
                }
            }
        }
    }

    fun nav(block: Navigation.() -> Unit) {

        val navigation = Navigation(this, guiHandler).apply(block)


        reservedSlot.enableNavSlotReservation = true
        reservedSlot.nextPageSlot = navigation.nextSlot
        reservedSlot.prevPageSlot = navigation.prevSlot
        reservedSlot.navMargin = navigation.margin



        if (LimeFrameAPI.debugging) println("Queued Navigation")
        val runBlock = { navigation.build() }

        if (currentExecutingAction == ChestGuiActions.NAVIGATION) runBlock()
        else actions += ChestGuiActions.NAVIGATION to runBlock
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
    fun build(): GUIEventImpl {
        actions.sortedBy { it.first.priority }.forEach { (action, block) ->
            currentExecutingAction = action
            block()
            currentExecutingAction = null
        }

        pages.forEach { (id, page) ->
            guiHandler.pageInventories[id] = page.inventory
        }

        return guiHandler
    }

}
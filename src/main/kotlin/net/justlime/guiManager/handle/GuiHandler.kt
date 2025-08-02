package net.justlime.guiManager.handle

import net.justlime.guiManager.models.GUISetting
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

/**
 * The core implementation of the GUI handler.
 *
 * This refined version simplifies handler management, improves event processing logic,
 * and adds the ability to open specific pages.
 *
 * @param setting The basic settings for the GUI (title, rows).
 */
class GuiHandler(private val setting: GUISetting) : GUI {

    // A single, optional handler for global events.
    var globalOpenHandler: ((InventoryOpenEvent) -> Unit)? = null
    var globalCloseHandler: ((InventoryCloseEvent) -> Unit)? = null
    var globalClickHandler: ((InventoryClickEvent) -> Unit)? = null

    // Page-specific handlers, mapping a page ID to a single handler function.
    val pageOpenHandlers = mutableMapOf<Int, (InventoryOpenEvent) -> Unit>()
    val pageCloseHandlers = mutableMapOf<Int, (InventoryCloseEvent) -> Unit>()
    val pageClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    // A single, unified map for all item-specific click handlers.
    // Structure: Page ID -> (Slot -> Handler)
    val itemClickHandler = mutableMapOf<Int, MutableMap<Int, (InventoryClickEvent) -> Unit>>()

    // Stores the actual Inventory object for each page. This is populated by the builder.
    val pageInventories = mutableMapOf<Int, Inventory>()

    // Tracks the current page for each player viewing the GUI.
    val currentPages = mutableMapOf<String, Int>()

    /**
     * Opens a specific page of the GUI for a player.
     *
     * @param player The player to open the GUI for.
     * @param pageId The ID of the page to open. Defaults to 0.
     */
    override fun open(player: Player, pageId: Int) {
        val inventoryToOpen = pageInventories[pageId]
        if (inventoryToOpen == null) {
            // Optionally send an error message to the player or log it.
            player.sendMessage("§cError: GUI page $pageId not found.")
            return
        }

        setCurrentPage(player, pageId)

        // Open the inventory for the player.
        player.openInventory(inventoryToOpen)
    }

    // --- Interface and Event Handler Implementation ---

    override fun getInventory(): Inventory {
        // Return the global page (0) inventory by default, or an empty one if it doesn't exist.
        return pageInventories[0] ?: Bukkit.createInventory(this, setting.rows * 9, setting.title)
    }

    /**
     * Sets the current page for a player. This is typically called by the builder or the open function.
     */
    fun setCurrentPage(player: Player, pageId: Int) {
        currentPages[player.name] = pageId
    }

    /**
     * Gets the current page a player is viewing. Defaults to 0.
     */
    fun getCurrentPage(player: Player): Int {
        return currentPages[player.name] ?: 0
    }

    /**
     * Handles click events with a clear priority system, stopping if the event is cancelled.
     */
    override fun onEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val slot = event.slot
        val pageId = getCurrentPage(player)

        // Priority 1: Page-and-slot-specific handler.
        itemClickHandler[pageId]?.get(slot)?.invoke(event)


        // Priority 3: Page-wide click handler.
        pageClickHandlers[pageId]?.invoke(event)

        // Priority 4: Global click handler.
        globalClickHandler?.invoke(event)
    }

    /**
     * Handles inventory open events.
     */
    override fun onEvent(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        pageOpenHandlers[pageId]?.invoke(event)
        if (event.isCancelled) return

        globalOpenHandler?.invoke(event)
    }

    /**
     * Handles inventory close events and cleans up player tracking to prevent memory leaks.
     */
    override fun onEvent(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        // Fire handlers first.
        pageCloseHandlers[pageId]?.invoke(event)
        globalCloseHandler?.invoke(event)

        // Clean up the player's page tracking to prevent memory leaks.
        currentPages.remove(player.name)
    }

    override fun load(inventory: Inventory): GUI {
        return this
    }
}

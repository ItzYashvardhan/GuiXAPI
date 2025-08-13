package net.justlime.limeframegui.impl

import net.justlime.limeframegui.handle.GUIEventHandler
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.type.ChestGUI
import net.justlime.limeframegui.color.FrameColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin

/**
 * The core implementation of the GUI handler.
 *
 * It simplifies event handler management, improves event processing logic,
 * and adds the ability to open specific pages.
 *
 * @param setting The basic settings for the GUI (title, rows).
 */
class GUIEventImpl(private val setting: GUISetting) : GUIEventHandler {
    private val hasTriggeredGlobalOpen = mutableSetOf<String>()

    /** A single, optional handler for global events. **/
    override var globalOpenHandler: ((InventoryOpenEvent) -> Unit)? = null
    override var globalCloseHandler: ((InventoryCloseEvent) -> Unit)? = null
    override var globalClickHandler: ((InventoryClickEvent) -> Unit)? = null

    /** Page-specific handlers, mapping a page ID to a single handler function**/
    override val pageOpenHandlers = mutableMapOf<Int, (InventoryOpenEvent) -> Unit>()
    override val pageCloseHandlers = mutableMapOf<Int, (InventoryCloseEvent) -> Unit>()
    override val pageClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    /** A single, unified map for all item-specific click handlers.**/
    // Structure: Page ID -> (Slot -> Handler)
    override val itemClickHandler = mutableMapOf<Int, MutableMap<Int, (InventoryClickEvent) -> Unit>>()

    // Stores the actual Inventory object for each page. This is populated by the builder.
    override val pageInventories = mutableMapOf<Int, Inventory>()

    // Tracks the current page for each player viewing the GUI.
    override val currentPages = mutableMapOf<String, Int>()

    /**
     * Opens a specific page of the GUI for a player.
     *
     * @param player The player to open the GUI for.
     * @param page The ID of the page to open. Defaults to 0.
     */
    override fun open(player: Player, page: Int) {

        val inventoryToOpen = pageInventories[page]
        if (inventoryToOpen == null) {
            // Optionally send an error message to the player or log it.
            player.sendMessage("§cError: GUI page $page not found.")
            return
        }

        setCurrentPage(player, page)

        // Open the inventory for the player.
        player.openInventory(inventoryToOpen)
    }

    /**
     * The base Inventory of Page. It means its content will copy to all pages.
     */
    override fun getInventory(): Inventory {
        return pageInventories[ChestGUI.GLOBAL_PAGE] ?: createPageInventory(ChestGUI.GLOBAL_PAGE, setting)
    }

    override fun createPageInventory(id: Int, setting: GUISetting): Inventory {

        val size = setting.rows * 9
        val title = setting.title.replace("{page}", id.toString())

        val coloredTitle = FrameColor.applyColor(title, setting.placeholderPlayer, setting.placeholderOfflinePlayer, setting.smallCapsTitle, setting.customPlaceholder)

        val inv = Bukkit.createInventory(this, size, coloredTitle)
        pageInventories[id] = inv
        return inv
    }

    /**
     * Sets the current page for a player. This is typically called by the builder or the open function.
     */
    override fun setCurrentPage(player: Player, page: Int) {
        currentPages[player.name] = page
    }

    /**
     * Gets the current page a player is viewing. Defaults to 0.
     */
    override fun getCurrentPage(player: Player): Int? {
        return currentPages[player.name]
    }

    /**
     * Handles inventory open events.
     */
    override fun onEvent(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        pageOpenHandlers[pageId]?.invoke(event)

        if (!hasTriggeredGlobalOpen.contains(player.name)) {
            globalOpenHandler?.invoke(event)
            hasTriggeredGlobalOpen.add(player.name)
        }
    }

    /**
     * Handles click events with a clear priority system, stopping if the event is cancelled.
     */
    override fun onEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val slot = event.slot
        val pageId = getCurrentPage(player) ?: return

        // Priority 1: Page-and-slot-specific handler.
        itemClickHandler[pageId]?.get(slot)?.invoke(event)

        // Priority 2: Page-wide click handler.
        pageClickHandlers[pageId]?.invoke(event)

        // Priority 3: Global click handler.
        globalClickHandler?.invoke(event)
    }

    /**
     * Handles inventory close events and cleans up player tracking to prevent memory leaks.
     */
    override fun onEvent(event: InventoryCloseEvent, plugin: JavaPlugin) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        // Fire handlers first.
        pageCloseHandlers[pageId]?.invoke(event)

        // Clean up the player's page tracking to prevent memory leaks.
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val openInventory = getTopInventorySafe(player)
            if (!pageInventories.containsValue(openInventory)) {
                globalCloseHandler?.invoke(event)
                currentPages.remove(player.name)
                hasTriggeredGlobalOpen.remove(player.name)
            }
        })

    }

    //Reflection Used here to support backward Compatibility
    private fun getTopInventorySafe(player: Player): Inventory? {
        return try {
            //Get Open Inventory Method
            val getOpenInvMethod = player.javaClass.getMethod("getOpenInventory")
            val inventoryView = getOpenInvMethod.invoke(player)

            val getTopInvMethod = inventoryView.javaClass.getDeclaredMethod("getTopInventory")

            // bypass package-private restriction
            getTopInvMethod.isAccessible = true

            getTopInvMethod.invoke(inventoryView) as? Inventory
        } catch (ex: Throwable) {
            Bukkit.getLogger().warning("Failed to get top inventory: ${ex.message}")
            null
        }
    }


}
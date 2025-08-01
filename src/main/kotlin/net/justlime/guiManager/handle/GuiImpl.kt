package net.justlime.guiManager.handle

import net.justlime.guiManager.models.GUISetting
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import java.util.*

class GuiImpl(private val setting: GUISetting) : GUI {

    private val inventory: Inventory = Bukkit.createInventory(this, setting.rows * 9, setting.title)

    // === Global handlers ===
    val clickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()
    val openHandlers = mutableMapOf<Int, (InventoryOpenEvent) -> Unit>()
    val closeHandlers = mutableMapOf<Int, (InventoryCloseEvent) -> Unit>()

    val itemClickHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    // === Page-specific handlers ===
    val pageClickHandler = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()
    val pageOpenHandlers = mutableMapOf<Int, (InventoryOpenEvent) -> Unit>()
    val pageCloseHandlers = mutableMapOf<Int, (InventoryCloseEvent) -> Unit>()
    val pageItemClickHandlers = mutableMapOf<Int, MutableMap<Int, (InventoryClickEvent) -> Unit>>()

    // === Track current page per player ===
    private val currentPages = mutableMapOf<String, Int>()

    override fun getInventory(): Inventory = inventory

    override fun load(inventory: Inventory): GUI {
        this.inventory.contents = inventory.contents
        return this
    }

    fun setCurrentPage(player: Player, pageId: Int) {
        currentPages[player.name] = pageId
    }

    fun getCurrentPage(player: Player): Int {
        return currentPages[player.name] ?: 0
    }

    override fun onEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val slot = event.slot
        val pageId = getCurrentPage(player)

        // 1. Global slot-specific handler
        itemClickHandlers[slot]?.invoke(event)

        // 2. Page-specific slot handler
        pageItemClickHandlers[pageId]?.get(slot)?.invoke(event)

        // 3. Page-wide click handler
        pageClickHandler[pageId]?.invoke(event)

        // 4. Global click handlers
        clickHandlers.forEach { it.value.invoke(event) }
    }

    override fun onEvent(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        pageOpenHandlers[pageId]?.invoke(event)
        openHandlers.forEach { it.value.invoke(event) }
    }

    override fun onEvent(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val pageId = getCurrentPage(player)

        pageCloseHandlers[pageId]?.invoke(event)
        closeHandlers.forEach { it.value.invoke(event) }
    }
}

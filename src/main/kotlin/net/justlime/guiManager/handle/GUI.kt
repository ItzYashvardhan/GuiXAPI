package net.justlime.guiManager.handle

import net.justlime.guiManager.impl.GuiPageImpl
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

interface GUI : InventoryHolder {

    // The inventory associated with this GUI
    override fun getInventory(): Inventory

    fun createPage(setting: GUISetting): GUIPage

    operator fun get(index: Int): GUIPage?
    operator fun set(index: Int, setting: GUISetting): GUIPage
    operator fun set(index: Int, guiPage: GUIPage): GUIPage

    operator fun minusAssign(index: Int)
    fun openPage(player: Player, index: Int)

    // --- Methods for the USER to call to add logic ---
    fun onClick(handler: (event: InventoryClickEvent) -> Unit)
    fun onOpen(handler: (event: InventoryOpenEvent) -> Unit)
    fun onClose(handler: (event: InventoryCloseEvent) -> Unit)

    // --- Methods for the LISTENER to call ---
    fun onEvent(event: InventoryClickEvent)
    fun onEvent(event: InventoryOpenEvent)
    fun onEvent(event: InventoryCloseEvent)

    //Method to add Items
    fun addItem(item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)? = {}): GUI
    fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit)? = null): GUI
    fun setItem(index: Int, item: GuiItem, onClick: ((InventoryClickEvent) -> Unit)? = {}): GUI
    fun removeItem(item: GuiItem): GUI

    companion object {
        /**
         * Factory method to create a standard GUI instance using the abstract implementation.
         */
        fun create(setting: GUISetting): GUI {
            // This returns a ready-to-use GUI object with all the logic built-in.
            return GuiImpl(setting)
        }

        fun load(inventory: Inventory, setting: GUISetting): GUI {
            val gui = create(setting)
            gui.inventory.contents = inventory.contents // Copy contents
            // If you had a way to serialize/deserialize event handlers, you'd load them here.
            return gui
        }

    }
}
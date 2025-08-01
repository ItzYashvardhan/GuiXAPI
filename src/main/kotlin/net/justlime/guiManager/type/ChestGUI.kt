package net.justlime.guiManager.type

import net.justlime.guiManager.handle.GUIPage
import net.justlime.guiManager.handle.GuiImpl
import net.justlime.guiManager.impl.GuiPageImpl
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class ChestGUI(title: String = "Inventory", rows: Int = 6, block: ChestGUI.() -> Unit = {}) {
    var currentPage = 0
    val pages = mutableMapOf<Int, GUIPage>()
    val setting = GUISetting(title, rows)
    private val guiHandler = GuiImpl(setting)

    init {
        pages[GLOBAL_PAGE] = createPage(GLOBAL_PAGE, setting)
        this.apply(block)
    }

    companion object {
        const val GLOBAL_PAGE = 0
    }

    fun onOpen(handler: (InventoryOpenEvent) -> Unit = {}) {
        guiHandler.openHandlers[GLOBAL_PAGE] = handler
    }

    fun onClose(handler: (InventoryCloseEvent) -> Unit = {}) {
        guiHandler.closeHandlers[GLOBAL_PAGE] = handler
    }

    fun onClick(handler: (InventoryClickEvent) -> Unit = {}) {
        guiHandler.clickHandlers[GLOBAL_PAGE] = handler
    }

    fun addPage(
        title: String = setting.title, rows: Int = setting.rows, id: Int = pages.keys.maxOrNull() ?: -1, block: GUIPage.() -> Unit = { }
    ): GUIPage {

        val pageId = if (pages.containsKey(id)) pages.keys.maxOrNull()?.plus(1) ?: 0 else id
        val newPage = createPage(pageId, GUISetting(title, rows))
        pages[pageId] = newPage
        guiHandler.pageOpenHandlers[pageId] = newPage::handleOpen
        guiHandler.pageClickHandler[pageId] = newPage::handleClick
        guiHandler.pageCloseHandlers[pageId] = newPage::handleClose
        newPage.apply(block)
        return newPage
    }

    fun addItem(item: GuiItem, index: Int = -1, onClick: (InventoryClickEvent) -> Unit = {}): ChestGUI {
        if (index != -1) {
            pages[GLOBAL_PAGE]?.setItem(index, item, onClick)
            guiHandler.itemClickHandlers[index] = onClick
            return this
        }

        pages[GLOBAL_PAGE]?.addItem(item, onClick)?.let { slot ->
            guiHandler.itemClickHandlers[slot] = onClick
        }
        return this
    }

    fun addItems(items: List<GuiItem>, onClick: ((GuiItem, InventoryClickEvent) -> Unit) = { _, _ -> {} }): ChestGUI {
        items.forEach { guiItem ->
            val slot = pages[GLOBAL_PAGE]?.addItem(guiItem) ?: -1
            if (slot != -1) {
                guiHandler.itemClickHandlers[slot] = { event -> onClick.invoke(guiItem, event) }
            }
        }
        return this
    }

    fun removeItem(item: GuiItem): ChestGUI {
        pages[GLOBAL_PAGE]?.removeItem(item)
        val slot = guiHandler.inventory.first(item.toItemStack())
        if (slot != -1) {
            guiHandler.itemClickHandlers.remove(slot)
        }
        return this
    }

    fun open(player: Player, page: Int = currentPage) {
        currentPage = page
        guiHandler.setCurrentPage(player, currentPage)
        player.openInventory(pages[page]?.getInventory() ?: return player.sendMessage("§cPage not found"))
        player.sendMessage("§aOpened Page $page")
    }

    fun getInventory() = pages[currentPage]?.getInventory() ?: Bukkit.createInventory(guiHandler.inventory.holder, setting.rows * 9, setting.title)

    private fun mergeGlobalItems(page: GUIPage): GUIPage {
        val globalPage = pages[GLOBAL_PAGE] ?: return page
        globalPage.getItems().forEach { (slot, guiItem) ->
            page.setItem(slot, guiItem, guiItem.onClickBlock)
        }
        return page
    }

    private fun createPage(pageId: Int, setting: GUISetting): GUIPage {
        val page = GuiPageImpl(guiHandler.inventory.holder!!, setting)
        if (pageId != GLOBAL_PAGE) {
            mergeGlobalItems(page)
        }
        pages[pageId] = page
        return page
    }

}

fun test() {
    ChestGUI("Test Inventory", 6) {
        val item1 = GuiItem(Material.DIAMOND)
        onClick {

        }
        val addItem = this.addItem(item1) {

            it.whoClicked.sendMessage("Item Clicked on ${setting.title} inventory")
        }

    }
}
package net.justlime.guiManager.handle

import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.type.ChestGUI
import net.justlime.guiManager.utilities.toGuiItem
import net.justlime.guiManager.utilities.toSlot
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class GuiBuilder(private val setting: GUISetting) {
    var prevPageItem: GuiItem = ItemStack(Material.ARROW).toGuiItem()
    var prevPageSlot: Pair<Int, Int> = 0 to 0
    var prevPageOnClick: (InventoryClickEvent) -> Unit = {}
    var nextPageItem: GuiItem = ItemStack(Material.ARROW).toGuiItem()
    var nextPageSlot: Pair<Int, Int> = 0 to 0
    var nextPageOnClick: (InventoryClickEvent) -> Unit = {}

    private val itemActions = mutableListOf<Triple<Int, GuiItem, (InventoryClickEvent) -> Unit>>()

    fun item(slot: Int, item: GuiItem, onClick: (InventoryClickEvent) -> Unit = {}) {
        itemActions.add(Triple(slot, item, onClick))
    }

//    fun build(): GUI {
//        val gui = ChestGUI()
//
//        var pageIndex = 0
//
//        val currentPage = gui.getTotalPages()
//
//        val prevSlot = prevPageSlot.toSlot(setting.rows)
//        val nextSlot = nextPageSlot.toSlot(setting.rows)
//
//        println(prevSlot)
//        println(nextSlot)
//
//        if (prevSlot != -1) gui.setItem(prevSlot, prevPageItem) {
//            val player = it.whoClicked as Player
//            pageIndex--
//            if (pageIndex == 0) {
//                gui.openPage(player, currentPage[pageIndex])
//                gui.setItem(prevSlot, GuiItem.air())
//            } else if (pageIndex > 0) {
//                gui.openPage(player, currentPage[pageIndex])
//            }else pageIndex = 0
//        }
//        if (nextSlot != -1) gui.setItem(nextSlot, nextPageItem) {
//            val player = it.whoClicked as Player
//            pageIndex++
//            if (pageIndex == currentPage.size-1) {
//                gui.openPage(player, currentPage[pageIndex])
//                gui.setItem(nextSlot, GuiItem.air())
//            }else if (pageIndex < currentPage.size-1){
//                gui.openPage(player, currentPage[pageIndex])
//            }
//            else pageIndex = currentPage.size-1
//        }
//
//        for ((slot, item, handler) in itemActions) {
//            when (slot) {
//                prevSlot -> {
//                    val newHandler = { event: InventoryClickEvent ->
//                        handler(event)
//                        prevPageOnClick(event)
//                    }
//                    gui.setItem(slot, item, newHandler)
//                }
//
//                nextSlot -> {
//                    val newHandler = { event: InventoryClickEvent ->
//                        handler(event)
//                        nextPageOnClick(event)
//                    }
//                    gui.setItem(slot, item, newHandler)
//                }
//
//                else -> {
//                    gui.setItem(slot, item, handler)
//                }
//            }
//        }
//        return gui
//    }

}
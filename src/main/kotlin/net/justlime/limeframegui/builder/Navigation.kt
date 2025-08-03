package net.justlime.limeframegui.builder

import net.justlime.limeframegui.impl.GuiHandler
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.type.ChestGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class Navigation(private val builder: ChestGuiBuilder, private val handler: GuiHandler) {

    // --- User-Overridable Settings ---
    var nextItem: GuiItem = GuiItem(Material.ARROW, "§aNext Page")
    var prevItem: GuiItem = GuiItem(Material.ARROW, "§aPrevious Page")
    var margin = 0

    var nextSlot: Int = -1
    var prevSlot: Int = -1

    fun build() {
        val nextOnClick = { event: InventoryClickEvent ->
            val player = event.whoClicked as Player
            val currentPage = handler.getCurrentPage(player)
            val maxPage = handler.pageInventories.keys.filter { it != ChestGUI.GLOBAL_PAGE }.maxOrNull() ?: currentPage

            if (currentPage < maxPage) {
                handler.open(player, currentPage + 1)
            } else {
                player.sendMessage("§cYou are on the last page.")
            }
        }

        val prevOnClick = { event: InventoryClickEvent ->
            val player = event.whoClicked as Player
            val currentPage = handler.getCurrentPage(player)
            val minPage = handler.pageInventories.keys.filter { it != ChestGUI.GLOBAL_PAGE }.minOrNull() ?: currentPage

            if (currentPage > minPage) {
                handler.open(player, currentPage - 1)
            } else {
                player.sendMessage("§cYou are on the first page.")
            }
        }
        val minPageId = builder.pages.keys.filter { it != ChestGUI.GLOBAL_PAGE }.minOrNull() ?: 0
        val maxPageId = builder.pages.keys.filter { it != ChestGUI.GLOBAL_PAGE }.maxOrNull() ?: 0

        builder.pages.forEach { (id, page) ->

            val lastSlot = page.getInventory().size - 1
            val lastRowFirstSlot = lastSlot - 8


            if (id != ChestGUI.GLOBAL_PAGE && id != minPageId) if (prevSlot == -1) page.setItem(lastRowFirstSlot + margin, prevItem, prevOnClick)
            else page.setItem(prevSlot + margin, prevItem, prevOnClick)

            if (id != ChestGUI.GLOBAL_PAGE && id != maxPageId) if (nextSlot == -1) page.setItem(lastSlot - margin, nextItem, nextOnClick)
            else page.setItem(nextSlot - margin, nextItem, nextOnClick)

        }
    }
}
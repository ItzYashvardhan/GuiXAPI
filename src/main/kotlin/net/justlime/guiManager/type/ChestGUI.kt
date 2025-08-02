package net.justlime.guiManager.type

import net.justlime.guiManager.builder.ChestGuiBuilder
import net.justlime.guiManager.handle.GuiHandler
import org.bukkit.entity.Player

class ChestGUI(title: String = "Inventory", rows: Int = 6, block: ChestGuiBuilder.() -> Unit = {}) {
    private val guiHandler: GuiHandler

    init {
        val builder = ChestGuiBuilder(title, rows)
        builder.apply(block)
        this.guiHandler = builder.build()
    }

    /**
     * Opens the GUI for a specific player.
     * @param player The player to open the GUI for.
     * @param page The page number to open to.
     */
    fun open(player: Player, page: Int = 0) {
        guiHandler.open(player, page)
    }

    companion object {
        const val GLOBAL_PAGE = 0
    }
}
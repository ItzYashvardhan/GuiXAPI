package net.justlime.limeframegui.type

import net.justlime.limeframegui.handle.GUIEventHandler
import net.justlime.limeframegui.impl.ChestGUIBuilder
import net.justlime.limeframegui.models.GUISetting
import net.kyori.adventure.title.Title.title
import org.bukkit.entity.Player

/**
 * Initializes a new ChestGUI instance using a builder pattern.
 *
 * @param title The title of the inventory.
 * @param rows The number of rows in the inventory (1-6).
 * @param block A lambda with `ChestGuiBuilder` as its receiver, allowing for
 *              a configuration of the GUI.
 */
class ChestGUI( setting: GUISetting, private val block: ChestGUIBuilder.() -> Unit = {}) {
    constructor(row: Int,title: String) : this(GUISetting(row, title), block = {})

    private val guiHandler: GUIEventHandler


    init {
        val builder = ChestGUIBuilder(setting)
        builder.apply(block)
        this.guiHandler = builder.build()
    }

    /**
     * Opens the GUI for a specific player.
     * @param player The player to open the GUI for.
     * @param page The page number to open to.
     */
    fun open(player: Player, page: Int = if (guiHandler.pageInventories[1] != null) 1 else 0) {
        guiHandler.open(player, page)
    }

    companion object {
        const val GLOBAL_PAGE = 0
    }
}
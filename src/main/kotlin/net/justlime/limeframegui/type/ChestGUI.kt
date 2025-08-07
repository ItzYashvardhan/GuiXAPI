package net.justlime.limeframegui.type

import net.justlime.limeframegui.handle.GUIEventHandler
import net.justlime.limeframegui.impl.ChestGUIBuilder
import net.justlime.limeframegui.models.GUISetting
import org.bukkit.entity.Player

/**
 * Initializes a new ChestGUI instance using a builder pattern.
 *
 * @param setting The settings for the GUI, including rows, title, and optional player for placeholders.
 */
class ChestGUI(val setting: GUISetting, block: ChestGUIBuilder.() -> Unit = {}) {
    constructor(row: Int, title: String, player: Player? = null, block: ChestGUIBuilder.() -> Unit) : this(GUISetting(row, title, player), block)

    private val guiHandler: GUIEventHandler

    init {
        val builder = ChestGUIBuilder(setting)
        builder.apply(block)
        this.guiHandler = builder.build()
    }

    /**
     * Opens the GUI for a given placeholder player.
     * @param page The page number to open to.
     */
    fun open(page: Int = if (guiHandler.pageInventories[1] != null) 1 else 0) {
        setting.placeholderPlayer?.let {
            guiHandler.open(it, page)
        }
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
package net.justlime.guiManager

import net.justlime.guiManager.example.commands.CommandManager
import org.bukkit.plugin.java.JavaPlugin


class GUIManager : JavaPlugin() {

    override fun onEnable() {
        this.saveDefaultConfig()
        CommandManager(this)
        GuiXApi.init(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}

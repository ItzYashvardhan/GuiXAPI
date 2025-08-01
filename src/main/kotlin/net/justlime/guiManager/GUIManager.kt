package net.justlime.guiManager

import net.justlime.guiManager.example.commands.CommandManager
import org.bukkit.plugin.java.JavaPlugin

lateinit var plugin: GUIManager

class GUIManager : JavaPlugin() {

    override fun onEnable() {
        plugin = this
        this.saveDefaultConfig()
        CommandManager(this)
        GuiXApi.init(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}

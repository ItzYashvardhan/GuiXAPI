package net.justlime.guiManager

import net.justlime.guiManager.example.commands.CommandManager
import net.justlime.guiManager.example.listener.ListenerManager
import org.bukkit.plugin.java.JavaPlugin

lateinit var plugin: GUIManager

class GUIManager : JavaPlugin() {

    override fun onEnable() {
        plugin = this
        this.saveDefaultConfig()

        // Plugin startup logic
        CommandManager(this)
        ListenerManager(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}

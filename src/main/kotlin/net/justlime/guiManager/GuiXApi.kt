package net.justlime.guiManager

import net.justlime.guiManager.listener.InventoryListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
* Initialize the Listeners
* */
object GuiXApi {
    fun init(plugin: JavaPlugin) {
        Bukkit.getPluginManager().registerEvents(InventoryListener(), plugin)
    }
}

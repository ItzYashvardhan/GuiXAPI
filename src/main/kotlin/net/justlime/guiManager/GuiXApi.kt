package net.justlime.guiManager

import net.justlime.guiManager.listener.InventoryListener
import net.justlime.guiManager.listener.PluginListener
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Initialize the Listeners
 * */
object GuiXApi {
    fun init(plugin: JavaPlugin) {
        Bukkit.getPluginManager().registerEvents(InventoryListener(plugin), plugin)

        //run a task on plugin disable
        Bukkit.getPluginManager().registerEvents(PluginListener(), plugin)

    }
}

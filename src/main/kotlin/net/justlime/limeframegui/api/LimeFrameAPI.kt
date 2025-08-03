package net.justlime.limeframegui.api

import net.justlime.limeframegui.listener.InventoryListener
import net.justlime.limeframegui.listener.PluginListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * Initialize the Listeners
 * */
object LimeFrameAPI {
    fun init(plugin: JavaPlugin) {
        Bukkit.getPluginManager().registerEvents(InventoryListener(plugin), plugin)

        //run a task on plugin disable
        Bukkit.getPluginManager().registerEvents(PluginListener(), plugin)
    }
}
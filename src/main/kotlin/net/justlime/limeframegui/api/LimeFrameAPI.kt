package net.justlime.limeframegui.api

import net.justlime.limeframegui.listener.InventoryListener
import net.justlime.limeframegui.listener.PluginListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Initialize the Listeners
 * */
object LimeFrameAPI {
    private lateinit var plugin: JavaPlugin
    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
        Bukkit.getPluginManager().registerEvents(InventoryListener(plugin), plugin)

        //run a task on plugin disable
        Bukkit.getPluginManager().registerEvents(PluginListener(), plugin)
    }

    fun getPlugin(): JavaPlugin {
        return plugin
    }
}
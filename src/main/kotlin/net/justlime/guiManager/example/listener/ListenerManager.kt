
package net.justlime.guiManager.example.listener

import org.bukkit.plugin.java.JavaPlugin

class ListenerManager(plugin: JavaPlugin) {

    init {
        plugin.server.pluginManager.registerEvents(InventoryListener(), plugin)
    }

}
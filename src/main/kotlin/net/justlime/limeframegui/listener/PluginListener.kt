package net.justlime.limeframegui.listener

import net.justlime.limeframegui.handle.GUIEventHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent

class PluginListener: Listener {
    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        //safely close all API Inventory useful if force disabled by plugman
        val onlinePlayers = event.plugin.server.onlinePlayers
        for (player in onlinePlayers) {
            val openInventory = player.openInventory.topInventory
            if (openInventory.holder is GUIEventHandler) {
                player.closeInventory()
            }
        }
    }

}
package net.justlime.limeframegui.listener

import net.justlime.limeframegui.handle.GUIEventHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.Inventory

class PluginListener : Listener {
    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        //safely close all API Inventory useful if force disabled by plugman
        val onlinePlayers = event.plugin.server.onlinePlayers
        for (player in onlinePlayers) {
            val openInventory = getTopInventorySafe(player)
            if (openInventory?.holder is GUIEventHandler) {
                player.closeInventory()
            }
        }
    }

    //Reflection Used here to support backward Compatibility
    private fun getTopInventorySafe(player: Player): Inventory? {
        return try {
            val getOpenInvMethod = player.javaClass.getMethod("getOpenInventory")
            val inventoryView = getOpenInvMethod.invoke(player)

            val getTopInvMethod = inventoryView.javaClass.getDeclaredMethod("getTopInventory")
            getTopInvMethod.isAccessible = true // bypass package-private restriction

            getTopInvMethod.invoke(inventoryView) as? Inventory
        } catch (ex: Throwable) {
            Bukkit.getLogger().warning("Failed to get top inventory: ${ex.message}")
            null
        }
    }


}
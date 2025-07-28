package net.justlime.guiManager.manager

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object InventoryManager {

    private val inventoryMap = mutableMapOf<String, Inventory>()

    fun addInventory(key: String, inventory: Inventory) {
        inventoryMap[key] = inventory
    }

    fun getInventory(key: String): Any? {
        return inventoryMap[key]
    }

    fun removeInventory(key: String) {
        inventoryMap.remove(key)
    }

    fun getPlayerHead(offlinePlayer: OfflinePlayer): ItemStack {

        // Create the player head item
        val playerHead = ItemStack(Material.PLAYER_HEAD, 1)

        // Set the head's meta to the player's information
        val meta = playerHead.itemMeta as? SkullMeta
        if (meta != null) {
            meta.owningPlayer = offlinePlayer // Assign the player to the head
            meta.setDisplayName("§a${offlinePlayer.name}'s Head") // Optional: Add a custom name
            playerHead.itemMeta = meta
        }
        return playerHead
    }


}
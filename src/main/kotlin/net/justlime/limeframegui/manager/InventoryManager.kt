package net.justlime.limeframegui.manager

import org.bukkit.inventory.Inventory

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

}
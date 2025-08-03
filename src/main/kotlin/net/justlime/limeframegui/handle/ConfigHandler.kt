package net.justlime.limeframegui.handle

import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.type.ChestGUI
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import java.io.File

interface ConfigHandler {

    companion object {

        fun loadItem(dataFolder: File, filename: String, path: String): GuiItem {
            val file = File(dataFolder, filename)
            val config = YamlConfiguration.loadConfiguration(file)
            val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

            val displayName = section.getString("displayName") ?: ""
            val material = Material.getMaterial(section.getString("material") ?: "STONE") ?: Material.STONE
            val lore = section.getStringList("lore")
            val glow = section.getBoolean("glow", false)
            val flags = section.getStringList("flags").mapNotNull { runCatching { ItemFlag.valueOf(it) }.getOrNull() }
            val customModelData = if (section.contains("customModelData")) section.getInt("customModelData") else null
            val amount = section.getInt("amount", 1)
            val skullTexture = section.getString("skullTexture")
            return GuiItem(material, displayName, amount, lore, glow, flags, customModelData, skullTexture)
        }

        fun loadItems(dataFolder: File, filename: String, path: String): List<GuiItem> {
            val file = File(dataFolder, filename)
            val config = YamlConfiguration.loadConfiguration(file)
            val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

            return section.getKeys(false).mapNotNull { key ->
                section.getConfigurationSection(key)?.let {
                    loadItem(dataFolder, filename, "$path.$key")
                }
            }
        }

        fun loadInventory(dataFolder: File, filename: String, path: String): Inventory {
            val file = File(dataFolder, filename)
            val config = YamlConfiguration.loadConfiguration(file)
            val gui = ChestGUI()

            val section = config.getConfigurationSection(path) ?: return Bukkit.createInventory(null, 9, "Inventory")

            val size = section.getInt("size", 9)
            val title = section.getString("title", "Inventory") ?: "Inventory"
            val inventory = Bukkit.createInventory(null, size, title)

            val itemsSection = section.getConfigurationSection("items") ?: return inventory

            for (key in itemsSection.getKeys(false)) {
                val slot = key.toIntOrNull() ?: continue
                val itemSection = itemsSection.getConfigurationSection(key) ?: continue
                val item = loadItem(dataFolder, filename, "$path.items.$key")
                inventory.setItem(slot, item.toItemStack())
            }

            return inventory
        }

        fun saveItem(dataFolder: File, filename: String, path: String, item: GuiItem): Boolean {
            val file = File(dataFolder, filename)
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val section = config.createSection(path)
            writeItemToSection(section, item)

            return runCatching {
                config.save(file)
            }.isSuccess
        }

        fun saveItems(dataFolder: File, filename: String, path: String, item: GuiItem): Boolean {
            val file = File(dataFolder, filename)
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val baseSection = config.createSection(path)
            val itemSection = baseSection.createSection("0") // Save single item as index "0"
            writeItemToSection(itemSection, item)

            return runCatching {
                config.save(file)
            }.isSuccess
        }

        fun saveInventory(dataFolder: File, filename: String, path: String, inventory: Inventory, inventoryTitle: String = "Inventory"): Boolean {
            val file = File(dataFolder, filename)
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val section = config.createSection(path)
            section.set("size", inventory.size)
            section.set("title", inventoryTitle)

            val itemsSection = section.createSection("items")
            for (i in 0 until inventory.size) {
                val item = inventory.getItem(i) ?: continue
                val guiItem = item.toGuiItem()
                val itemSection = itemsSection.createSection(i.toString())
                writeItemToSection(itemSection, guiItem)
            }

            return runCatching {
                config.save(file)
            }.isSuccess
        }

        /**
         * Shared internal method to write a GuiItem to a ConfigurationSection.
         */
        private fun writeItemToSection(section: ConfigurationSection, item: GuiItem) {
            section.set("displayName", item.displayName)
            section.set("material", item.material.name)
            section.set("lore", item.lore)
            section.set("glow", item.glow)
            section.set("flags", item.flags.mapNotNull { it?.name })
            section.set("customModelData", item.customModelData.toString())
            section.set("skullTexture", item.skullTexture.toString())
            section.set("amount", item.amount)
        }
    }
}

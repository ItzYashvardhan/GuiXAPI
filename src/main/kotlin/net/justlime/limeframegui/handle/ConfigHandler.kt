package net.justlime.limeframegui.handle

import net.justlime.limeframegui.api.LimeFrameAPI
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
        var dataFolder = LimeFrameAPI.getPlugin().dataFolder

        fun loadItem(filename: String, path: String, dataFolder: File = Companion.dataFolder): GuiItem {
            val file = File(dataFolder, filename)
            val config = YamlConfiguration.loadConfiguration(file)
            val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

            val displayName = section.getString("name") ?: ""
            val material = Material.getMaterial(section.getString("material") ?: "STONE") ?: Material.STONE
            val lore = section.getStringList("lore")
            val glow = section.getBoolean("glow", false)
            val flags = section.getStringList("flags").mapNotNull { runCatching { ItemFlag.valueOf(it) }.getOrNull() }
            val customModelData = if (section.contains("model")) section.getInt("model") else null
            val amount = section.getInt("amount", 1)
            val skullTexture = section.getString("texture")
            return GuiItem(material, displayName, amount, lore, glow, flags, customModelData, skullTexture)
        }

        fun loadItems(filename: String, path: String, dataFolder: File = Companion.dataFolder): List<GuiItem> {
            val file = File(dataFolder, filename)
            val config = YamlConfiguration.loadConfiguration(file)
            val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

            return section.getKeys(false).mapNotNull { key ->
                section.getConfigurationSection(key)?.let {
                    loadItem(filename, "$path.$key", dataFolder)
                }
            }
        }

        fun loadInventory(filename: String, path: String, dataFolder: File = Companion.dataFolder): Inventory {
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
                val item = loadItem(filename, "$path.items.$key", dataFolder)
                inventory.setItem(slot, item.toItemStack())
            }

            return inventory
        }

        fun saveItem(filename: String, path: String, item: GuiItem, dataFolder: File = Companion.dataFolder): Boolean {
            val file = File(dataFolder, filename)
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val section = config.createSection(path)
            writeItemToSection(section, item)

            return runCatching {
                config.save(file)
            }.isSuccess
        }

        fun saveItems(filename: String, path: String, item: GuiItem, dataFolder: File = Companion.dataFolder): Boolean {
            val file = File(dataFolder, filename)
            val config = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

            val baseSection = config.createSection(path)
            val itemSection = baseSection.createSection("0") // Save single item as index "0"
            writeItemToSection(itemSection, item)

            return runCatching {
                config.save(file)
            }.isSuccess
        }

        fun saveInventory(filename: String, path: String, inventory: Inventory, inventoryTitle: String = "Inventory", dataFolder: File = Companion.dataFolder): Boolean {
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
            section.set("name", item.displayName)
            section.set("material", item.material.name)
            section.set("lore", item.lore)
            section.set("glow", item.glow)
            section.set("flags", item.flags.mapNotNull { it?.name })
            section.set("model", item.customModelData.toString())
            section.set("texture", item.skullTexture.toString())
            section.set("amount", item.amount)
        }
    }
}

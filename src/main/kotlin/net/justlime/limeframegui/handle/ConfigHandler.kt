package net.justlime.limeframegui.handle

import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.models.ConfigKeys
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import java.io.File

class ConfigHandler(private val filename: String, private val dataFolder: File = LimeFrameAPI.getPlugin().dataFolder) {

    companion object {
        var keys: ConfigKeys = ConfigKeys()

        fun setKeys(customizer: ConfigKeys.() -> Unit) {
            keys.customizer()
        }

        fun loadKeysFromFile(file: File = File(LimeFrameAPI.getPlugin().dataFolder, "gui-keys.yml")) {
            if (!file.exists()) return

            val config = YamlConfiguration.loadConfiguration(file)
            val section = config.getConfigurationSection("item-keys") ?: return

            keys = ConfigKeys(
                material = section.getString("material", keys.material)!!,
                name = section.getString("name", keys.name)!!,
                lore = section.getString("lore", keys.lore)!!,
                glow = section.getString("glow", keys.glow)!!,
                flags = section.getString("flags", keys.flags)!!,
                model = section.getString("model", keys.model)!!,
                amount = section.getString("amount", keys.amount)!!,
                texture = section.getString("texture", keys.texture)!!
            )
        }
    }

    fun test(){
        ConfigHandler.setKeys {
            material = "material"
            name = "name"
        }
    }

    private var config: YamlConfiguration = loadYaml()

    private fun loadYaml(): YamlConfiguration {
        val file = File(dataFolder, filename)
        return YamlConfiguration.loadConfiguration(file)
    }

    fun reload(): Boolean {
        val file = File(dataFolder, filename)
        return if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file)
            true
        } else false
    }

    fun loadItem(path: String): GuiItem {
        val section = config.getConfigurationSection(path)
            ?: throw IllegalArgumentException("Section '$path' not found in $filename")

        val displayName = section.getString(keys.name) ?: ""
        val material = Material.getMaterial(section.getString(keys.material) ?: "STONE") ?: Material.STONE
        val lore = section.getStringList(keys.lore)
        val glow = section.getBoolean(keys.glow, false)
        val flags = section.getStringList(keys.flags).mapNotNull { runCatching { ItemFlag.valueOf(it) }.getOrNull() }
        val customModelData = section.takeIf { it.contains(keys.model) }?.getInt(keys.model)
        val amount = section.getInt(keys.amount, 1)
        val skullTexture = section.getString(keys.texture)

        return GuiItem(material, displayName, amount, lore, glow, flags, customModelData, skullTexture)
    }

    fun loadItems(path: String): List<GuiItem> {
        val section = config.getConfigurationSection(path)
            ?: throw IllegalArgumentException("Section '$path' not found in $filename")

        return section.getKeys(false).mapNotNull { key ->
            section.getConfigurationSection(key)?.let {
                loadItem("$path.$key")
            }
        }
    }

    fun loadInventory(path: String): Inventory {
        val section = config.getConfigurationSection(path) ?: return Bukkit.createInventory(null, 9, "Inventory")

        val size = section.getInt("size", 9)
        val title = section.getString("title", "Inventory") ?: "Inventory"
        val inventory = Bukkit.createInventory(null, size, title)

        val itemsSection = section.getConfigurationSection("items") ?: return inventory

        for (key in itemsSection.getKeys(false)) {
            val slot = key.toIntOrNull() ?: continue
            val itemSection = itemsSection.getConfigurationSection(key) ?: continue
            val item = loadItem("$path.items.$key")
            inventory.setItem(slot, item.toItemStack())
        }

        return inventory
    }

    fun saveItem(path: String, item: GuiItem): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

        val section = configToSave.createSection(path)
        writeItemToSection(section, item)

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun saveItems(path: String, item: GuiItem): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

        val baseSection = configToSave.createSection(path)
        val itemSection = baseSection.createSection("0")
        writeItemToSection(itemSection, item)

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun saveInventory(path: String, inventory: Inventory, inventoryTitle: String = "Inventory"): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

        val section = configToSave.createSection(path)
        section.set("size", inventory.size)
        section.set("title", inventoryTitle)

        val itemsSection = section.createSection("items")
        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue
            val guiItem = item.toGuiItem()
            val itemSection = itemsSection.createSection(i.toString())
            writeItemToSection(itemSection, guiItem)
        }

        return runCatching { configToSave.save(file) }.isSuccess
    }

    private fun writeItemToSection(section: ConfigurationSection, item: GuiItem) {
        section.set(keys.name, item.displayName)
        section.set(keys.material, item.material.name)
        section.set(keys.lore, item.lore)
        section.set(keys.glow, item.glow)
        section.set(keys.flags, item.flags.mapNotNull { it?.name })
        section.set(keys.model, item.customModelData)
        section.set(keys.texture, item.skullTexture)
        section.set(keys.amount, item.amount)
    }
}

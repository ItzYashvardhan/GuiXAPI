package net.justlime.limeframegui.impl

import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.utilities.FrameConverter
import net.justlime.limeframegui.models.ConfigKeys
import net.justlime.limeframegui.models.GUISetting
import net.justlime.limeframegui.models.GuiItem
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.io.File

class ConfigHandler(private val filename: String, private val dataFolder: File = LimeFrameAPI.getPlugin().dataFolder) {

    companion object {
        var keys: ConfigKeys = ConfigKeys()

        fun setKeys(customizer: ConfigKeys.() -> Unit) {
            keys.customizer()
        }

    }

    private var config: YamlConfiguration = loadYaml()

    fun reload(): Boolean {
        val file = File(dataFolder, filename)
        return if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file)
            true
        } else false
    }

    fun saveItem(path: String, item: GuiItem): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val section = configToSave.createSection(path)
        writeItemToSection(section, item)

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun saveItemBase64(path: String, itemStack: ItemStack): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val section = configToSave.createSection(path)
        val encodedItem = FrameConverter.serializeItemStack(itemStack)
        section.set("data", encodedItem)

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun loadItem(path: String): GuiItem {
        val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

        return with(section) {
            GuiItem(
                material = Material.getMaterial(getString(keys.material) ?: "STONE") ?: Material.STONE,
                displayName = getString(keys.name) ?: "",
                lore = getStringList(keys.lore),
                glow = getBoolean(keys.glow, false),
                flags = getStringList(keys.flags).mapNotNull { runCatching { ItemFlag.valueOf(it) }.getOrNull() },
                customModelData = takeIf { contains(keys.model) }?.getInt(keys.model),
                amount = getInt(keys.amount, 1),
                skullTexture = getString(keys.texture),
                slot = getString(keys.slot)?.toIntOrNull(),
                slotList = getIntegerList(keys.slotList)
            )
        }
    }

    fun loadItemBase64(path: String): ItemStack {
        val encodedItem = config.getString(path) ?: return ItemStack(Material.AIR)
        return FrameConverter.deserializeItemStack(encodedItem) ?: ItemStack(Material.AIR)
    }

    fun saveItems(path: String, sectionName: String, items: List<GuiItem>): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val baseSection = configToSave.createSection(path)
        baseSection.set("title", sectionName)

        val itemsSection = baseSection.createSection("items")
        val itemMap = mutableMapOf<GuiItem, MutableList<Int>>()

        for ((index, item) in items.withIndex()) {
            itemMap.computeIfAbsent(item) { mutableListOf() }.add(item.slot ?: index)
        }

        for ((item, slots) in itemMap) {
            val key = slots.first().toString()
            val itemSection = itemsSection.createSection(key)

            val cleanItem = item.copy(slot = null, slotList = mutableListOf())
            writeItemToSection(itemSection, cleanItem)

            if (slots.size > 1) {
                itemSection.set(keys.slotList, slots)
            }
        }

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun saveItemsBase64(path: String, sectionName: String, item: ItemStack): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val section = configToSave.createSection(path)
        val encodedItem = FrameConverter.serializeItemStack(item)
        section.set("data", encodedItem)
        section.set("title", sectionName)

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun loadItems(path: String): List<GuiItem> {
        val section = config.getConfigurationSection(path) ?: throw IllegalArgumentException("Section '$path' not found in $filename")

        return section.getKeys(false).mapNotNull { key ->
            section.getConfigurationSection(key)?.let {
                loadItem("$path.$key")
            }
        }
    }

    fun loadItemsBase64(path: String): List<ItemStack> {
        val encodedItems = config.getString(path) ?: return emptyList()
        return FrameConverter.deserializeItemStackList(encodedItems) ?: emptyList()
    }

    fun loadInventory(path: String): Inventory {
        val section = config.getConfigurationSection(path) ?: return Bukkit.createInventory(null, 9, "Inventory")

        val size = section.getInt("rows", 9)
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

    fun loadInventorySetting(path: String): GUISetting {
        val section = config.getConfigurationSection(path) ?: return GUISetting("LimeFrameGUI", 6)
        val title = section.getString("title", "GUI") ?: "LimeFrameGUI"
        val rows = section.getInt("rows", 6)
        return GUISetting(title, rows)
    }

    fun loadInventoryBase64(path: String, defaultTitle: String = "Inventory"): Inventory {
        val encodedInventory = config.getString("$path.data") ?: return Bukkit.createInventory(null, 9, defaultTitle)
        val title = config.getString("$path.title") ?: defaultTitle
        return FrameConverter.deserializeInventory(encodedInventory) ?: Bukkit.createInventory(null, 9, title)
    }


    fun saveInventoryBase64(path: String, inventory: Inventory, title: String = "Inventory"): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val encodedInventory = FrameConverter.serializeInventory(inventory)
        configToSave.set("$path.data", encodedInventory)
        configToSave.set("$path.title", title)
        configToSave.set("$path.rows", inventory.size / 9)

        return runCatching { configToSave.save(file) }.isSuccess
    }


    fun saveInventory(path: String, inventory: Inventory, inventoryTitle: String = "Inventory"): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val section = configToSave.createSection(path)
        section.set("rows", inventory.size.div(9))
        section.set("title", inventoryTitle)

        val itemsSection = section.createSection("items")
        val itemMap = mutableMapOf<GuiItem, MutableList<Int>>()

        for (i in 0 until inventory.size) {
            val itemStack = inventory.getItem(i) ?: continue
            val guiItem = itemStack.toGuiItem()
            itemMap.computeIfAbsent(guiItem) { mutableListOf() }.add(i)
        }

        for ((item, slots) in itemMap) {
            val key = slots.first().toString()
            val itemSection = itemsSection.createSection(key)

            // Clear slot info before saving to avoid redundant fields
            val cleanItem = item.copy(slot = null, slotList = mutableListOf())
            writeItemToSection(itemSection, cleanItem)

            if (slots.size > 1) {
                itemSection.set(keys.slotList, slots)
            }
        }

        return runCatching { configToSave.save(file) }.isSuccess
    }

    fun saveInventorySetting(path: String, setting: GUISetting): Boolean {
        val file = File(dataFolder, filename)
        val configToSave = loadOrCreateYaml()

        val section = configToSave.createSection(path)
        section.set("title", setting.title)
        section.set("rows", setting.rows)

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
        item.slot?.let { section.set(keys.slot, it) }
        if (item.slotList.isNotEmpty()) section.set(keys.slotList, item.slotList)
    }

    private fun loadYaml(): YamlConfiguration {
        val file = File(dataFolder, filename)
        return YamlConfiguration.loadConfiguration(file)
    }

    private fun loadOrCreateYaml(): YamlConfiguration {
        val file = File(dataFolder, filename)
        return if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()
    }

}
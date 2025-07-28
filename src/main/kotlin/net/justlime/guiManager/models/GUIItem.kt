package net.justlime.guiManager.models

import net.justlime.guiManager.utilities.SkullProfileCache
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

data class GuiItem(
    var material: Material,
    var displayName: String = "",
    var amount: Int = 1,
    var lore: List<String> = emptyList(),
    var glow: Boolean = false,
    var flags: Collection<ItemFlag?> = emptyList(),
    var customModelData: Int? = null,
    var skullTexture: String? = null,
    val slot: Int = 0,
) {

    companion object {
        fun getItem(inventory: Inventory, slot: Int): GuiItem {
            val itemStack = inventory.getItem(slot)
            return GuiItem(
                displayName = itemStack?.itemMeta?.displayName ?: "",
                material = itemStack?.type ?: Material.AIR,
                lore = itemStack?.itemMeta?.lore ?: emptyList(),
                glow = false,
                flags = itemStack?.itemMeta?.itemFlags ?: emptyList(),
                customModelData = itemStack?.itemMeta?.customModelData,
                amount = itemStack?.amount ?: 0
            )
        }

        fun getItem(item: ItemStack): GuiItem {
            return GuiItem(
                displayName = item.itemMeta?.displayName ?: "",
                material = item.type,
                lore = item.itemMeta?.lore ?: emptyList(),
                glow = false,
                flags = item.itemMeta?.itemFlags ?: emptyList(),
                customModelData = if (item.itemMeta?.hasCustomModelData() == true) item.itemMeta?.customModelData else null,
                amount = item.amount
            )
        }

        fun getItem(inventory: Inventory, displayName: String): List<GuiItem> {
            val items = mutableListOf<GuiItem>()
            for (i in 0 until inventory.size) {
                val itemStack = inventory.getItem(i)
                if (itemStack != null && itemStack.itemMeta?.displayName == displayName) {
                    items.add(
                        GuiItem(
                            displayName = itemStack.itemMeta?.displayName ?: "",
                            material = itemStack.type,
                            lore = itemStack.itemMeta?.lore ?: emptyList(),
                            glow = false,
                            flags = itemStack.itemMeta?.itemFlags ?: emptyList(),
                            customModelData = itemStack.itemMeta?.customModelData,
                            amount = itemStack.amount
                        )
                    )
                }
            }
            return items
        }

        fun getItem(inventory: Inventory, material: Material): List<GuiItem> {
            val items = mutableListOf<GuiItem>()
            for (i in 0 until inventory.size) {
                val itemStack = inventory.getItem(i)
                if (itemStack != null && itemStack.type == material) {
                    items.add(
                        GuiItem(
                            displayName = itemStack.itemMeta?.displayName ?: "",
                            material = itemStack.type,
                            lore = itemStack.itemMeta?.lore ?: emptyList(),
                            glow = false,
                            flags = itemStack.itemMeta?.itemFlags ?: emptyList(),
                            customModelData = itemStack.itemMeta?.customModelData,
                            amount = itemStack.amount
                        )
                    )
                }
            }
            return items
        }

        fun air(): GuiItem {
            return GuiItem(
                displayName = "", material = Material.AIR, lore = emptyList(), glow = false, flags = emptyList(), customModelData = null, amount = 1
            )
        }
    }

    fun toItemStack(): ItemStack {
        val item = if (material == Material.PLAYER_HEAD && !skullTexture.isNullOrEmpty()) {
            ItemStack(Material.PLAYER_HEAD)
        } else {
            ItemStack(material, amount)
        }

        val meta = item.itemMeta ?: return item

        // If it's a head, apply profile from cache
        if (meta is SkullMeta && !skullTexture.isNullOrEmpty()) {
            meta.ownerProfile = SkullProfileCache.getProfile(skullTexture!!)
        }

        // Apply custom settings
        meta.setDisplayName(displayName)
        if (lore.isNotEmpty()) meta.lore = lore

        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        if (flags.isNotEmpty()) meta.addItemFlags(*flags.filterNotNull().toTypedArray())
        if (customModelData != null) meta.setCustomModelData(customModelData)

        item.itemMeta = meta
        return item
    }
}

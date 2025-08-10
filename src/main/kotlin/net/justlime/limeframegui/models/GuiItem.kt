package net.justlime.limeframegui.models

import com.google.common.collect.Multimap
import net.justlime.limeframegui.utilities.FrameColor
import net.justlime.limeframegui.utilities.SkullProfileCache
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.SkullMeta

/**
 * Represents an item in a LimeFrame GUI.
 *
 * @param material The Bukkit material type of the item.
 * @param name The display name of the item.
 * @param amount The number of items in the stack (default: 1).
 * @param lore A list of lore lines displayed under the item name.
 * @param glow Whether the item should visually glow.
 * @param flags Item flags to hide or show specific properties in tooltips.
 * @param customModelData Custom model data ID for resource pack integration.
 * @param texture Base64-encoded texture string for custom player heads (only works with PLAYER_HEAD).
 * @param enchantments Map of enchantments and their levels for this item.
 * @param unbreakable Whether the item is unbreakable (no durability loss).
 * @param damage The current damage/durability value of the item (0 = new).
 * @param hideToolTip Used this to completely hide details of item on hover.
 * @param attributeModifiers Attribute modifiers applied to the item (e.g., extra damage, speed boost).
 * @param slot Single slot index to place this item into.
 * @param slotList Multiple slot indices to place the same item in several slots.
 * @param placeholderPlayer Player object used for applying placeholders in displayName and lore.
 * @param placeholderOfflinePlayer OfflinePlayer object used for applying placeholders in displayName and lore.
 * @param smallCaps Whether to apply small caps formatting to the display name and lore.
 * @param onClickBlock Event callback invoked when this item is clicked in the GUI (TODO).
 */
data class GuiItem(
    // Appearance
    var material: Material,
    var name: String? = "",
    var amount: Int = 1,
    var lore: List<String> = mutableListOf(),
    var glow: Boolean = false,
    var flags: Collection<ItemFlag?> = emptyList(),
    var customModelData: Int? = null,
    var texture: String? = null,

    // Functional Meta
    var enchantments: Map<Enchantment, Int> = emptyMap(),
    var unbreakable: Boolean = false,
    var damage: Int? = null, // Durability value
    var hideToolTip: Boolean = false,
    var attributeModifiers: Multimap<Attribute, AttributeModifier>? = null,

    // Placement
    var slot: Int? = null,
    var slotList: List<Int> = mutableListOf(),

    // Placeholder & Dynamic Content
    var customPlaceholder: Map<String, String>? = null,
    var placeholderPlayer: Player? = null,
    var placeholderOfflinePlayer: OfflinePlayer? = null,
    var smallCaps: Boolean? = null,

    // Click Handling
    var onClickBlock: (InventoryClickEvent) -> Unit = {}, //TODO
) {

    companion object {
        fun air(): GuiItem {
            return GuiItem(name = "", material = Material.AIR)
        }
    }

    fun toItemStack(): ItemStack {
        val item = if (material.name.contains("PLAYER_HEAD", ignoreCase = true) && !texture.isNullOrEmpty()) {
            ItemStack(Material.PLAYER_HEAD, amount)
        } else {
            ItemStack(material, amount)
        }

        val meta = item.itemMeta ?: return item

        // Apply skull texture
        if (meta is SkullMeta && !texture.isNullOrEmpty()) {
            try {
                meta.ownerProfile = SkullProfileCache.getProfile(texture!!)
            } catch (_: Throwable) {
                try {
                    val profileField = meta.javaClass.getDeclaredField("profile")
                    profileField.isAccessible = true
                    profileField.set(meta, SkullProfileCache.getProfile(texture!!))
                } catch (_: Throwable) {
                }
            }
        }

        // Name & lore
        if (hideToolTip) {
            try { meta.isHideTooltip = true } catch (_: Exception) {}
        }
        meta.setDisplayName(name?.let { FrameColor.applyColor(it, placeholderPlayer, placeholderOfflinePlayer,smallCaps, customPlaceholder ) })
        if (lore.isNotEmpty()) {
            meta.lore = try {
                FrameColor.applyColor(lore, placeholderPlayer, placeholderOfflinePlayer,smallCaps, customPlaceholder)
            } catch (_: Throwable) {
                lore
            }
        }

        // Glow
        try {
            meta.setEnchantmentGlintOverride(glow)
        } catch (_: Throwable) {
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
        }

        // Item flags
        if (flags.isNotEmpty()) {
            try {
                meta.addItemFlags(*flags.filterNotNull().toTypedArray())
            } catch (_: IllegalArgumentException) { }
        }


        // Custom model data
        customModelData?.let {
            try { meta.setCustomModelData(it) } catch (_: Throwable) {}
        }

        // Enchantments
        if (enchantments.isNotEmpty()) {
            enchantments.forEach { (enchantment, lvl) -> meta.addEnchant(enchantment, lvl, true) }
        }

        // Unbreakable
        try { meta.isUnbreakable = unbreakable } catch (_: Throwable) {}

        // Damage (durability)
        damage?.let {
            if (meta is Damageable) {
                try { meta.damage = it } catch (_: Throwable) {}
            }
        }

        // Attribute modifiers
        attributeModifiers?.let {
            try { meta.attributeModifiers = it } catch (_: Throwable) {}
        }

        item.itemMeta = meta
        return item
    }
}

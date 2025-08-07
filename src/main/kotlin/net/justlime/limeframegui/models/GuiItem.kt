package net.justlime.limeframegui.models

import net.justlime.limeframegui.utilities.FrameColor
import net.justlime.limeframegui.utilities.SkullProfileCache
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

/**
 * @param flags Used this field add custom flags
 * @param skullTexture Supported base64 url for skull texture
 * @param slot Pass a single slot id
 * @param slotList Pass a list of slot id
 * @param glow This field is responsible to toggle glow able items
 * @param placeholderPlayer This fields is used to apply placeholder to item name and lore for given player
 */
data class GuiItem(
    var material: Material,
    var displayName: String? = "",
    var amount: Int = 1,
    var lore: MutableList<String> = mutableListOf(),
    var glow: Boolean = false,
    var flags: Collection<ItemFlag?> = emptyList(),
    var customModelData: Int? = null,
    var skullTexture: String? = null,
    var slot: Int? = null,
    var slotList: MutableList<Int> = mutableListOf(), //to store single item in many slots
    var onClickBlock: (InventoryClickEvent) -> Unit = {}, //TODO "To Store Click data"
    var placeholderPlayer: Player? = null

) {

    companion object {
        //LET AN INVISIBLE ITEM BE CREATED AND ASK YOUR FRIEND TO FIND ALSO ADD 100 PAGE IF THEY FOUND AND CLICK ON IT LET IT SAY UHH THANK YOU SO MUCH NOW FIND ME AGAIN xd
        fun air(): GuiItem {
            return GuiItem(displayName = "", material = Material.AIR)
        }
    }

    //THIS THING RIGHT THERE IS RESPONSIBLE FOR YOUR PRECIOUS ITEM FOUND IN BLOCKY CHEST
    fun toItemStack(): ItemStack {
        val item = if (material.name.contains("PLAYER_HEAD", ignoreCase = true) && !skullTexture.isNullOrEmpty()) {
            ItemStack(Material.matchMaterial("PLAYER_HEAD") ?: Material.matchMaterial("SKULL_ITEM")!!, amount)
        } else {
            ItemStack(material, amount)
        }

        val meta = item.itemMeta ?: return item

        // If it's a head, apply profile from cache (safe)
        if (meta is SkullMeta && !skullTexture.isNullOrEmpty()) {
            try {
                // Paper API 1.18+
                meta.ownerProfile = SkullProfileCache.getProfile(skullTexture!!)
            } catch (_: Throwable) {
                // Fallback for 1.8.8 or unsupported versions
                try {
                    val skullMetaClass = meta.javaClass
                    val profileField = skullMetaClass.getDeclaredField("profile")
                    profileField.isAccessible = true
                    profileField.set(meta, SkullProfileCache.getProfile(skullTexture!!))
                } catch (_: Throwable) {  }
            }
        }

        // Display name
        meta.setDisplayName(displayName?.let { FrameColor.applyColor(it,placeholderPlayer) })

        // Lore
        if (lore.isNotEmpty()) {
            try {
                meta.lore = FrameColor.applyColor(lore,placeholderPlayer)
            } catch (_: Throwable) {
                meta.lore = lore
            }
        }

        // Enchantment glint override (1.20+)
        try {
            meta.setEnchantmentGlintOverride(glow)
        } catch (_: Throwable) {
            if (glow) {
                try {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                } catch (_: Throwable) {
                    glow = false
                }
            }
        }

        // Item flags
        try {
            if (flags.isNotEmpty()) {
                meta.addItemFlags(*flags.filterNotNull().toTypedArray())
            }
        } catch (_: Throwable) {
        }

        // CustomModelData (1.14+)
        try {
            if (customModelData != null) meta.setCustomModelData(customModelData)
        } catch (_: Throwable) {
        }

        item.itemMeta = meta
        return item
    }
}


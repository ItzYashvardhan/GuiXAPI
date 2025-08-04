package net.justlime.limeframegui.models

import net.justlime.limeframegui.utilities.FrameColor
import net.justlime.limeframegui.utilities.SkullProfileCache
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

data class GuiItem(var material: Material,
                   var displayName: String? = "",
                   var amount: Int = 1,
                   var lore: MutableList<String> = mutableListOf(),
                   var glow: Boolean = false,
                   var flags: Collection<ItemFlag?> = emptyList(),
                   var customModelData: Int? = null,
                   var skullTexture: String? = null,
                   var slot: Int? = null, //Store object slot
                   var slotList: MutableList<Int> = mutableListOf(), //to store single item in many slots
                   var onClickBlock: (InventoryClickEvent) -> Unit = {}, //TODO "To Store Click data can be used later like template"
                   var key: String? = null
) {

    companion object {
        //LET AN INVISIBLE ITEM BE CREATED AND ASK YOUR FRIEND TO FIND ALSO ADD 100 PAGE IF THEY FOUND AND CLICK ON IT LET IT SAY UHH THANK YOU SO MUCH NOW FIND ME AGAIN xd
        fun air(): GuiItem {
            return GuiItem(displayName = "", material = Material.AIR)
        }
    }

    //THIS THING RIGHT THERE IS RESPONSIBLE FOR YOUR PRECIOUS ITEM FOUND IN BLOCKY CHEST
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
        if (displayName!=null) meta.setDisplayName(FrameColor.applyColor(displayName!!)) else meta.setDisplayName(null)
        if (lore.isNotEmpty()) meta.lore = FrameColor.applyColor(lore)

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

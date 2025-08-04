package net.justlime.limeframegui.utilities

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.justlime.limeframegui.models.GuiItem
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

fun ItemStack.toGuiItem(): GuiItem {
    val meta = this.itemMeta

    val displayName = meta?.displayName ?: this.type.name
    val lore = meta?.lore ?: emptyList()
    val glow = meta?.hasEnchants() == true && meta.itemFlags.contains(ItemFlag.HIDE_ENCHANTS)
    val flags = meta?.itemFlags ?: emptySet()
    val customModelData = if (meta?.hasCustomModelData() == true) meta.customModelData else null

    // If this is a skull, get texture
    var skullTexture: String? = null
    if (this.type == Material.PLAYER_HEAD) {
        skullTexture = SkullUtils.getTextureFromSkull(this)
    }

    return GuiItem(
        displayName = displayName,
        material = type,
        amount = amount,
        lore = lore.toMutableList(),
        glow = glow,
        flags = flags,
        customModelData = customModelData,
        skullTexture = skullTexture
    )
}

fun Inventory.addItem(item: GuiItem): List<GuiItem> {
    val remaining = this.addItem(item.toItemStack()) // returns Map<Int, ItemStack>
    return remaining.values.map { it.toGuiItem() }   // Convert remaining ItemStacks to GuiItems
}

fun Inventory.addItems(item: List<GuiItem>): List<GuiItem> {
    val remainingItems = mutableListOf<GuiItem>()
    item.forEach { guiItem ->
        val remaining = this.addItem(guiItem.toItemStack())
        remainingItems.addAll(remaining.values.map { it.toGuiItem() })
    }
    return remainingItems
}

fun Inventory.setItem(index: Int, item: GuiItem): Boolean {
    val stack = item.toItemStack()
    if (index in 0 until this.size) {
        this.setItem(index, stack)
        return true
    }
    return false
}

fun Inventory.remove(item: GuiItem): Boolean {
    val stackToRemove = item.toItemStack()
    val result = this.removeItem(stackToRemove)
    return result.isEmpty() // If nothing is left, it means removal was successful
}

fun createSkullWithTexture(texture: String): ItemStack {
    val skull = ItemStack(Material.PLAYER_HEAD)
    val meta = skull.itemMeta as SkullMeta

    try {
        val profile = GameProfile(UUID.randomUUID(), null)
        val properties = profile.properties
        properties.put("textures", Property("textures", texture))

        val metaClass = meta.javaClass
        val profileField = metaClass.getDeclaredField("profile")
        profileField.isAccessible = true
        profileField.set(meta, profile)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    skull.itemMeta = meta
    return skull
}

fun Pair<Int, Int>.toSlot(totalRows: Int = 6): Int {
    var (row, col) = this

    if (row == 0 && col == 0) return -1

    row = if (row < 0) totalRows + row + 1 else row
    col = if (col < 0) 9 + col + 1 else col

    return (row - 1) * 9 + (col - 1)
}

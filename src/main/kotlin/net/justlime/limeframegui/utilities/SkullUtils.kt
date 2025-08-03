/**
 * Code Reserved to DeluxeMenu*
 * github: https://github.com/HelpChat/DeluxeMenus/blob/main/src/main/java/com/extendedclip/deluxemenus/utils/SkullUtils.java
 *
 * */

package net.justlime.limeframegui.utilities

import com.google.common.primitives.Ints
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.lang.reflect.Field
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object SkullUtils {

    val playerHead = ItemStack(Material.PLAYER_HEAD)

    object VersionHelper {
        const val V1_18_1: Int = 1181
        const val V1_12: Int = 1120

        val CURRENT_VERSION: Int = getCurrentVersion()
        val HAS_PLAYER_PROFILES: Boolean = CURRENT_VERSION >= V1_18_1

        val IS_SKULL_OWNER_LEGACY: Boolean = CURRENT_VERSION <= V1_12

        private fun getCurrentVersion(): Int {
            // No need to cache since will only run once
            val matcher: Matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion())

            val stringBuilder = StringBuilder()
            if (matcher.find()) {
                stringBuilder.append(matcher.group("version").replace(".", ""))
                val patch: String? = matcher.group("patch")
                if (patch == null) stringBuilder.append("0")
                else stringBuilder.append(patch.replace(".", ""))
            }

            val version = Ints.tryParse(stringBuilder.toString())

            // Should never fail
            if (version == null) throw RuntimeException("Could not retrieve server version!")

            return version
        }

    }

    private val GSON = Gson()

    /**
     * Helper method to get the encoded bytes for a full MC Texture
     *
     * @param url the url of the texture
     * @return fully encoded texture url
     */
    fun getEncoded(url: String): String {
        val encodedData: ByteArray =
            Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "https://textures.minecraft.net/texture/$url").toByteArray())
        return String(encodedData)
    }

    /**
     * Get the skull from a base64 encoded texture url
     *
     * @param base64Url base64 encoded url to use
     * @return skull
     */
    fun getSkullByBase64EncodedTextureUrl(base64Url: String): ItemStack {
        val head: ItemStack = getHead()
        if (base64Url.isEmpty()) {
            return head
        }

        val headMeta = head.itemMeta as SkullMeta?
        if (headMeta == null) {
            return head
        }

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            val profile = getPlayerProfile(base64Url)
            headMeta.ownerProfile = profile
            head.itemMeta = headMeta
            return head
        }

        val profile = getGameProfile(base64Url)
        val profileField: Field
        try {
            profileField = headMeta.javaClass.getDeclaredField("profile")
            profileField.setAccessible(true)
            profileField.set(headMeta, profile)
        } catch (_: NoSuchFieldException) {
            println(
                "Failed to get head item from base64 texture url"
            )
        } catch (_: IllegalArgumentException) {
            println(
                "Failed to get head item from base64 texture url"
            )
        } catch (_: IllegalAccessException) {
            println(
                "Failed to get head item from base64 texture url"
            )
        }
        head.itemMeta = headMeta
        return head
    }

    fun getTextureFromSkull(item: ItemStack): String? {
        if (item.itemMeta !is SkullMeta) return null
        val meta = item.itemMeta as SkullMeta?

        if (VersionHelper.HAS_PLAYER_PROFILES) {
            val profile = meta!!.ownerProfile
            if (profile == null) return null

            val url: URL? = profile.textures.skin
            if (url == null) return null

            return url.toString().removePrefix("https://textures.minecraft.net/texture/")
        }

        val profile: GameProfile
        try {
            val profileField: Field = meta!!.javaClass.getDeclaredField("profile")
            profileField.setAccessible(true)
            profile = profileField.get(meta) as GameProfile
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            return null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return null
        }

        for (property in profile.properties.get("textures")) {
            if (property.name.equals("textures")) {
                return decodeSkinUrl(property.value)
            }
        }
        return null
    }

    /**
     * Get the skull from a player name
     *
     * @param playerName the player name to use
     * @return skull
     */
    fun getSkullByName(playerName: String): ItemStack {
        val head: ItemStack = getHead().clone()
        if (playerName.isEmpty()) {
            return head
        }

        val headMeta = head.itemMeta as SkullMeta?
        if (headMeta == null) {
            return head
        }

        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)

        if (VersionHelper.HAS_PLAYER_PROFILES && offlinePlayer.playerProfile.textures.isEmpty) {
            // updates the Player Profile and populates textures for offline players - for some reason this doesn't populate when getting the Profile first time
            headMeta.ownerProfile = offlinePlayer.playerProfile.update().join()
        } else if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            headMeta.owningPlayer = offlinePlayer
        } else {
            headMeta.owner = offlinePlayer.name
        }

        head.itemMeta = headMeta
        return head
    }

    fun getSkullOwner(skull: ItemStack?): String? {
        if (skull == null || skull.itemMeta !is SkullMeta) return null
        val meta = skull.itemMeta as SkullMeta?

        if (!VersionHelper.IS_SKULL_OWNER_LEGACY) {
            if (meta!!.owningPlayer == null) return null
            return meta.owningPlayer!!.name
        }

        return meta?.owner
    }

    private fun getGameProfile(base64Url: String): GameProfile {
        val profile = GameProfile(UUID.randomUUID(), "")
        profile.properties.put("textures", Property("textures", base64Url))
        return profile
    }

    /**
     * Create a player profile object
     * Player profile was introduced in 1.18.1+
     *
     * @param base64Url the base64 encoded texture URL to use
     * @return player profile
     */
    private fun getPlayerProfile(texture: String): PlayerProfile {
        val profile = Bukkit.createPlayerProfile(UUID.randomUUID())

        val skinUrl = if (texture.startsWith("http")) {
            texture
        } else if (!texture.startsWith("{")) {
            // Assume it's raw texture ID
            "https://textures.minecraft.net/texture/$texture"
        } else {
            decodeSkinUrl(texture) ?: ""
        }

        if (skinUrl.isEmpty()) return profile

        try {
            profile.textures.skin = URL(skinUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return profile
    }

    /**
     * Decode a base64 string and extract the url of the skin. Example:
     * <br></br>
     * - Base64: `eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNlYjE3MDhkNTQwNGVmMzI2MTAzZTdiNjA1NTljOTE3OGYzZGNlNzI5MDA3YWM5YTBiNDk4YmRlYmU0NjEwNyJ9fX0=`
     * <br></br>
     * - JSON: `{"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107"}}}`
     * <br></br>
     * - Result: `http://textures.minecraft.net/texture/dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107`
     * <br></br>
     * Credit: [iGabyTM](https://github.com/TriumphTeam/triumph-gui/pull/104/files#diff-ef6f3ffdac8e5f722e2e9121be8003b26d087c2d7871ca43d31b65c7565b0c1fR92)
     *
     * @param base64Texture the texture
     * @return the url of the texture if found, otherwise `null`
     */
    fun decodeSkinUrl(base64Texture: String): String? {
        if (!Base64.getDecoder().decode(base64Texture).isNotEmpty()) {
            return null
        }
        val decoded = String(Base64.getDecoder().decode(base64Texture))
        if (!decoded.trim().startsWith("{")) {
            return null // Not JSON
        }
        val decodedObject = GSON.fromJson(decoded, JsonObject::class.java)
        val textures = decodedObject["textures"]?.asJsonObject ?: return null
        val skin = textures["SKIN"]?.asJsonObject ?: return null
        return skin["url"]?.asString
    }

    fun getHead(): ItemStack {
        return playerHead
    }
}
/**
 * Code Brought You by DeluxeMenu :)*
 * github: https://github.com/HelpChat/DeluxeMenus/blob/main/src/main/java/com/extendedclip/deluxemenus/utils/SkullUtils.java
 *
 * */

package net.justlime.limeframegui.utilities

import com.google.common.primitives.Ints
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object SkullUtils {

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

            // Should never fail
            val version = Ints.tryParse(stringBuilder.toString()) ?: throw RuntimeException("Could not retrieve server version!")


            return version
        }

    }

    private val GSON = Gson()

    fun getTextureFromSkull(item: ItemStack): String? {
        val meta = item.itemMeta as? SkullMeta ?: return null

        // Modern versions (1.18+)
        if (VersionHelper.HAS_PLAYER_PROFILES) {
            val profile = meta.ownerProfile ?: return null
            val url = profile.textures.skin ?: return null
            return url.toString().removePrefix("https://textures.minecraft.net/texture/")
        }

        // Legacy versions (1.8–1.17)
        val profile = try {
            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.get(meta) as? GameProfile
        } catch (e: Exception) {
            println(e.message)
            null
        } ?: return null

        val property = profile.properties.get("textures").firstOrNull() ?: return null
        return decodeSkinUrl(property.value)
    }

    /**
     * Create a player profile object from a texture identifier.
     * Player profile was introduced in 1.18.1+
     *
     * This method correctly handles texture identifiers that are:
     * 1. A raw texture ID (e.g., "dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107")
     * 2. A full texture URL (e.g., "http://textures.minecraft.net/texture/...")
     * 3. A Base64-encoded texture value.
     *
     * @param texture The texture identifier.
     * @return player profile
     */
    fun createProfileFromTexture(texture: String): PlayerProfile {
        val profile = Bukkit.createPlayerProfile(UUID.randomUUID())
        val textures = profile.textures

        val skinUrl = if (texture.startsWith("http")) {
            texture
        } else {
            // Try to decode as a Base64 texture. If it fails (returns null),
            // then assume it's a raw texture ID.
            decodeSkinUrl(texture) ?: "https://textures.minecraft.net/texture/$texture"
        }

        if (skinUrl.isEmpty()) return profile

        runCatching {
            textures.skin = URL(skinUrl)
        }.onFailure {
            Bukkit.getLogger().warning("[LimeFrameGUI] Could not set skull skin from a malformed URL: $skinUrl. Error: ${it.message}")
        }
        profile.setTextures(textures)
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
        return try {
            val decodedBytes = Base64.getDecoder().decode(base64Texture)
            if (decodedBytes.isEmpty()) {
                return null
            }

            val decodedJson = String(decodedBytes)
            if (!decodedJson.trim().startsWith("{")) {
                return null // Decoded content is not JSON
            }

            val decodedObject = GSON.fromJson(decodedJson, JsonObject::class.java)
            val textures = decodedObject["textures"]?.asJsonObject ?: return null
            val skin = textures["SKIN"]?.asJsonObject ?: return null
            skin["url"]?.asString
        } catch (e: Exception) { // Catches IllegalArgumentException (invalid base64) and JsonParseException
            null
        }
    }
}
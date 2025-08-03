package net.justlime.limeframegui.utilities

import org.bukkit.profile.PlayerProfile

object SkullProfileCache {
    private val cache = mutableMapOf<String, PlayerProfile>()

    /**
     * Retrieves a PlayerProfile from the cache or creates it if it doesn't exist.
     * This function safely handles various texture formats (raw ID, full URL, Base64)
     * by delegating the creation logic to SkullUtils.
     *
     * @param texture The texture identifier.
     * @return A cached or newly created PlayerProfile.
     */
    fun getProfile(texture: String): PlayerProfile {
        return cache.getOrPut(texture) {
            SkullUtils.createProfileFromTexture(texture)
        }
    }
}
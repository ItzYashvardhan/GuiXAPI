package net.justlime.limeframegui.utilities

import org.bukkit.Bukkit
import org.bukkit.profile.PlayerProfile
import java.net.URL
import java.util.*

object SkullProfileCache {
    private val cache = mutableMapOf<String, PlayerProfile>()

    fun getProfile(textureUrl: String): PlayerProfile {
        return cache.getOrPut(textureUrl) {
            val profile = Bukkit.createPlayerProfile(UUID.randomUUID())
            profile.textures.skin = URL(textureUrl)
            profile
        }
    }
}
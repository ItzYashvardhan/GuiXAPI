package net.justlime.limeframegui.adapter

import org.bukkit.Bukkit

object VersionAdapterLoader {
    fun load(): VersionAdapter {
        val versionString = Bukkit.getBukkitVersion().substringBefore('-') // e.g. "1.21.1"
        val parts = versionString.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: error("Invalid version: $versionString")
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val className = when {
            major < 1 || (major == 1 && minor < 16) -> "net.justlime.legacy.VersionAdapter_Legacy"
            major == 1 && minor == 16 -> "net.justlime.v1_16.VersionAdapter_v1_16"
            major > 1 || (minor >= 21) -> "net.justlime.v1_21.VersionAdapter_v1_21"
            else -> error("Unsupported version: $versionString")
        }

        return Class.forName(className).getDeclaredConstructor().newInstance() as VersionAdapter
    }
}

package net.justlime.limeframegui.utilities

import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.regex.Pattern

object VersionHandler {

    // Pre-compile the regex pattern for parsing the server version for efficiency.
    private val SERVER_VERSION_PATTERN = Pattern.compile("\\(MC: (\\d+\\.\\d+(\\.\\d+)?)\\)")

    /**
     * Gets the native Minecraft version of the server (e.g., "1.20.1").
     * It parses the result of Bukkit.getVersion().
     *
     * @return The server's Minecraft version as a String, or a fallback if parsing fails.
     */
    fun getNativeServerVersion(): String {
        val matcher = SERVER_VERSION_PATTERN.matcher(Bukkit.getVersion())
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            // Fallback for server versions that might not include the "(MC: ...)" string.
            Bukkit.getBukkitVersion().split("-")[0]
        }
    }

    /**
     * Gets the Minecraft version of a specific client using the ViaVersion API.
     *
     * @param player The player whose version is to be checked.
     * @return The client's Minecraft version as a String (e.g., "1.16.5"),
     * or null if ViaVersion is not available or the version cannot be determined.
     */
    fun getClientVersion(player: Player): String? {
        // Ensure ViaVersion is running before attempting to use its API.
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            return null
        }
        val protocolVersion = Via.getAPI().getPlayerVersion(player.uniqueId)
        return ProtocolVersion.getProtocol(protocolVersion).name
    }

    /**
     * Checks if a player's client version is within a specified range.
     *
     * - If only `minVersion` is provided, it checks if the client version is `minVersion` or newer.
     * Example: `isVersionSupported(player, "1.19")` returns true for clients on 1.19, 1.19.4, 1.20, etc.
     *
     * - If both `minVersion` and `maxVersion` are provided, it checks if the client version
     * is between them (inclusive).
     * Example: `isVersionSupported(player, "1.8", "1.8.9")` returns true only for 1.8.x clients.
     *
     * @param player The player to check.
     * @param minVersion The minimum supported version string (e.g., "1.19").
     * @param maxVersion The optional maximum supported version string (e.g., "1.21").
     * @return `true` if the client's version is supported, `false` otherwise.
     */
    fun isVersionSupported(player: Player, minVersion: String, maxVersion: String? = null): Boolean {
        val clientVersionStr = getClientVersion(player) ?: return false

        val clientVersion = parseVersion(clientVersionStr)
        val min = parseVersion(minVersion)
        val max = maxVersion?.let { parseVersion(it) }

        // Check if client version is greater than or equal to minVersion
        val isAtLeastMin = compareVersions(clientVersion, min) >= 0

        return if (max != null) {
            // If max is specified, also check if client version is less than or equal to maxVersion
            val isAtMostMax = compareVersions(clientVersion, max) <= 0
            isAtLeastMin && isAtMostMax
        } else {
            // If only min is specified, just return the first check
            isAtLeastMin
        }
    }

    /**
     * A helper function to parse a version string (e.g., "1.16.5") into a list of integers.
     * This allows for proper numerical comparison.
     */
    fun parseVersion(version: String): List<Int> {
        return version.split('.').mapNotNull { it.toIntOrNull() }
    }

    /**
     * Compares two parsed version lists.
     * @return A value < 0 if v1 < v2, 0 if v1 == v2, a value > 0 if v1 > v2.
     */
    fun compareVersions(v1: List<Int>, v2: List<Int>): Int {
        val size = maxOf(v1.size, v2.size)
        for (i in 0 until size) {
            val part1 = v1.getOrElse(i) { 0 }
            val part2 = v2.getOrElse(i) { 0 }
            if (part1 != part2) {
                return part1.compareTo(part2)
            }
        }
        return 0
    }
}
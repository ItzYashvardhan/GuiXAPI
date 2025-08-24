package net.justlime.limeframegui.api

import net.justlime.limeframegui.color.FontColor
import net.justlime.limeframegui.enums.ColorType
import net.justlime.limeframegui.listener.InventoryListener
import net.justlime.limeframegui.listener.PluginListener
import net.justlime.limeframegui.models.FrameConfigKeys
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * Initialize the Listeners
 * */
object LimeFrameAPI {
    private lateinit var plugin: JavaPlugin

    var debugging: Boolean = false
    var keys: FrameConfigKeys = FrameConfigKeys()

    fun init(plugin: JavaPlugin, colorType: ColorType = ColorType.LEGACY) {
        this.plugin = plugin
        Bukkit.getPluginManager().registerEvents(InventoryListener(plugin), plugin)

        FontColor.setColorType(colorType)
        if (colorType == ColorType.MINI_MESSAGE) FontColor.initMiniMessage()

        //run a task on plugin disable
        Bukkit.getPluginManager().registerEvents(PluginListener(), plugin)
    }

    fun setKeys(customizer: FrameConfigKeys.() -> Unit) { keys.customizer() }

    fun getPlugin(): JavaPlugin { return plugin }

}
package net.justlime.limeframegui

import net.justlime.limeframegui.api.LimeFrameAPI
import net.justlime.limeframegui.example.commands.CommandManager
import org.bukkit.plugin.java.JavaPlugin


class LimeFrameGUI : JavaPlugin() {

    override fun onEnable() {
        this.saveDefaultConfig()
        CommandManager(this)
        LimeFrameAPI.init(this)
        LimeFrameAPI.debugging = false
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}

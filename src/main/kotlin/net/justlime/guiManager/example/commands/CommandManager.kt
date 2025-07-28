
package net.justlime.guiManager.example.commands

import net.justlime.guiManager.handle.CommandHandler
import org.bukkit.plugin.java.JavaPlugin

class CommandManager(val plugin: JavaPlugin) {
    private val simpleGUI = SimpleGUICommand(plugin)
    private val commandList = mutableMapOf<String, CommandHandler>()

    init {
        commandList["simplegui"] = simpleGUI
        initializeCommand()
    }

    fun initializeCommand() {
        commandList.forEach { (command, handle) ->
            plugin.getCommand(command)?.apply {
                setExecutor(handle)
                tabCompleter = handle
                permission = handle.permission
                aliases = handle.aliases
            }
        }
    }

}

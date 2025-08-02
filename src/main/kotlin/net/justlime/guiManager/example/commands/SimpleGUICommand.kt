package net.justlime.guiManager.example.commands

import net.justlime.guiManager.handle.CommandHandler
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.type.ChestGUI
import net.justlime.guiManager.utilities.toGuiItem
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class SimpleGUICommand(val plugin: JavaPlugin) : CommandHandler {
    override val permission: String = ""
    override val aliases: List<String> = mutableListOf()
    val guiSetting = GUISetting("Simple Inventory", 6)

    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String?>
    ): Boolean {
        if (sender !is Player) {
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("No arguments provided")
            return true
        }
        when (args[0]) {
            "save" -> {}
            "page" -> {
                val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                pageExample(sender, page)
            }

            else -> {}
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String?>
    ): List<String?>? {
        return listOf("save", "page")
    }

    fun pageExample(sender: Player, page: Int) {

        val nextItem = ItemStack(Material.ARROW).toGuiItem()
        nextItem.displayName = "next"
        val prevItem = ItemStack(Material.ARROW).toGuiItem()
        prevItem.displayName = "prev"

        val item1 = ItemStack(Material.PAPER).toGuiItem()
        val item2 = ItemStack(Material.DIAMOND).toGuiItem()
        val item3 = ItemStack(Material.STONE).toGuiItem()
        val item4 = ItemStack(Material.IRON_SWORD).toGuiItem()


        val gui = ChestGUI("Pager GUI", 6) {

            nav{
//                this.nextItem = nextItem
                this.prevItem = prevItem
                this.margin = 3
            }

            onOpen {
                sender.sendMessage("Opening")
            }
            onPageOpen {
                sender.sendMessage("Opening a Page")
            }

            onClose {
                sender.sendMessage("Closing")

            }

            onPageClose {
                sender.sendMessage("Closing a Page")
            }

            addItem(nextItem,11){
                it.whoClicked.sendMessage("§cYou click on next page")
            }

            setting.title = "Page 1"
            addPage {
                addItem(item1) {
                    it.whoClicked.sendMessage("Clicked on Item 1")
                }
                addItem(item2) {
                    it.whoClicked.sendMessage("Clicked on Item 2")
                }
            }

            setting.title = "Page 3"
            addPage {
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 5")
                }
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 6")
                }
            }

            onClick { it.isCancelled = true }



            setting.title = "Page 2"
            addPage {
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 3")
                }
                addItem(item4) {
                    it.whoClicked.sendMessage("Clicked on Item 4")
                }
            }
            // Open initial page
        }.open(sender, page)
    }
}


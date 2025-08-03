package net.justlime.guiManager.example.commands

import net.justlime.guiManager.handle.CommandHandler
import net.justlime.guiManager.type.ChestGUI
import net.justlime.guiManager.utilities.toGuiItem
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SimpleGUICommand() : CommandHandler {
    override val permission: String = ""
    override val aliases: List<String> = mutableListOf()

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
                pageExample(sender)
            }

            "home" -> {
                homePage(sender)
            }

            else -> {}
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String?>
    ): List<String?>? {
        return listOf("save", "page","home")
    }

    fun pageExample(player: Player){

        val nextItem = ItemStack(Material.ARROW).toGuiItem()
        nextItem.displayName = "next"
        val prevItem = ItemStack(Material.ARROW).toGuiItem()
        prevItem.displayName = "prev"

        val item1 = ItemStack(Material.PAPER).toGuiItem()
        val item2 = ItemStack(Material.DIAMOND).toGuiItem()
        val item3 = ItemStack(Material.STONE).toGuiItem()
        val item4 = ItemStack(Material.IRON_SWORD).toGuiItem()


       ChestGUI("Pager GUI", 6) {

            nav {
                this.nextItem = nextItem
                this.prevItem = prevItem
                this.margin = 3
            }

            //Global Click handler
            onClick { it.isCancelled = true }

            onOpen { player.sendMessage("Opening") } //Run once when a new gui instance open
            onPageOpen { player.sendMessage("Opening a Page") } //Run everytime when a new page open

            onClose { player.sendMessage("Closing") }
            onPageClose { player.sendMessage("Closing a Page") }

            //This item added to every page
            addItem(item3, 11) { it.whoClicked.sendMessage("§cYou click on a item at ${it.slot}") }

            addPage("Page {page}", 6) {
                //this item added to specific page only (page 1)
                addItem(item1) {
                    it.whoClicked.sendMessage("Clicked on Item 1")
                }
                addItem(item2) {
                    it.whoClicked.sendMessage("Clicked on Item 2")
                }

                //Runs for only specific Page (1)
                onOpen {
                    player.sendMessage("You open a page 1")
                }
            }

            setting.title = "Page {page}"
            setting.rows = 3
            addPage {
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 5 at page $currentPage")
                }
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 6")
                }
            }

            addPage("Page {page}", 4) {
                addItem(item4) {
                    it.whoClicked.sendMessage("Clicked on Item ${it.slot} at page $currentPage")
                }
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item ${it.slot} at page $currentPage")
                }
            }

        }.open(player)
    }

    fun simpleGUI(): ChestGUI {
        return ChestGUI("Simple GUI", 6) {
            onClick { it.isCancelled = true }

            val item = ItemStack(Material.DIAMOND).toGuiItem().apply {
                displayName = "§aClick Me!"
                lore = listOf("§7This is a simple item.")
            }

            addItem(item) {
                it.whoClicked.sendMessage("§aYou clicked the diamond!")
            }
        }
    }

    fun homePage(player: Player) {

        ChestGUI("Home Page", 1) {
            val simpleItem = ItemStack(Material.GRASS_BLOCK).toGuiItem().apply { displayName = "Open Simple GUI" }

            addItem(simpleItem) {
                simpleGUI().open(it.whoClicked as Player)
            }

            val pageItem = ItemStack(Material.BOOK).toGuiItem().apply { displayName = "Open Pager GUI" }

            addItem(pageItem) {
                pageExample(player)
            }

        }.open(player)

    }

}


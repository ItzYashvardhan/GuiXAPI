package net.justlime.limeframegui.example.commands

import net.justlime.limeframegui.enums.ColorType
import net.justlime.limeframegui.handle.CommandHandler
import net.justlime.limeframegui.impl.ConfigHandler
import net.justlime.limeframegui.type.ChestGUI
import net.justlime.limeframegui.type.ChestGUI.Companion.GLOBAL_PAGE
import net.justlime.limeframegui.utilities.FrameColor
import net.justlime.limeframegui.utilities.toGuiItem
import org.bukkit.Bukkit
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

            "save" -> savePage(sender)
            "page" -> {
                pageExample(sender)
            }

            "home" -> {
                homePage(sender)
            }

            "nested" -> {
                nestedPage(sender)
            }

            else -> {}
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String?>
    ): List<String?> {
        val completion = mutableListOf<String>()
        if (args.isNotEmpty()) completion.addAll(listOf("save", "page", "home", "nested"))
        return completion
    }

    fun pageExample(player: Player) {

        val nextItem = ItemStack(Material.ARROW).toGuiItem()
        nextItem.displayName = "next"
        val prevItem = ItemStack(Material.ARROW).toGuiItem()
        prevItem.displayName = "prev"

        val item1 = ItemStack(Material.PAPER).toGuiItem()
        val item2 = ItemStack(Material.DIAMOND).toGuiItem()
        val item3 = ItemStack(Material.STONE).toGuiItem()
        val item4 = ItemStack(Material.IRON_SWORD).toGuiItem()


        ChestGUI(6, "Pager GUI") {

            nav {
                this.nextItem = nextItem
                this.prevItem = prevItem
                this.margin = 3
            }

            //Global Click handler
            onClick { it.isCancelled = true }

            //This item added to every page
            setItem(item3)

            addPage(6, "Page {page}") {
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

            addPage(4, "Page {page}") {
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
        return ChestGUI(6, "Simple GUI") {
            onClick { it.isCancelled = true }

            val item = ItemStack(Material.DIAMOND).toGuiItem().apply {
                displayName = "§aClick Me!"
                lore = mutableListOf("§7This is a simple item.")
            }

            addItem(item) {
                it.whoClicked.sendMessage("§aYou clicked the diamond!")
            }
        }
    }

    fun homePage(player: Player) {

        ChestGUI(1, "Home Page") {
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

    fun savePage(player: Player) {
        FrameColor.colorType = ColorType.MINI_MESSAGE

        val config = ConfigHandler("config.yml")
        val setting = config.loadInventorySetting("inventory")
        val inventory = config.loadInventory("inventory") ?: Bukkit.createInventory(null, setting.rows * 9, setting.title)

        ChestGUI(setting.rows, setting.title) {

            onOpen {}
            loadInventoryContents(inventory)

            onClose {
                val inventory = pages[GLOBAL_PAGE]?.inventory ?: return@onClose //Definitely not happening
                config.saveInventory("inventory", inventory, setting.title)
            }
        }.open(player)

    }

    fun nestedPage(player: Player) {

        ChestGUI(6, "Nested GUI") {
            onClick { it.isCancelled = true }

            addPage(6, "Page 1") {
                addItem(ItemStack(Material.STONE).toGuiItem().apply { displayName = "Go to Nested Page 3" }) {
                    openPage(player, 3)
                }
                addPage(3, 5, "Page 11") {
                    addItem(ItemStack(Material.DIAMOND).toGuiItem().apply { displayName = "Go to Nested Page 4" }) {
                        openPage(player, 4)
                    }
                    addItem(ItemStack(Material.RED_BED).toGuiItem().apply { displayName = "Go to Nested Page 1" }) {
                        openPage(player, 1)
                    }
                    addPage {
                        addItem(ItemStack(Material.RED_BED).toGuiItem().apply { displayName = "Go to Nested Page 3" }) {
                            openPage(player, 3)
                        }
                        addItem(ItemStack(Material.RED_BED).toGuiItem().apply { displayName = "Go to Nested Page 1" }) {
                            openPage(player, 1)
                        }
                    }

                }

            }
        }.open(player)

    }

}


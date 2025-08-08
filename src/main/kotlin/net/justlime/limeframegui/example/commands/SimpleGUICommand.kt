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

            this.nav {
                this.nextItem = nextItem
                this.prevItem = prevItem
                this.margin = 3
//                this.nextSlot = 48
//                this.prevSlot = 51
            }


            //Global Click handler
            onClick { it.isCancelled = true }

            //This item added to every page
            //You can used it as Custom Background Design
            item4.slot = 5
            setItem(item4){
                it.whoClicked.sendMessage("You click on global item")
            }
            val a = this
            addPage(id = 2, title = "Kebab Mai Hadi"){
                item4.slotList = (11..20).toList()
                setItem(item4){
                    it.whoClicked.sendMessage("You click on global item")
                }
            }
            addPage(6, "Regular Page {page}") {
                //this item added to specific page only (page 1)
                for (i in 1..100) {
                    val newItem = item1.copy(displayName = "Item $i")
                    addItem(newItem) {
                        it.whoClicked.sendMessage("Clicked on Item at ${it.slot} of page $currentPage")
                    }
                }

                //Runs for only specific Page (1)
                onOpen {
                    player.sendMessage("You open a page 1")
                }
            }

            setting.title = "Custom Page {page}"
            setting.rows = 3
            addPage {
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 5 at page $currentPage")
                }
                addItem(item3) {
                    it.whoClicked.sendMessage("Clicked on Item 6")
                }
            }

            addPage(4, "Custom Page {page}") {
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

        //Useful if you gui can various different page
        //Don't use nav{} //It will give unexpected behaviour

        ChestGUI(6, "Nested GUI") {
            onClick { it.isCancelled = true }

            addPage(6, "Nested Page 1") {
                val item1 = ItemStack(Material.PAPER).toGuiItem()
                item1.displayName = "Go to Nested Page 2"
                addItem(item1) {
                    openPage(it.whoClicked as Player, 2)
                }

                addPage(2, 6, "Nested Page 2") {
                    val item2 = ItemStack(Material.DIAMOND).toGuiItem()
                    item2.displayName = "Go back to Nested Page 1"
                    addItem(item2) {
                        openPage(it.whoClicked as Player, 1)
                    }
                    val item3 = ItemStack(Material.GOLD_INGOT).toGuiItem()
                    item3.displayName = "Go to Nested Page 3"
                    addItem(item3) {
                        openPage(it.whoClicked as Player, 3)
                    }
                    addPage(3, 6, "Nested Page 3") {
                        val item4 = ItemStack(Material.IRON_INGOT).toGuiItem()
                        item4.displayName = "Go back to Nested Page 2"
                        addItem(item4) {
                            openPage(it.whoClicked as Player, 2)
                        }
                    }
                }

            }

        }.open(player)

    }

}


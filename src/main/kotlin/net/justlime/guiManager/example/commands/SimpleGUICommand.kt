package net.justlime.guiManager.example.commands

import net.justlime.guiManager.handle.CommandHandler
import net.justlime.guiManager.handle.ConfigHandler
import net.justlime.guiManager.handle.GUI
import net.justlime.guiManager.manager.InventoryManager
import net.justlime.guiManager.models.GUISetting
import net.justlime.guiManager.models.GuiItem
import net.justlime.guiManager.utilities.setItem
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

        var number = 1

        if (sender !is Player) {
            sender.sendMessage("Player only command")
            return true
        }

        val simpleGui = GUI.create(guiSetting)

        val inventory = simpleGui.inventory
        InventoryManager.addInventory("simplegui", inventory)
        val item = GuiItem(Material.NETHERRACK, "Simple", glow = true)
        inventory.setItem(0, item)


        if (args.isNotEmpty() && args[0] == "save") {
            itemEditor(sender)
            return true
        }

        if (args.isNotEmpty() && args[0] == "page") {
            pageExample(sender)
            return true
        }
        simpleGui.onOpen {
            sender.sendMessage("Opening Invenotry!")
        }

        //This will not Registered if above condition true and return
        //Global Setting for onClick
        simpleGui.onClick {
            it.isCancelled = true
        }

        val guiItem = GuiItem.getItem(ItemStack(Material.PAPER))


        simpleGui.addItem(guiItem) { event ->
            sender.sendMessage("You Clicked on Simple GUI!")
            sender.sendMessage(number.toString())
            number++
        }

        simpleGui

        val guiItems = listOf(
            GuiItem.getItem(ItemStack(Material.STONE)),
            GuiItem.getItem(ItemStack(Material.WOODEN_SWORD)),
            GuiItem.getItem(ItemStack(Material.PILLAGER_SPAWN_EGG)),
        )
        simpleGui.addItems(guiItems) { item, event ->
            event.whoClicked.sendMessage("You Clicked on ${item.material.name}!")
            number++
        }

        val setItem = ItemStack(Material.ARROW).toGuiItem()
        simpleGui.setItem(53, setItem) { item ->
            sender.sendMessage("You Clicked on Arrow! at slot ${item.slot}")
        }


        simpleGui.onClose {
            sender.sendMessage("You Closed Simple GUI!")
        }

        sender.openInventory(inventory)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String?>
    ): List<String?>? {
        return listOf("save", "page")
    }

    fun itemEditor(sender: Player) {

        //Edit the inventory and save it
        val loadInv = ConfigHandler.loadInventory(plugin.dataFolder, "Inventory", "simplegui")
        val newInventory = GUI.load(loadInv, guiSetting)

        newInventory.onOpen {
            sender.sendMessage("Opening Invenotry!")
        }

        newInventory.onClose {
            sender.sendMessage("Saving Invenotry!")
            ConfigHandler.saveInventory(plugin.dataFolder, "Inventory", "simplegui", newInventory.inventory)

        }
        sender.openInventory(newInventory.inventory)
    }

    fun pageExample(sender: Player) {

        val nextItem = ItemStack(Material.ARROW).toGuiItem()
        val prevItem = ItemStack(Material.ARROW).toGuiItem()

        val item1 = ItemStack(Material.PAPER).toGuiItem()
        val item2 = ItemStack(Material.DIAMOND).toGuiItem()
        val item3 = ItemStack(Material.STONE).toGuiItem()
        val item4 = ItemStack(Material.IRON_SWORD).toGuiItem()

        val pagerGui = GUI.create(guiSetting)

        pagerGui.setItem(48, nextItem) { event ->
            sender.sendMessage("open 48")
            pagerGui.openPage(sender, 1)
        }

        val home = ItemStack(Material.ENDER_PEARL).toGuiItem()
        pagerGui.setItem(49,home){
            sender.sendMessage("open 49")
            sender.openInventory(pagerGui.inventory)
        }

        pagerGui.setItem(50, prevItem) { event ->
            sender.sendMessage("open 50")
            pagerGui.openPage(sender, 2)
        }

        pagerGui[1] = pagerGui.createPage(guiSetting.copy(title = "Page 1/2", rows = 4)).apply {
            this.addItem(item1) { event ->
                event.whoClicked.sendMessage("You clicked on ${item1.material.name}")
            }
            this.addItem(item2)
        }

        pagerGui[2] = guiSetting.copy(title = "Page 2/2")
        pagerGui[2]!!.apply {
            this.addItem(item3){
                it.whoClicked.sendMessage("You clicked on Item3")
            }
            this.addItem(item4)
            this.setItem(50, GuiItem.air().copy(Material.STONE))
            this.setItem(51, GuiItem.air().copy(Material.STONE))
        }
        pagerGui.onClick {
            it.isCancelled = true
        }

        sender.openInventory(pagerGui.inventory)

    }

}

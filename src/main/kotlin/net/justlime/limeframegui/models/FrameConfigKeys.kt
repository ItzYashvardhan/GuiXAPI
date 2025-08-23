package net.justlime.limeframegui.models

import net.justlime.limeframegui.utilities.FontLoader

data class FrameConfigKeys(
    var inventoryTitle: String = "title",
    var inventoryRows: String = "rows",
    var inventoryItemSection: String = "items",
    var defaultInventoryTitle: String = "LimeFrame Inventory",
    var defaultInventoryRows: Int = 6,
    var material: String = "material",
    var name: String = "name",
    var lore: String = "lore",
    var amount: String = "amount",
    var glow: String = "glow",
    var flags: String = "flags",
    var model: String = "model",
    var texture: String = "texture",
    var unbreakable: String = "unbreakable",
    var damage: String = "damage",
    var slot: String = "slot",
    var slotList: String = "slots",
    var base64Data: String = "data",
    var smallCapsTitle: String = "font-title",
    var smallCapsName: String = "font-name",
    var smallCapsLore: String = "font-lore",
    var smallCapsFont: Map<String, Map<String, String>> = FontLoader.capsFont,
    var smallCaps: Boolean = false,//Set to try to use small caps font.
)



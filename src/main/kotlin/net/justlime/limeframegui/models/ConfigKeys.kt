package net.justlime.limeframegui.models

data class ConfigKeys(
    var inventoryTitle: String = "title",
    var inventoryRows: String = "rows",
    var inventoryItemSection: String = "items",
    var defaultInventoryTitle: String = "LimeFrame Inventory",
    var defaultInventoryRows: Int = 6,
    var material: String = "material",
    var name: String = "name",
    var lore: String = "lore",
    var glow: String = "glow",
    var flags: String = "flags",
    var model: String = "model",
    var amount: String = "amount",
    var texture: String = "texture",
    var slot: String = "slot",
    var slotList: String = "slots",
    var base64Data: String = "data"
)


package net.justlime.limeframegui.enums

enum class ChestGuiActions(val priority: Int) {
    GLOBAL_EVENT(1), //Open,Close
    GLOBAL_ITEMS(2),//Item click event
    PAGE_MANAGEMENT(3),//PAGE ADD, PAGE REMOVE
    PAGE_EVENT(4),//Open,Close
    NAVIGATION(5)
}
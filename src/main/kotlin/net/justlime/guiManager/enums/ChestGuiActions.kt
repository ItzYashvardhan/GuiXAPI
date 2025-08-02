package net.justlime.guiManager.enums

enum class ChestGuiActions(val priority: Int) {
    NAVIGATION(1),
    GLOBAL_EVENT(2),
    GLOBAL_ITEMS(3),
    PAGE(4),
}
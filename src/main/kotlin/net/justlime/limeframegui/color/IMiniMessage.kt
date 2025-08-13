package net.justlime.limeframegui.color

interface IMiniMessage {
    val legacySerializer: Any
    fun legacyToMini(text: String): String
}
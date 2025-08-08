package net.justlime.limeframegui.models

/**
 * This class prosed only used for get reserved slot so when adding a new item to page it will simply skip that slot
 * */
data class FrameReservedSlotPage(
    var enableNavSlotReservation: Boolean = false,
    var nextPageSlot: Int = -1, var prevPageSlot: Int = -1, var navMargin: Int = -1, var otherSlot: MutableSet<Int> = mutableSetOf()
)

package net.justlime.limeframegui.utilities

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

object ToolTipUtil {

    fun applyHideTooltip(meta: ItemMeta, hideTooltip: Boolean) {
        if (!hideTooltip) return

        try {
            // Try to get the new Paper API method (1.20+)
            val method = meta.javaClass.getMethod("setHideTooltip", Boolean::class.javaPrimitiveType)
            method.invoke(meta, true)
        } catch (e: NoSuchMethodException) {
           //Ignore
        } catch (e: Exception) {
            //Ignore

        }
    }
}
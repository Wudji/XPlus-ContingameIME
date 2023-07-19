package city.windmill.ingameime.forge

import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent

@OnlyIn(Dist.CLIENT)
class LegacyScreenEvents {
    abstract class ModEvent : Event(), IModBusEvent

    @Suppress("unused")
    class WindowSizeChanged(val width: Int, val height: Int) : ModEvent()
    class ScreenChanged(val oldScreen: Screen?, val newScreen: Screen?) : ModEvent()
    
    class EditOpen(val edit: Any, val caretPos: Pair<Int, Int>) : ModEvent()
    class EditCaret(val edit: Any, val caretPos: Pair<Int, Int>) : ModEvent()
    class EditClose(val edit: Any) : ModEvent()
}
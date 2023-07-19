package city.windmill.ingameime.forge

import city.windmill.ingameime.client.ConfigHandler
import city.windmill.ingameime.IngameIME
import city.windmill.ingameime.client.IMEHandler
import city.windmill.ingameime.client.KeyHandler
import city.windmill.ingameime.client.ScreenHandler
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.forge.ScreenEvents.EDIT_CARET
import city.windmill.ingameime.forge.ScreenEvents.EDIT_CLOSE
import city.windmill.ingameime.forge.ScreenEvents.EDIT_OPEN
import city.windmill.ingameime.forge.ScreenEvents.SCREEN_CHANGED
import city.windmill.ingameime.forge.ScreenEvents.WINDOW_SIZE_CHANGED
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.platform.forge.EventBuses
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.fml.IExtensionPoint
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.network.NetworkConstants
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(IngameIME.MODID)
object IngameIMEClientForge {
    //val modEventBus = EventBuses.getModEventBus(IngameIME.MODID).get()
    val INGAMEIME_BUS = MOD_BUS

    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    init {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        //EventBuses.registerModEventBus(IngameIME.MODID, LOADING_CONTEXT.extension())
        LOADING_CONTEXT.registerExtensionPoint(IExtensionPoint.DisplayTest::class.java) {
            IExtensionPoint.DisplayTest(NetworkConstants::IGNORESERVERONLY) { _, _ -> true }
        }

        runForDist({
            if (Util.getPlatform() == Util.OS.WINDOWS) {
                IngameIME.LOGGER.info("it is Windows OS! Loading mod...")

                INGAMEIME_BUS.addListener(::onInitializeClient)
                /*
                with(INGAMEIME_BUS) {
                    addListener(::registerKeys)
                    addListener(::enqueueIMC)
                }
                 */
            } else
                IngameIME.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
        }) { IngameIME.LOGGER.warn("This mod cant work in a DelicateServer!") }
    }

    private fun onInitializeClient(event: FMLClientSetupEvent) {
        IngameIME.onInitClient()
        KeyMappingRegistry.register(KeyHandler.toggleKey)
        ClientLifecycleEvent.CLIENT_STARTED.register(ClientLifecycleEvent.ClientState {
            ConfigHandler.initialConfig()

            ClientGuiEvent.RENDER_POST.register(ClientGuiEvent.ScreenRenderPost { screen, graphics, mouseX, mouseY, delta ->
                //Track mouse move here
                if (mouseX != prevX || mouseY != prevY) {
                    ScreenEvents.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                    prevX = mouseX
                    prevY = mouseY
                }

                OverlayScreen.render(graphics, mouseX, mouseY, delta)
            })
            ScreenEvents.SCREEN_MOUSE_MOVE.register(ScreenEvents.MouseMove { _, _, _, _ ->
                IMEHandler.IMEState.onMouseMove()
            })
            ClientScreenInputEvent.KEY_PRESSED_PRE.register(ClientScreenInputEvent.KeyPressed { client, screen, keyCode, scanCode, modifiers ->
                if (KeyHandler.KeyState.onKeyDown(keyCode, scanCode, modifiers))
                    EventResult.interruptDefault()
                else
                    EventResult.pass()
            })
            ClientScreenInputEvent.KEY_RELEASED_PRE.register(ClientScreenInputEvent.KeyReleased { client, screen, keyCode, scanCode, modifiers ->
                if (KeyHandler.KeyState.onKeyUp(keyCode, scanCode, modifiers))
                    EventResult.interruptDefault()
                else
                    EventResult.pass()
            })
            /*
            if (ModList.get().isLoaded("satin"))
                ResolutionChangeCallback.EVENT.register(ResolutionChangeCallback { _, _ ->
                    ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                })
            else {
                WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                    ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                })
            }
             */
            WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
            })
            with(ScreenHandler.ScreenState) {
                SCREEN_CHANGED.register(ScreenEvents.ScreenChanged(::onScreenChange))
            }
            with(ScreenHandler.ScreenState.EditState) {
                EDIT_OPEN.register(ScreenEvents.EditOpen(::onEditOpen))
                EDIT_CARET.register(ScreenEvents.EditCaret(::onEditCaret))
                EDIT_CLOSE.register(ScreenEvents.EditClose(::onEditClose))
            }
            //Ensure native dll are loaded, or crash the game
            IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
        })
        //Ensure native dll are loaded, or crash the game
        IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }

    /*
    private fun registerKeys(event: RegisterKeyMappingsEvent) {
        event.register(KeyHandler.toggleKey)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun enqueueIMC(event: InterModEnqueueEvent) {
        with(FORGE_BUS) {
            addListener<ScreenEvent.Render.Post> {
                OverlayScreen.render(it.guiGraphics, it.mouseX, it.mouseY, it.partialTick)
            }
            addListener<ScreenEvent.KeyPressed.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyDown(it.keyCode, it.scanCode, it.modifiers)
            }
            addListener<ScreenEvent.KeyReleased.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyUp(it.keyCode, it.scanCode, it.modifiers)
            }
        }
        with(INGAMEIME_BUS) {
            addListener<LegacyScreenEvents.WindowSizeChanged> {
                ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
            }
            addListener<LegacyScreenEvents.ScreenChanged> {
                ScreenHandler.ScreenState.onScreenChange(it.oldScreen, it.newScreen)
            }
            addListener<LegacyScreenEvents.EditOpen> {
                ScreenHandler.ScreenState.EditState.onEditOpen(it.edit, it.caretPos)
            }
            addListener<LegacyScreenEvents.EditCaret> {
                ScreenHandler.ScreenState.EditState.onEditCaret(it.edit, it.caretPos)
            }
            addListener<LegacyScreenEvents.EditClose> {
                ScreenHandler.ScreenState.EditState.onEditClose(it.edit)
            }
        }
        ConfigHandler.initialConfig()

        //Ensure native dll are loaded, or crash the game
        IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }
     */
}
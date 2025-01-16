package city.windmill.ingameime.fabric.mixin;


import city.windmill.ingameime.client.handler.ConfigHandler;
import city.windmill.ingameime.client.handler.IMEHandler;
import city.windmill.ingameime.client.handler.ScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Shadow
    protected EditBox input;

    @Inject(method = "render", at = @At("RETURN"))
    private void updateStatus(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (input.getValue().startsWith("/")) {
            if(input.getValue().startsWith("/msg") || input.getValue().startsWith("/tell") || input.getValue().startsWith("/tellraw")){
                IMEHandler.IMEState.Companion.onEditState(ScreenHandler.ScreenState.EditState.EDIT_OPEN);
            }else if(ConfigHandler.INSTANCE.getDisableIMEInCommandMode()){
               IMEHandler.IMEState.Companion.onEditState(ScreenHandler.ScreenState.EditState.NULL_EDIT);
            }
        } else IMEHandler.IMEState.Companion.onEditState(ScreenHandler.ScreenState.EditState.EDIT_OPEN);
    }
}

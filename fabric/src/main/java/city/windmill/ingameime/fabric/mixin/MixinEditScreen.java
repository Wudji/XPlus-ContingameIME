package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({Screen.class, AbstractSignEditScreen.class})
class MixinScreen {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}

@Mixin({BookEditScreen.class, AbstractSignEditScreen.class})
class MixinEditScreen {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }
}

@Mixin(BookEditScreen.class)
abstract class MixinBookEditScreen {
    @Inject(method = "renderCursor",
            at = @At(value = "INVOKE",
                    shift = At.Shift.BY,
                    by = 2,
                    target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;convertLocalToScreen(Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;)Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;")
    )
    private void onCaret_Book(GuiGraphics guiGraphics, BookEditScreen.Pos2i pos2i, boolean bl, CallbackInfo ci) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(pos2i.x, pos2i.y));
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I")
    )
    private void onCaret_Book(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci,@Local(ordinal = 0) int k,@Local(ordinal = 3) int o) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(
                k + 36 + (114 - o) / 2
                        - Minecraft.getInstance().font.width("_"),50

        ));
    }
}

@Mixin(AbstractSignEditScreen.class)
abstract class MixinSignEditScreen extends Screen {

    @Mutable
    @Final
    @Shadow
    protected final SignBlockEntity sign;
    private MixinSignEditScreen(Component component, SignBlockEntity sign) {
        super(component);
        this.sign = sign;
    }


    @Inject(method = "renderSignText",
            at = {
                    @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                            ordinal = 1),
                    @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                            ordinal = 0)},
            locals = LocalCapture.CAPTURE_FAILSOFT)
    //private void onCaret_Sign(GuiGraphics guiGraphics, CallbackInfo ci, float g, BlockState lv, boolean bl, boolean bl2, float h, MultiBufferSource.BufferSource lv2, float k, int l, int m, int n, int o, Matrix4f matrix4f, int p, String string, float q, int r, int s) {
    private void onCaret_Sign(GuiGraphics guiGraphics, CallbackInfo ci, @Local(ordinal = 4) int m, @Local(ordinal = 5) int q) {
        try {

//            Field m03 = matrix4f.getClass().getDeclaredField("m03");
//            Field m13 = matrix4f.getClass().getDeclaredField("m13");
//            m03.setAccessible(true);
//            m13.setAccessible(true);
            //s(23)->x,o(17)->y
            // dirty fix
            ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(50,50));
        } catch (Exception ignored) {

        }
    }
}

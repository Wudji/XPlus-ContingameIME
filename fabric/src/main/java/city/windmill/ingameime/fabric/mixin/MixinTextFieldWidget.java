package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = TextFieldWidget.class, remap = false)
abstract class MixinTextFieldWidget {
    @Shadow(remap = false)
    private boolean hasBorder;
    @Shadow(remap = false)
    private Rectangle bounds;

    @Dynamic
    @Inject(method = {"setFocused", "method_25365"}, at = @At("HEAD"))
    private void onSelected(boolean focused, CallbackInfo ci) {
        int caretX = hasBorder ? bounds.x + 4 : bounds.x;
        int caretY = hasBorder ? bounds.y + (bounds.height - 8) / 2 : bounds.y;

        if (focused){
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        }
        else{
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
        }

    }
    @Dynamic
    @Inject(method = {"render", "method_25394"},
            at = @At(value = "INVOKE", target = "java/lang/String.isEmpty()Z", ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void onCaret(GuiGraphics guiGraphics, int arg1, int arg2, float arg3, CallbackInfo ci, int l, int m, int n, String string, boolean bl, boolean bl2, int o, int p, int q, boolean bl3, int r) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(r, p));
    }
}
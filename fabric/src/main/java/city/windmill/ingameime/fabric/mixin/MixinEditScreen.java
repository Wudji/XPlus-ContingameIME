package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class, AbstractSignEditScreen.class})
class MixinScreen {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}

@Mixin(value = {BookEditScreen.class, AbstractSignEditScreen.class}, priority = 980)
class MixinEditScreen {
    @Inject(method = "init", at = @At("HEAD"))
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
    private MixinSignEditScreen(Component component, SignBlockEntity sign) {
        super(component);
    }

    @Inject(method = "renderSignText",
            at = {
                    @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                            ordinal = 1),
                    @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                            ordinal = 0)
            }
    )
    private void onCaret_Sign(GuiGraphics guiGraphics, CallbackInfo ci,
                              @Local(ordinal = 4) int verticalOffset,
                              @Local(ordinal = 6) int horizontalOffset) {
        int x = this.width / 2;
        int y = 90;
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(x + horizontalOffset, y + verticalOffset));
    }
}

package baritone.launch.mixins;

import kotlin.Unit;
import org.polyfrost.polyui.color.Colors;
import org.polyfrost.polyui.component.Component;
import org.polyfrost.polyui.component.Drawable;
import org.polyfrost.polyui.event.State;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.polyfrost.polyui.component.extensions.VisualsKt")
public abstract class MixinPolyUiTogglePalette {

    @Inject(
            method = "toggleable$lambda$2(Lorg/polyfrost/polyui/color/Colors;)Lorg/polyfrost/polyui/color/Colors$Palette;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void hypervision$useSuccessPaletteOnInitialTogglePaint(
            Colors colors,
            CallbackInfoReturnable<Colors.Palette> cir
    ) {
        if (colors != null) {
            cir.setReturnValue(colors.getState().getSuccess());
        }
    }

    @Inject(
            method = "toggleable$lambda$1(Lorg/polyfrost/polyui/component/Drawable;Lorg/polyfrost/polyui/event/State;Z)Lkotlin/Unit;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void hypervision$useSuccessPaletteOnToggleStateChange(
            Drawable drawable,
            State<?> state,
            boolean ignored,
            CallbackInfoReturnable<Unit> cir
    ) {
        Colors colors = ((Component) drawable).getPolyUI().getColors();
        if (colors == null) {
            return;
        }
        boolean enabled = Boolean.TRUE.equals(state.getValue());
        drawable.setPalette(enabled
                ? colors.getState().getSuccess()
                : colors.getComponent().getBg());
        cir.setReturnValue(Unit.INSTANCE);
    }
}

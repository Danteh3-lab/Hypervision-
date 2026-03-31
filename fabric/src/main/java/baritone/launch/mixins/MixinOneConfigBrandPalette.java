package baritone.launch.mixins;

import hypervision.fabric.HypervisionTheme;
import org.polyfrost.polyui.color.Colors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.polyfrost.oneconfig.internal.ui.OneConfigUI")
public abstract class MixinOneConfigBrandPalette {

    @Inject(
            method = "label$lambda$1(Lorg/polyfrost/polyui/color/Colors;)Lorg/polyfrost/polyui/color/Colors$Palette;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void hypervision$useCrimsonBadgePalette(Colors colors, CallbackInfoReturnable<Colors.Palette> cir) {
        cir.setReturnValue(HypervisionTheme.INSTANCE.getBrand().getFg());
    }
}

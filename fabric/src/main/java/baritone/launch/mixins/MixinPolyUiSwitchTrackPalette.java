package baritone.launch.mixins;

import org.polyfrost.polyui.color.Colors;
import org.polyfrost.polyui.component.Component;
import org.polyfrost.polyui.component.impl.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.polyfrost.polyui.component.impl.Derivatives")
public abstract class MixinPolyUiSwitchTrackPalette {

    @Inject(
            method = "Switch_D95pXEA$lambda$2$0(Lorg/polyfrost/polyui/component/impl/Block;Z)Lkotlin/Unit;",
            at = @At("HEAD"),
            remap = false
    )
    private static void hypervision$syncSwitchTrackPalette(
            Block block,
            boolean enabled,
            CallbackInfoReturnable<?> cir
    ) {
        Colors colors = ((Component) block).getPolyUI().getColors();
        if (colors == null) {
            return;
        }
        block.setPalette(enabled
                ? colors.getState().getSuccess()
                : colors.getComponent().getBg());
    }
}

/*
 * This file is part of hypervision.
 *
 * Hypervision is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypervision is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with hypervision.  If not, see <https://www.gnu.org/licenses/>.
 */

package hypervision.launch.mixins;

import hypervision.api.HypervisionAPI;
import hypervision.api.IBaritone;
import hypervision.api.event.events.PlayerUpdateEvent;
import hypervision.api.event.events.SprintStateEvent;
import hypervision.api.event.events.type.EventState;
import hypervision.behavior.LookBehavior;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Brady
 * @since 8/1/2018
 */
@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/player/AbstractClientPlayer.tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onPreUpdate(CallbackInfo ci) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer((LocalPlayer) (Object) this);
        if (hypervision != null) {
            hypervision.getGameEventHandler().onPlayerUpdate(new PlayerUpdateEvent(EventState.PRE));
        }
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/world/entity/player/Abilities.mayfly:Z"
            )
    )
    private boolean isAllowFlying(Abilities capabilities) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer((LocalPlayer) (Object) this);
        if (hypervision == null) {
            return capabilities.mayfly;
        }
        return !hypervision.getPathingBehavior().isPathing() && capabilities.mayfly;
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/KeyMapping.isDown()Z"
            )
    )
    private boolean isKeyDown(KeyMapping keyBinding) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer((LocalPlayer) (Object) this);
        if (hypervision == null) {
            return keyBinding.isDown();
        }
        SprintStateEvent event = new SprintStateEvent();
        hypervision.getGameEventHandler().onPlayerSprintState(event);
        if (event.getState() != null) {
            return event.getState();
        }
        if (hypervision != HypervisionAPI.getProvider().getPrimaryBaritone()) {
            // hitting control shouldn't make all bots sprint
            return false;
        }
        return keyBinding.isDown();
    }

    @Inject(
            method = "rideTick",
            at = @At(
                    value = "HEAD"
            )
    )
    private void updateRidden(CallbackInfo cb) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer((LocalPlayer) (Object) this);
        if (hypervision != null) {
            ((LookBehavior) hypervision.getLookBehavior()).pig();
        }
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"
            )
    )
    private boolean tryToStartFallFlying(final LocalPlayer instance) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer(instance);
        if (hypervision != null && hypervision.getPathingBehavior().isPathing()) {
            return false;
        }
        return instance.tryToStartFallFlying();
    }
}


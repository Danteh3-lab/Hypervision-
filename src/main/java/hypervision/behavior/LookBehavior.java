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

package hypervision.behavior;

import hypervision.Hypervision;
import hypervision.api.Settings;
import hypervision.api.behavior.ILookBehavior;
import hypervision.api.behavior.look.IAimProcessor;
import hypervision.api.behavior.look.ITickableAimProcessor;
import hypervision.api.event.events.*;
import hypervision.api.utils.IPlayerContext;
import hypervision.api.utils.Rotation;
import hypervision.behavior.look.ForkableRandom;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class LookBehavior extends Behavior implements ILookBehavior {

    /**
     * The current look target, may be {@code null}.
     */
    private Target target;

    /**
     * The rotation known to the server. Returned by {@link #getEffectiveRotation()} for use in {@link IPlayerContext}.
     */
    private Rotation serverRotation;

    /**
     * The last player rotation. Used to restore the player's angle when using free look.
     *
     * @see Settings#freeLook
     */
    private Rotation prevRotation;

    private final AimProcessor processor;

    private final Deque<Float> smoothYawBuffer;
    private final Deque<Float> smoothPitchBuffer;

    public LookBehavior(Hypervision hypervision) {
        super(hypervision);
        this.processor = new AimProcessor(hypervision.getPlayerContext());
        this.smoothYawBuffer = new ArrayDeque<>();
        this.smoothPitchBuffer = new ArrayDeque<>();
    }

    @Override
    public void updateTarget(Rotation rotation, boolean blockInteract) {
        this.target = new Target(rotation, Target.Mode.resolve(ctx, blockInteract), blockInteract);
    }

    @Override
    public IAimProcessor getAimProcessor() {
        return this.processor;
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.IN) {
            this.processor.tick();
        }
    }

    @Override
    public void onPlayerUpdate(PlayerUpdateEvent event) {

        if (this.target == null) {
            return;
        }

        final Target.Mode effectiveMode = this.effectiveMode();
        this.processor.setForceImmediate(this.target.blockInteract);

        switch (event.getState()) {
            case PRE: {
                if (effectiveMode == Target.Mode.NONE) {
                    // Just return for PRE, we still want to set target to null on POST
                    return;
                }

                this.prevRotation = new Rotation(ctx.player().getYRot(), ctx.player().getXRot());
                final Rotation actual = this.processor.peekRotation(this.target.rotation);
                ctx.player().setYRot(actual.getYaw());
                ctx.player().setXRot(actual.getPitch());
                break;
            }
            case POST: {
                // Reset the player's rotations back to their original values
                if (this.prevRotation != null) {
                    if (!this.shouldSyncClientLook()) {
                        this.smoothYawBuffer.addLast(this.target.rotation.getYaw());
                        while (this.smoothYawBuffer.size() > hypervision.settings().smoothLookTicks.value) {
                            this.smoothYawBuffer.removeFirst();
                        }
                        this.smoothPitchBuffer.addLast(this.target.rotation.getPitch());
                        while (this.smoothPitchBuffer.size() > hypervision.settings().smoothLookTicks.value) {
                            this.smoothPitchBuffer.removeFirst();
                        }
                        if (effectiveMode == Target.Mode.SERVER) {
                            ctx.player().setYRot(this.prevRotation.getYaw());
                            ctx.player().setXRot(this.prevRotation.getPitch());
                        } else if (ctx.player().isFallFlying() ? hypervision.settings().elytraSmoothLook.value : hypervision.settings().smoothLook.value) {
                            ctx.player().setYRot((float) this.smoothYawBuffer.stream().mapToDouble(d -> d).average().orElse(this.prevRotation.getYaw()));
                            if (ctx.player().isFallFlying()) {
                                ctx.player().setXRot((float) this.smoothPitchBuffer.stream().mapToDouble(d -> d).average().orElse(this.prevRotation.getPitch()));
                            }
                        }
                    }
                    //ctx.player().xRotO = prevRotation.getPitch();
                    //ctx.player().yRotO = prevRotation.getYaw();
                    this.prevRotation = null;
                }
                // The target is done being used for this game tick, so it can be invalidated
                this.target = null;
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onSendPacket(PacketEvent event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) {
            return;
        }

        final ServerboundMovePlayerPacket packet = (ServerboundMovePlayerPacket) event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket.Rot || packet instanceof ServerboundMovePlayerPacket.PosRot) {
            this.serverRotation = new Rotation(packet.getYRot(0.0f), packet.getXRot(0.0f));
        }
    }

    @Override
    public void onWorldEvent(WorldEvent event) {
        this.serverRotation = null;
        this.target = null;
    }

    public void pig() {
        if (this.target != null) {
            this.processor.setForceImmediate(this.target.blockInteract);
            final Rotation actual = this.processor.peekRotation(this.target.rotation);
            ctx.player().setYRot(actual.getYaw());
        }
    }

    public Optional<Rotation> getEffectiveRotation() {
        if (hypervision.settings().freeLook.value) {
            return Optional.ofNullable(this.serverRotation);
        }
        // If freeLook isn't on, just defer to the player's actual rotations
        return Optional.empty();
    }

    private boolean shouldSyncClientLook() {
        return hypervision.settings().syncClientLook.value;
    }

    private Target.Mode effectiveMode() {
        if (this.target == null) {
            return Target.Mode.NONE;
        }
        if (this.shouldSyncClientLook() && this.target.mode == Target.Mode.NONE) {
            return Target.Mode.CLIENT;
        }
        return this.target.mode;
    }

    @Override
    public void onPlayerRotationMove(RotationMoveEvent event) {
        if (this.target != null) {
            this.processor.setForceImmediate(this.target.blockInteract);
            final Rotation actual = this.processor.peekRotation(this.target.rotation);
            event.setYaw(actual.getYaw());
            event.setPitch(actual.getPitch());
        }
    }

    private static final class AimProcessor extends AbstractAimProcessor {

        public AimProcessor(final IPlayerContext ctx) {
            super(ctx);
        }

        @Override
        protected Rotation getPrevRotation() {
            // Implementation will use LookBehavior.serverRotation
            return ctx.playerRotations();
        }
    }

    private static abstract class AbstractAimProcessor implements ITickableAimProcessor {

        protected final IPlayerContext ctx;
        private final ForkableRandom rand;
        private double randomYawOffset;
        private double randomPitchOffset;
        private double driftYawOffset;
        private double driftPitchOffset;
        private double driftYawVelocity;
        private double driftPitchVelocity;
        private double biasTargetYawOffset;
        private double biasTargetPitchOffset;
        private int biasRetargetTicks;
        private boolean forceImmediate;

        public AbstractAimProcessor(IPlayerContext ctx) {
            this.ctx = ctx;
            this.rand = new ForkableRandom();
        }

        private AbstractAimProcessor(final AbstractAimProcessor source) {
            this.ctx = source.ctx;
            this.rand = source.rand.fork();
            this.randomYawOffset = source.randomYawOffset;
            this.randomPitchOffset = source.randomPitchOffset;
            this.driftYawOffset = source.driftYawOffset;
            this.driftPitchOffset = source.driftPitchOffset;
            this.driftYawVelocity = source.driftYawVelocity;
            this.driftPitchVelocity = source.driftPitchVelocity;
            this.biasTargetYawOffset = source.biasTargetYawOffset;
            this.biasTargetPitchOffset = source.biasTargetPitchOffset;
            this.biasRetargetTicks = source.biasRetargetTicks;
            this.forceImmediate = source.forceImmediate;
        }

        @Override
        public final Rotation peekRotation(final Rotation rotation) {
            final Rotation prev = this.getPrevRotation();

            float desiredYaw = rotation.getYaw();
            float desiredPitch = rotation.getPitch();

            // In other words, the target doesn't care about the pitch, so it used playerRotations().getPitch()
            // and it's safe to adjust it to a normal level
            if (desiredPitch == prev.getPitch()) {
                desiredPitch = nudgeToLevel(desiredPitch);
            }

            if (!this.forceImmediate) {
                desiredYaw += this.randomYawOffset;
                desiredPitch += this.randomPitchOffset;
                desiredYaw = prev.getYaw() + this.stepTowardsYaw(prev.getYaw(), desiredYaw);
                desiredPitch = prev.getPitch() + this.stepTowardsPitch(prev.getPitch(), desiredPitch);
            }

            return new Rotation(
                    this.calculateMouseMove(prev.getYaw(), desiredYaw),
                    this.calculateMouseMove(prev.getPitch(), desiredPitch)
            ).clamp();
        }

        @Override
        public final void tick() {
            final Settings settings = hypervision.settings();
            final double fineAmplitude = settings.randomLooking.value;
            final double coarseAmplitude = settings.randomLooking113.value;
            final double maxYawBias = 1.6 + coarseAmplitude * 0.85;
            final double maxPitchBias = 0.65 + fineAmplitude * 22.0;

            if (--this.biasRetargetTicks <= 0
                    || Math.abs(this.biasTargetYawOffset - this.driftYawOffset) < 0.14
                    || Math.abs(this.biasTargetPitchOffset - this.driftPitchOffset) < 0.08) {
                this.biasRetargetTicks = 14 + (int) (this.rand.nextDouble() * 18.0);
                this.biasTargetYawOffset = this.chooseBiasOffset(maxYawBias, Math.min(0.45, maxYawBias * 0.35));
                this.biasTargetPitchOffset = this.chooseBiasOffset(maxPitchBias, Math.min(0.2, maxPitchBias * 0.3));
            }

            this.driftYawVelocity = this.driftYawVelocity * 0.88
                    + this.clamp(this.biasTargetYawOffset - this.driftYawOffset, -0.35, 0.35) * 0.18
                    + this.biasedRandom() * (0.012 + coarseAmplitude * 0.008);
            this.driftPitchVelocity = this.driftPitchVelocity * 0.84
                    + this.clamp(this.biasTargetPitchOffset - this.driftPitchOffset, -0.18, 0.18) * 0.16
                    + (this.rand.nextDouble() - 0.5) * (0.01 + fineAmplitude * 0.5);

            this.driftYawOffset = this.clamp(
                    this.driftYawOffset + this.driftYawVelocity,
                    -maxYawBias,
                    maxYawBias
            );
            this.driftPitchOffset = this.clamp(
                    this.driftPitchOffset + this.driftPitchVelocity,
                    -maxPitchBias,
                    maxPitchBias
            );

            this.randomYawOffset = this.driftYawOffset + (this.rand.nextDouble() - 0.5) * Math.max(0.02, fineAmplitude * 0.55);
            this.randomPitchOffset = this.driftPitchOffset + (this.rand.nextDouble() - 0.5) * Math.max(0.015, fineAmplitude * 0.4);
        }

        @Override
        public final void advance(int ticks) {
            for (int i = 0; i < ticks; i++) {
                this.tick();
            }
        }

        @Override
        public Rotation nextRotation(final Rotation rotation) {
            final Rotation actual = this.peekRotation(rotation);
            this.tick();
            return actual;
        }

        @Override
        public final ITickableAimProcessor fork() {
            return new AbstractAimProcessor(this) {

                private Rotation prev = AbstractAimProcessor.this.getPrevRotation();

                @Override
                public Rotation nextRotation(final Rotation rotation) {
                    return (this.prev = super.nextRotation(rotation));
                }

                @Override
                protected Rotation getPrevRotation() {
                    return this.prev;
                }
            };
        }

        public final void setForceImmediate(boolean forceImmediate) {
            this.forceImmediate = forceImmediate;
        }

        protected abstract Rotation getPrevRotation();

        /**
         * Nudges the player's pitch to a regular level. (Between {@code -20} and {@code 10}, increments are by {@code 1})
         */
        private float nudgeToLevel(float pitch) {
            if (pitch < -20) {
                return pitch + 1;
            } else if (pitch > 10) {
                return pitch - 1;
            }
            return pitch;
        }

        private float calculateMouseMove(float current, float target) {
            final float delta = target - current;
            final double deltaPx = angleToMouse(delta); // yes, even the mouse movements use double
            return current + mouseToAngle(deltaPx);
        }

        private float stepTowardsYaw(float current, float target) {
            final float delta = Rotation.normalizeYaw(target - current);
            final float abs = Math.abs(delta);
            float maxStep = (float) (2.4 + Math.min(16.0, abs * 0.22));
            if (ctx.player().isFallFlying()) {
                maxStep += 5.0F;
            }
            return this.clamp(delta, -maxStep, maxStep);
        }

        private float stepTowardsPitch(float current, float target) {
            final float delta = target - current;
            final float abs = Math.abs(delta);
            float maxStep = (float) (1.8 + Math.min(10.0, abs * 0.15));
            if (ctx.player().isFallFlying()) {
                maxStep += 3.5F;
            }
            return this.clamp(delta, -maxStep, maxStep);
        }

        private double angleToMouse(float angleDelta) {
            final float minAngleChange = mouseToAngle(1);
            return Math.round(angleDelta / minAngleChange);
        }

        private float mouseToAngle(double mouseDelta) {
            // casting float literals to double gets us the precise values used by mc
            final double f = ctx.minecraft().options.sensitivity().get() * (double) 0.6f + (double) 0.2f;
            return (float) (mouseDelta * f * f * f * 8.0d) * 0.15f; // yes, one double and one float scaling factor
        }

        private double biasedRandom() {
            double random = this.rand.nextDouble() - 0.5;
            if (Math.abs(random) < 0.1) {
                random *= 4;
            }
            return random;
        }

        private double chooseBiasOffset(double maxMagnitude, double minMagnitude) {
            if (maxMagnitude <= 0.0) {
                return 0.0;
            }
            final double range = Math.max(0.0, maxMagnitude - minMagnitude);
            final double magnitude = minMagnitude + this.rand.nextDouble() * range;
            return this.rand.nextDouble() < 0.5 ? magnitude : -magnitude;
        }

        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        private double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private static class Target {

        public final Rotation rotation;
        public final Mode mode;
        public final boolean blockInteract;

        public Target(Rotation rotation, Mode mode, boolean blockInteract) {
            this.rotation = rotation;
            this.mode = mode;
            this.blockInteract = blockInteract;
        }

        enum Mode {
            /**
             * Rotation will be set client-side and is visual to the player
             */
            CLIENT,

            /**
             * Rotation will be set server-side and is silent to the player
             */
            SERVER,

            /**
             * Rotation will remain unaffected on both the client and server
             */
            NONE;

            static Mode resolve(IPlayerContext ctx, boolean blockInteract) {
                final Settings settings = hypervision.settings();
                final boolean antiCheat = settings.antiCheatCompatibility.value;
                final boolean blockFreeLook = settings.blockFreeLook.value;

                if (ctx.player().isFallFlying()) {
                    // always need to set angles while flying
                    return settings.elytraFreeLook.value ? SERVER : CLIENT;
                } else if (settings.freeLook.value) {
                    // Regardless of if antiCheatCompatibility is enabled, if a blockInteract is requested then the player
                    // rotation needs to be set somehow, otherwise Hypervision will halt since objectMouseOver() will just be
                    // whatever the player is mousing over visually. Let's just settle for setting it silently.
                    if (blockInteract) {
                        return blockFreeLook ? SERVER : CLIENT;
                    }
                    return antiCheat ? SERVER : NONE;
                }

                // all freeLook settings are disabled so set the angles
                return CLIENT;
            }
        }
    }
}


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

import hypervision.Hypervision;
import hypervision.api.HypervisionAPI;
import hypervision.api.IBaritone;
import hypervision.api.event.events.BlockChangeEvent;
import hypervision.api.event.events.ChatEvent;
import hypervision.api.event.events.ChunkEvent;
import hypervision.api.event.events.type.EventState;
import hypervision.api.utils.Pair;
import hypervision.cache.CachedChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brady
 * @since 8/3/2018
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetHandler {

    // unused lol
    /*@Inject(
            method = "handleChunkData",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/multiplayer/ChunkProviderClient.func_212474_a(IILnet/minecraft/network/PacketBuffer;IZ)Lnet/minecraft/world/chunk/Chunk;"
            )
    )
    private void preRead(SPacketChunkData packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            ClientPlayerEntity player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPlayNetHandler) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.PRE,
                                packetIn.isFullChunk() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                                packetIn.getChunkX(),
                                packetIn.getChunkZ()
                        )
                );
            }
        }
    }*/

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "sendChat(Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sendChatMessage(String string, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(string);
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForPlayer(this.minecraft.player);
        if (hypervision == null) {
            return;
        }
        hypervision.getGameEventHandler().onSendChatMessage(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleLevelChunkWithLight",
            at = @At("RETURN")
    )
    private void postHandleChunkData(ClientboundLevelChunkWithLightPacket packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            LocalPlayer player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPacketListener) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.POST,
                                !packetIn.isSkippable() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                                packetIn.getX(),
                                packetIn.getZ()
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleForgetLevelChunk",
            at = @At("HEAD")
    )
    private void preChunkUnload(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            LocalPlayer player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPacketListener) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(EventState.PRE, ChunkEvent.Type.UNLOAD, packet.getX(), packet.getZ())
                );
            }
        }
    }

    @Inject(
            method = "handleForgetLevelChunk",
            at = @At("RETURN")
    )
    private void postChunkUnload(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            LocalPlayer player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPacketListener) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(EventState.POST, ChunkEvent.Type.UNLOAD, packet.getX(), packet.getZ())
                );
            }
        }
    }

    @Inject(
            method = "handleBlockUpdate",
            at = @At("RETURN")
    )
    private void postHandleBlockChange(ClientboundBlockUpdatePacket packetIn, CallbackInfo ci) {
        if (!hypervision.settings().repackOnAnyBlockChange.value) {
            return;
        }
        if (!CachedChunk.BLOCKS_TO_KEEP_TRACK_OF.contains(packetIn.getBlockState().getBlock())) {
            return;
        }
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            LocalPlayer player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPacketListener) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.POST,
                                ChunkEvent.Type.POPULATE_FULL,
                                packetIn.getPos().getX() >> 4,
                                packetIn.getPos().getZ() >> 4
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleChunkBlocksUpdate",
            at = @At("RETURN")
    )
    private void postHandleMultiBlockChange(ClientboundSectionBlocksUpdatePacket packetIn, CallbackInfo ci) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForConnection((ClientPacketListener) (Object) this);
        if (hypervision == null) {
            return;
        }

        List<Pair<BlockPos, BlockState>> changes = new ArrayList<>();
        packetIn.runUpdates((mutPos, state) -> {
            changes.add(new Pair<>(mutPos.immutable(), state));
        });
        if (changes.isEmpty()) {
            return;
        }
        hypervision.getGameEventHandler().onBlockChange(new BlockChangeEvent(
                new ChunkPos(changes.get(0).first()),
                changes
        ));
    }

    @Inject(
            method = "handlePlayerCombatKill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;shouldShowDeathScreen()Z"
            )
    )
    private void onPlayerDeath(ClientboundPlayerCombatKillPacket packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : HypervisionAPI.getProvider().getAllBaritones()) {
            LocalPlayer player = ibaritone.getPlayerContext().player();
            if (player != null && player.connection == (ClientPacketListener) (Object) this) {
                ibaritone.getGameEventHandler().onPlayerDeath();
            }
        }
    }

    /*
    @Inject(
            method = "handleChunkData",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/world/chunk/Chunk.read(Lnet/minecraft/network/PacketBuffer;IZ)V"
            )
    )
    private void preRead(SPacketChunkData packetIn, CallbackInfo ci) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForConnection((NetHandlerPlayClient) (Object) this);
        if (hypervision == null) {
            return;
        }
        hypervision.getGameEventHandler().onChunkEvent(
                new ChunkEvent(
                        EventState.PRE,
                        packetIn.isFullChunk() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                        packetIn.getChunkX(),
                        packetIn.getChunkZ()
                )
        );
    }

    @Inject(
            method = "handleChunkData",
            at = @At("RETURN")
    )
    private void postHandleChunkData(SPacketChunkData packetIn, CallbackInfo ci) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForConnection((NetHandlerPlayClient) (Object) this);
        if (hypervision == null) {
            return;
        }
        hypervision.getGameEventHandler().onChunkEvent(
                new ChunkEvent(
                        EventState.POST,
                        packetIn.isFullChunk() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                        packetIn.getChunkX(),
                        packetIn.getChunkZ()
                )
        );
    }

    @Inject(
            method = "handleBlockChange",
            at = @At("RETURN")
    )
    private void postHandleBlockChange(SPacketBlockChange packetIn, CallbackInfo ci) {
        IBaritone hypervision = HypervisionAPI.getProvider().getBaritoneForConnection((NetHandlerPlayClient) (Object) this);
        if (hypervision == null) {
            return;
        }

        final ChunkPos pos = new ChunkPos(packetIn.getBlockPosition().getX() >> 4, packetIn.getBlockPosition().getZ() >> 4);
        final Pair<BlockPos, IBlockState> changed = new Pair<>(packetIn.getBlockPosition(), packetIn.getBlockState());
        hypervision.getGameEventHandler().onBlockChange(new BlockChangeEvent(pos, Collections.singletonList(changed)));
    }

    @Inject(
            method = "handleMultiBlockChange",
            at = @At("RETURN")
    )
    private void postHandleMultiBlockChange(SPacketMultiBlockChange packetIn, CallbackInfo ci) {

    }

     */
}



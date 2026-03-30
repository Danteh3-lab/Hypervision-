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

package hypervision.api;

import hypervision.api.cache.IWorldScanner;
import hypervision.api.command.ICommand;
import hypervision.api.command.ICommandSystem;
import hypervision.api.schematic.ISchematicSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.Objects;

/**
 * Provides the present {@link IBaritone} instances, as well as non-Hypervision instance related APIs.
 *
 * @author leijurv
 */
public interface IBaritoneProvider {

    /**
     * Returns the primary {@link IBaritone} instance. This instance is persistent, and
     * is represented by the local player that is created by the game itself, not a "bot"
     * player through hypervision.
     *
     * @return The primary {@link IBaritone} instance.
     */
    IBaritone getPrimaryBaritone();

    /**
     * Returns all of the active {@link IBaritone} instances. This includes the local one
     * returned by {@link #getPrimaryBaritone()}.
     *
     * @return All active {@link IBaritone} instances.
     * @see #getBaritoneForPlayer(LocalPlayer)
     */
    List<IBaritone> getAllBaritones();

    /**
     * Provides the {@link IBaritone} instance for a given {@link LocalPlayer}.
     *
     * @param player The player
     * @return The {@link IBaritone} instance.
     */
    default IBaritone getBaritoneForPlayer(LocalPlayer player) {
        for (IBaritone hypervision : this.getAllBaritones()) {
            if (Objects.equals(player, hypervision.getPlayerContext().player())) {
                return hypervision;
            }
        }
        return null;
    }

    /**
     * Provides the {@link IBaritone} instance for a given {@link Minecraft}.
     *
     * @param minecraft The minecraft
     * @return The {@link IBaritone} instance.
     */
    default IBaritone getBaritoneForMinecraft(Minecraft minecraft) {
        for (IBaritone hypervision : this.getAllBaritones()) {
            if (Objects.equals(minecraft, hypervision.getPlayerContext().minecraft())) {
                return hypervision;
            }
        }
        return null;
    }

    /**
     * Provides the {@link IBaritone} instance for the player with the specified connection.
     *
     * @param connection The connection
     * @return The {@link IBaritone} instance.
     */
    default IBaritone getBaritoneForConnection(ClientPacketListener connection) {
        for (IBaritone hypervision : this.getAllBaritones()) {
            final LocalPlayer player = hypervision.getPlayerContext().player();
            if (player != null && player.connection == connection) {
                return hypervision;
            }
        }
        return null;
    }

    /**
     * Creates and registers a new {@link IBaritone} instance using the specified {@link Minecraft}. The existing
     * instance is returned if already registered.
     *
     * @param minecraft The minecraft
     * @return The {@link IBaritone} instance
     */
    IBaritone createBaritone(Minecraft minecraft);

    /**
     * Destroys and removes the specified {@link IBaritone} instance. If the specified instance is the
     * {@link #getPrimaryBaritone() primary Hypervision}, this operation has no effect and will return {@code false}.
     *
     * @param Hypervision The Hypervision instance to remove
     * @return Whether the Hypervision instance was removed
     */
    boolean destroyBaritone(IBaritone hypervision);

    /**
     * Returns the {@link IWorldScanner} instance. This is not a type returned by
     * {@link IBaritone} implementation, because it is not linked with {@link IBaritone}.
     *
     * @return The {@link IWorldScanner} instance.
     */
    IWorldScanner getWorldScanner();

    /**
     * Returns the {@link ICommandSystem} instance. This is not bound to a specific {@link IBaritone}
     * instance because {@link ICommandSystem} itself controls global behavior for {@link ICommand}s.
     *
     * @return The {@link ICommandSystem} instance.
     */
    ICommandSystem getCommandSystem();

    /**
     * @return The {@link ISchematicSystem} instance.
     */
    ISchematicSystem getSchematicSystem();
}


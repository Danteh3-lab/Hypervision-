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

package hypervision.cache;

import hypervision.Hypervision;
import hypervision.api.cache.IWorldProvider;
import hypervision.api.utils.IPlayerContext;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Brady
 * @since 8/4/2018
 */
public class WorldProvider implements IWorldProvider {

    private static final String PREFERRED_DIR_NAME = "hypervision";
    private static final String LEGACY_DIR_NAME = "baritone";
    private static final String README_URL = "https://example.com/hypervision";

    private static final Map<Path, WorldData> worldCache = new HashMap<>();

    private final Hypervision hypervision;
    private final IPlayerContext ctx;
    private WorldData currentWorld;

    /**
     * This lets us detect a broken load/unload hook.
     * @see #detectAndHandleBrokenLoading()
     */
    private Level mcWorld;

    public WorldProvider(Hypervision hypervision) {
        this.hypervision = hypervision;
        this.ctx = hypervision.getPlayerContext();
    }

    @Override
    public final WorldData getCurrentWorld() {
        this.detectAndHandleBrokenLoading();
        return this.currentWorld;
    }

    /**
     * Called when a new world is initialized to discover the
     *
     * @param world The new world
     */
    public final void initWorld(Level world) {
        this.getSaveDirectories(world).ifPresent(dirs -> {
            final Path worldDir = dirs.getA();
            final Path readmeDir = dirs.getB();

            try {
                // lol wtf is this Hypervision folder in my minecraft save?
                // good thing we have a readme
                Files.createDirectories(readmeDir);
                Files.write(
                        readmeDir.resolve("readme.txt"),
                        (README_URL + "\n").getBytes(StandardCharsets.US_ASCII)
                );
            } catch (IOException ignored) {}

            // We will actually store the world data in a subfolder: "DIM<id>"
            final Path worldDataDir = this.getWorldDataDirectory(worldDir, world);
            try {
                Files.createDirectories(worldDataDir);
            } catch (IOException ignored) {}

            System.out.println("hypervision world data dir: " + worldDataDir);
            synchronized (worldCache) {
                this.currentWorld = worldCache.computeIfAbsent(worldDataDir, d -> new WorldData(d, world.dimensionType()));
            }
            this.mcWorld = ctx.world();
        });
    }

    public final void closeWorld() {
        WorldData world = this.currentWorld;
        this.currentWorld = null;
        this.mcWorld = null;
        if (world == null) {
            return;
        }
        world.onClose();
    }

    private Path getWorldDataDirectory(Path parent, Level world) {
        ResourceLocation dimId = world.dimension().location();
        int height = world.dimensionType().logicalHeight();
        return parent.resolve(dimId.getNamespace()).resolve(dimId.getPath() + "_" + height);
    }

    private static Path resolveDataRoot(Path parent) {
        Path preferred = parent.resolve(PREFERRED_DIR_NAME);
        Path legacy = parent.resolve(LEGACY_DIR_NAME);
        return Files.exists(preferred) || !Files.exists(legacy) ? preferred : legacy;
    }

    /**
     * @param world The world
     * @return An {@link Optional} containing the world's Hypervision dir and readme dir, or {@link Optional#empty()} if
     *         the world isn't valid for caching.
     */
    private Optional<Tuple<Path, Path>> getSaveDirectories(Level world) {
        Path worldDir;
        Path readmeDir;

        // If there is an integrated server running (Aka Singleplayer) then do magic to find the world save file
        if (ctx.minecraft().hasSingleplayerServer()) {
            worldDir = ctx.minecraft().getSingleplayerServer().getWorldPath(LevelResource.ROOT);

            // Gets the "depth" of this directory relative to the game's run directory, 2 is the location of the world
            if (worldDir.relativize(ctx.minecraft().gameDirectory.toPath()).getNameCount() != 2) {
                // subdirectory of the main save directory for this world
                worldDir = worldDir.getParent();
            }

            worldDir = resolveDataRoot(worldDir);
            readmeDir = worldDir;
        } else { // Otherwise, the server must be remote...
            String folderName;
            final ServerData serverData = ctx.minecraft().getCurrentServer();
            if (serverData != null) {
                folderName = ctx.minecraft().isConnectedToRealms() ? "realms" : serverData.ip;
            } else {
                //replaymod causes null currentServer and false singleplayer.
                System.out.println("World seems to be a replay. Not loading hypervision cache.");
                currentWorld = null;
                mcWorld = ctx.world();
                return Optional.empty();
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                folderName = folderName.replace(":", "_");
            }
            // TODO: This should probably be in "Hypervision/servers"
            worldDir = hypervision.getDirectory().resolve(folderName);
            // Just write the readme to the Hypervision directory instead of each server save in it
            readmeDir = hypervision.getDirectory();
        }

        return Optional.of(new Tuple<>(worldDir, readmeDir));
    }

    /**
     * Why does this exist instead of fixing the event? Some mods break the event. Lol.
     */
    private void detectAndHandleBrokenLoading() {
        if (this.mcWorld != ctx.world()) {
            if (this.currentWorld != null) {
                System.out.println("mc.world unloaded unnoticed! Unloading hypervision cache now.");
                closeWorld();
            }
            if (ctx.world() != null) {
                System.out.println("mc.world loaded unnoticed! Loading hypervision cache now.");
                initWorld(ctx.world());
            }
        } else if (this.currentWorld == null && ctx.world() != null && (ctx.minecraft().hasSingleplayerServer() || ctx.minecraft().getCurrentServer() != null)) {
            System.out.println("Retrying to load hypervision cache");
            initWorld(ctx.world());
        }
    }
}


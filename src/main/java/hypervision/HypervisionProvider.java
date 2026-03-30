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

package hypervision;

import hypervision.api.IBaritone;
import hypervision.api.IBaritoneProvider;
import hypervision.api.cache.IWorldScanner;
import hypervision.api.command.ICommandSystem;
import hypervision.api.schematic.ISchematicSystem;
import hypervision.cache.FasterWorldScanner;
import hypervision.command.CommandSystem;
import hypervision.command.ExampleBaritoneControl;
import hypervision.utils.schematic.SchematicSystem;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Brady
 * @since 9/29/2018
 */
public final class HypervisionProvider implements IBaritoneProvider {

    private final List<IBaritone> all;
    private final List<IBaritone> allView;

    public HypervisionProvider() {
        this.all = new CopyOnWriteArrayList<>();
        this.allView = Collections.unmodifiableList(this.all);

        // Setup chat control, just for the primary instance
        final hypervision primary = (hypervision) this.createBaritone(Minecraft.getInstance());
        primary.registerBehavior(ExampleBaritoneControl::new);
    }

    @Override
    public IBaritone getPrimaryBaritone() {
        return this.all.get(0);
    }

    @Override
    public List<IBaritone> getAllBaritones() {
        return this.allView;
    }

    @Override
    public synchronized IBaritone createBaritone(Minecraft minecraft) {
        IBaritone hypervision = this.getBaritoneForMinecraft(minecraft);
        if (hypervision == null) {
            this.all.add(hypervision = new hypervision(minecraft));
        }
        return hypervision;
    }

    @Override
    public synchronized boolean destroyBaritone(IBaritone hypervision) {
        return hypervision != this.getPrimaryBaritone() && this.all.remove(hypervision);
    }

    @Override
    public IWorldScanner getWorldScanner() {
        return FasterWorldScanner.INSTANCE;
    }

    @Override
    public ICommandSystem getCommandSystem() {
        return CommandSystem.INSTANCE;
    }

    @Override
    public ISchematicSystem getSchematicSystem() {
        return SchematicSystem.INSTANCE;
    }
}


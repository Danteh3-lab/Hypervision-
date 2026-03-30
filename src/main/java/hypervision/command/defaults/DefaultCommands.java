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

package hypervision.command.defaults;

import hypervision.api.IBaritone;
import hypervision.api.command.ICommand;

import java.util.*;

public final class DefaultCommands {

    private DefaultCommands() {
    }

    public static List<ICommand> createAll(IBaritone hypervision) {
        Objects.requireNonNull(hypervision);
        List<ICommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(hypervision),
                new SetCommand(hypervision),
                new CommandAlias(hypervision, Arrays.asList("modified", "mod", "hypervision", "modifiedsettings"), "List modified settings", "set modified"),
                new CommandAlias(hypervision, "reset", "Reset all settings or just one", "set reset"),
                new GoalCommand(hypervision),
                new GotoCommand(hypervision),
                new PathCommand(hypervision),
                new ProcCommand(hypervision),
                new ETACommand(hypervision),
                new VersionCommand(hypervision),
                new RepackCommand(hypervision),
                new BuildCommand(hypervision),
                //new SchematicaCommand(Hypervision),
                new LitematicaCommand(hypervision),
                new ComeCommand(hypervision),
                new AxisCommand(hypervision),
                new ForceCancelCommand(hypervision),
                new GcCommand(hypervision),
                new InvertCommand(hypervision),
                new TunnelCommand(hypervision),
                new RenderCommand(hypervision),
                new FarmCommand(hypervision),
                new FollowCommand(hypervision),
                new PickupCommand(hypervision),
                new ExploreFilterCommand(hypervision),
                new ReloadAllCommand(hypervision),
                new SaveAllCommand(hypervision),
                new ExploreCommand(hypervision),
                new BlacklistCommand(hypervision),
                new FindCommand(hypervision),
                new MineCommand(hypervision),
                new ClickCommand(hypervision),
                new SurfaceCommand(hypervision),
                new ThisWayCommand(hypervision),
                new WaypointsCommand(hypervision),
                new CommandAlias(hypervision, "sethome", "Sets your home waypoint", "waypoints save home"),
                new CommandAlias(hypervision, "home", "Path to your home waypoint", "waypoints goto home"),
                new SelCommand(hypervision),
                new ElytraCommand(hypervision)
        ));
        ExecutionControlCommands prc = new ExecutionControlCommands(hypervision);
        commands.add(prc.pauseCommand);
        commands.add(prc.resumeCommand);
        commands.add(prc.pausedCommand);
        commands.add(prc.cancelCommand);
        return Collections.unmodifiableList(commands);
    }
}


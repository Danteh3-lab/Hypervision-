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

package hypervision.api.command.manager;

import hypervision.api.IBaritone;
import hypervision.api.command.ICommand;
import hypervision.api.command.argument.ICommandArgument;
import hypervision.api.command.registry.Registry;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Brady
 * @since 9/21/2019
 */
public interface ICommandManager {

    IBaritone getBaritone();

    Registry<ICommand> getRegistry();

    /**
     * @param name The command name to search for.
     * @return The command, if found.
     */
    ICommand getCommand(String name);

    boolean execute(String string);

    boolean execute(Tuple<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(Tuple<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(String prefix);
}


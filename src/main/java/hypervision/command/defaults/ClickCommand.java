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
import hypervision.api.command.Command;
import hypervision.api.command.argument.IArgConsumer;
import hypervision.api.command.exception.CommandException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ClickCommand extends Command {

    public ClickCommand(IBaritone hypervision) {
        super(hypervision, "click");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMax(0);
        hypervision.openClick();
        logDirect("aight dude");
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Open click";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Opens click dude",
                "",
                "Usage:",
                "> click"
        );
    }
}


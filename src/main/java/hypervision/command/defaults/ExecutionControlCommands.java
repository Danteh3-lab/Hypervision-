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
import hypervision.api.command.exception.CommandInvalidStateException;
import hypervision.api.process.IBaritoneProcess;
import hypervision.api.process.PathingCommand;
import hypervision.api.process.PathingCommandType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Contains the pause, resume, and paused commands.
 * <p>
 * This thing is scoped to hell, private so far you can't even access it using reflection, because you AREN'T SUPPOSED
 * TO USE THIS to pause and resume hypervision. Make your own process that returns {@link PathingCommandType#REQUEST_PAUSE
 * REQUEST_PAUSE} as needed.
 */
public class ExecutionControlCommands {

    Command pauseCommand;
    Command resumeCommand;
    Command pausedCommand;
    Command cancelCommand;

    public ExecutionControlCommands(IBaritone hypervision) {
        // array for mutability, non-field so reflection can't touch it
        final boolean[] paused = {false};
        hypervision.getPathingControlManager().registerProcess(
                new IBaritoneProcess() {
                    @Override
                    public boolean isActive() {
                        return paused[0];
                    }

                    @Override
                    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
                        hypervision.getInputOverrideHandler().clearAllKeys();
                        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                    }

                    @Override
                    public boolean isTemporary() {
                        return true;
                    }

                    @Override
                    public void onLostControl() {
                    }

                    @Override
                    public double priority() {
                        return DEFAULT_PRIORITY + 1;
                    }

                    @Override
                    public String displayName0() {
                        return "Pause/Resume Commands";
                    }
                }
        );
        pauseCommand = new Command(hypervision, "pause", "p", "paws") {
            @Override
            public void execute(String label, IArgConsumer args) throws CommandException {
                args.requireMax(0);
                if (paused[0]) {
                    throw new CommandInvalidStateException("Already paused");
                }
                paused[0] = true;
                logDirect("Paused");
            }

            @Override
            public Stream<String> tabComplete(String label, IArgConsumer args) {
                return Stream.empty();
            }

            @Override
            public String getShortDesc() {
                return "Pauses hypervision until you use resume";
            }

            @Override
            public List<String> getLongDesc() {
                return Arrays.asList(
                        "The pause command tells hypervision to temporarily stop whatever it's doing.",
                        "",
                        "This can be used to pause pathing, building, following, whatever. A single use of the resume command will start it right back up again!",
                        "",
                        "Usage:",
                        "> pause"
                );
            }
        };
        resumeCommand = new Command(hypervision, "resume", "r", "unpause", "unpaws") {
            @Override
            public void execute(String label, IArgConsumer args) throws CommandException {
                args.requireMax(0);
                hypervision.getBuilderProcess().resume();
                if (!paused[0]) {
                    throw new CommandInvalidStateException("Not paused");
                }
                paused[0] = false;
                logDirect("Resumed");
            }

            @Override
            public Stream<String> tabComplete(String label, IArgConsumer args) {
                return Stream.empty();
            }

            @Override
            public String getShortDesc() {
                return "Resumes hypervision after a pause";
            }

            @Override
            public List<String> getLongDesc() {
                return Arrays.asList(
                        "The resume command tells hypervision to resume whatever it was doing when you last used pause.",
                        "",
                        "Usage:",
                        "> resume"
                );
            }
        };
        pausedCommand = new Command(hypervision, "paused") {
            @Override
            public void execute(String label, IArgConsumer args) throws CommandException {
                args.requireMax(0);
                logDirect(String.format("hypervision is %spaused", paused[0] ? "" : "not "));
            }

            @Override
            public Stream<String> tabComplete(String label, IArgConsumer args) {
                return Stream.empty();
            }

            @Override
            public String getShortDesc() {
                return "Tells you if hypervision is paused";
            }

            @Override
            public List<String> getLongDesc() {
                return Arrays.asList(
                        "The paused command tells you if hypervision is currently paused by use of the pause command.",
                        "",
                        "Usage:",
                        "> paused"
                );
            }
        };
        cancelCommand = new Command(hypervision, "cancel", "c", "stop") {
            @Override
            public void execute(String label, IArgConsumer args) throws CommandException {
                args.requireMax(0);
                if (paused[0]) {
                    paused[0] = false;
                }
                hypervision.getPathingBehavior().cancelEverything();
                logDirect("ok canceled");
            }

            @Override
            public Stream<String> tabComplete(String label, IArgConsumer args) {
                return Stream.empty();
            }

            @Override
            public String getShortDesc() {
                return "Cancel what hypervision is currently doing";
            }

            @Override
            public List<String> getLongDesc() {
                return Arrays.asList(
                        "The cancel command tells hypervision to stop whatever it's currently doing.",
                        "",
                        "Usage:",
                        "> cancel"
                );
            }
        };
    }
}


package baritone.utils.gui;

import baritone.api.IBaritone;

import java.util.function.Consumer;

public final class CommandMenuDispatcher {

    private static Consumer<IBaritone> opener;

    private CommandMenuDispatcher() {
    }

    public static void setOpener(Consumer<IBaritone> opener) {
        CommandMenuDispatcher.opener = opener;
    }

    public static boolean open(IBaritone baritone) {
        if (opener == null) {
            return false;
        }
        opener.accept(baritone);
        return true;
    }
}

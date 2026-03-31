package hypervision.fabric;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;

public final class HypervisionOneConfig extends Config {

    public static final HypervisionOneConfig INSTANCE = new HypervisionOneConfig();

    @Number(title = "Goal X", description = "Target X coordinate for quick pathfinding tests.", min = -30000000, max = 30000000, category = "Pathfinding", subcategory = "Go To")
    public int goalX = 0;

    @Number(title = "Goal Y", description = "Target Y coordinate for quick pathfinding tests.", min = -64, max = 320, category = "Pathfinding", subcategory = "Go To")
    public int goalY = 64;

    @Number(title = "Goal Z", description = "Target Z coordinate for quick pathfinding tests.", min = -30000000, max = 30000000, category = "Pathfinding", subcategory = "Go To")
    public int goalZ = 0;

    private boolean buildingDefaultBackup;

    private HypervisionOneConfig() {
        super("hypervision-oneconfig.json", "Hypervision", Category.OTHER);
    }

    @Override
    protected Tree makeTree() {
        Tree tree = super.makeTree();
        HypervisionSettingsMenuBuilder.populate(tree, this::isBuildingDefaultBackup);
        return tree;
    }

    @Override
    protected void initialize(boolean byConfigManager) {
        buildingDefaultBackup = true;
        try {
            super.initialize(byConfigManager);
        } finally {
            buildingDefaultBackup = false;
        }
    }

    public void syncFromBaritone() {
        if (tree == null) {
            preload();
        }
        if (tree != null) {
            HypervisionSettingsMenuBuilder.refresh(tree);
            getProperty("goalX").setAsReferential(goalX);
            getProperty("goalY").setAsReferential(goalY);
            getProperty("goalZ").setAsReferential(goalZ);
        }
    }

    boolean isBuildingDefaultBackup() {
        return buildingDefaultBackup;
    }

    @Button(title = "Pause Hypervision", description = "Pause the current automation process.", text = "Pause", category = "Controls", subcategory = "Core Controls")
    private void pauseHypervision() {
        runCommand("pause");
    }

    @Button(title = "Resume Hypervision", description = "Resume after a pause.", text = "Resume", category = "Controls", subcategory = "Core Controls")
    private void resumeHypervision() {
        runCommand("resume");
    }

    @Button(title = "Cancel Current Task", description = "Cancel the current path or active process.", text = "Cancel", category = "Controls", subcategory = "Core Controls")
    private void cancelCurrentTask() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        showMessage("Canceled current Hypervision task");
    }

    @Button(title = "Clear Goal", description = "Clear the armed goal and stop pathing.", text = "Clear goal", category = "Controls", subcategory = "Core Controls")
    private void clearGoal() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        showMessage("Cleared current Hypervision goal");
    }

    @Button(title = "Command Reference", description = "Show the in-chat help for advanced commands like goto, mine, build, and follow.", text = "Open help", category = "Controls", subcategory = "Core Controls")
    private void openCommandReference() {
        Minecraft.getInstance().setScreen(null);
        runCommand("help");
    }

    @Button(title = "Use Current Position", description = "Copy your current block position into the quick goto fields.", text = "Use current position", category = "Pathfinding", subcategory = "Go To")
    private void useCurrentPosition() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            showMessage("No player loaded for position capture");
            return;
        }
        goalX = client.player.blockPosition().getX();
        goalY = client.player.blockPosition().getY();
        goalZ = client.player.blockPosition().getZ();
        if (tree != null) {
            getProperty("goalX").setAsReferential(goalX);
            getProperty("goalY").setAsReferential(goalY);
            getProperty("goalZ").setAsReferential(goalZ);
        }
        save();
    }

    @Button(title = "Go To Coordinates", description = "Set a goal and start pathfinding toward the configured coordinates.", text = "Go to", category = "Pathfinding", subcategory = "Go To")
    private void goToCoordinates() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goalX, goalY, goalZ));
        showMessage("Going to " + goalX + ", " + goalY + ", " + goalZ);
        Minecraft.getInstance().setScreen(null);
    }

    private void runCommand(String command) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
    }

    static void showMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal("[Hypervision] " + message), false);
        }
    }
}

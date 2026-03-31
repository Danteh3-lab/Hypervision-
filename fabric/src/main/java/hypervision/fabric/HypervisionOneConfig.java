package hypervision.fabric;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

import java.util.Locale;

public final class HypervisionOneConfig extends Config {

    public static final HypervisionOneConfig INSTANCE = new HypervisionOneConfig();

    @Switch(title = "Interpolated Look", description = "Turn toward automation targets over several ticks instead of snapping.", category = "Look", subcategory = "Rotation")
    public static boolean interpolatedLook = true;

    @Number(title = "Interpolated Look Length", description = "How many ticks a full interpolated turn should take.", min = 1, max = 40, unit = "ticks", category = "Look", subcategory = "Rotation")
    public static int interpolatedLookLength = 10;

    @Switch(title = "Sync Client Look", description = "Keep the visible client camera matched to Hypervision's automation look.", category = "Look", subcategory = "Rotation")
    public static boolean syncClientLook = true;

    @Switch(title = "Free Look", description = "Allow silent server-side look updates when possible.", category = "Look", subcategory = "Rotation")
    public static boolean freeLook = true;

    @Switch(title = "Block Free Look", description = "Break and place blocks without forcing client-side turns.", category = "Look", subcategory = "Rotation")
    public static boolean blockFreeLook = false;

    @Switch(title = "Elytra Free Look", description = "Allow silent view corrections while using elytra automation.", category = "Look", subcategory = "Rotation")
    public static boolean elytraFreeLook = true;

    @Switch(title = "Smooth Look", description = "Average recent automation rotations onto the visible camera.", category = "Look", subcategory = "Compatibility")
    public static boolean smoothLook = false;

    @Switch(title = "Elytra Smooth Look", description = "Apply the smooth-look fallback during elytra flight.", category = "Look", subcategory = "Compatibility")
    public static boolean elytraSmoothLook = false;

    @Number(title = "Smooth Look Ticks", description = "How many ticks are averaged for the smooth-look fallback.", min = 1, max = 20, unit = "ticks", category = "Look", subcategory = "Compatibility")
    public static int smoothLookTicks = 5;

    @Switch(title = "Render Path", description = "Draw the current path overlay in-world.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderPath = true;

    @Switch(title = "Render Path As Line", description = "Show the path as a continuous line instead of boxes.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderPathAsLine = false;

    @Switch(title = "Render Path Ignore Depth", description = "Draw the path through blocks instead of depth-testing it.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderPathIgnoreDepth = true;

    @Switch(title = "Render Goal", description = "Draw the current goal marker in-world.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderGoal = true;

    @Switch(title = "Animated Goal", description = "Use Hypervision's animated goal rendering.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderGoalAnimated = true;

    @Switch(title = "Goal Ignore Depth", description = "Draw the goal marker through blocks.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderGoalIgnoreDepth = true;

    @Switch(title = "Goal Beacon", description = "Render X/Z goals with a beacon-style beam.", category = "Rendering", subcategory = "Path & Goal")
    public static boolean renderGoalXZBeacon = false;

    @Switch(title = "Chat Debug", description = "Print extra debug information to chat.", category = "Debug", subcategory = "Chat")
    public static boolean chatDebug = false;

    @Number(title = "Goal X", description = "Target X coordinate for quick pathfinding tests.", min = -30000000, max = 30000000, category = "Pathfinding", subcategory = "Go To")
    public static int goalX = 0;

    @Number(title = "Goal Y", description = "Target Y coordinate for quick pathfinding tests.", min = -64, max = 320, category = "Pathfinding", subcategory = "Go To")
    public static int goalY = 64;

    @Number(title = "Goal Z", description = "Target Z coordinate for quick pathfinding tests.", min = -30000000, max = 30000000, category = "Pathfinding", subcategory = "Go To")
    public static int goalZ = 0;

    private boolean syncing;

    private HypervisionOneConfig() {
        super("hypervision-oneconfig.json", "Hypervision", Category.OTHER);
    }

    @Override
    protected void initialize(boolean byConfigManager) {
        super.initialize(byConfigManager);
        addCallback("interpolatedLook", () -> applySetting("interpolatedLook", interpolatedLook));
        addCallback("interpolatedLookLength", () -> applySetting("interpolatedLookLength", interpolatedLookLength));
        addCallback("syncClientLook", () -> applySetting("syncClientLook", syncClientLook));
        addCallback("freeLook", () -> applySetting("freeLook", freeLook));
        addCallback("blockFreeLook", () -> applySetting("blockFreeLook", blockFreeLook));
        addCallback("elytraFreeLook", () -> applySetting("elytraFreeLook", elytraFreeLook));
        addCallback("smoothLook", () -> applySetting("smoothLook", smoothLook));
        addCallback("elytraSmoothLook", () -> applySetting("elytraSmoothLook", elytraSmoothLook));
        addCallback("smoothLookTicks", () -> applySetting("smoothLookTicks", smoothLookTicks));
        addCallback("renderPath", () -> applySetting("renderPath", renderPath));
        addCallback("renderPathAsLine", () -> applySetting("renderPathAsLine", renderPathAsLine));
        addCallback("renderPathIgnoreDepth", () -> applySetting("renderPathIgnoreDepth", renderPathIgnoreDepth));
        addCallback("renderGoal", () -> applySetting("renderGoal", renderGoal));
        addCallback("renderGoalAnimated", () -> applySetting("renderGoalAnimated", renderGoalAnimated));
        addCallback("renderGoalIgnoreDepth", () -> applySetting("renderGoalIgnoreDepth", renderGoalIgnoreDepth));
        addCallback("renderGoalXZBeacon", () -> applySetting("renderGoalXZBeacon", renderGoalXZBeacon));
        addCallback("chatDebug", () -> applySetting("chatDebug", chatDebug));
    }

    public void syncFromBaritone() {
        syncing = true;
        try {
            Settings settings = BaritoneAPI.getSettings();
            syncBoolean("interpolatedLook", settings.interpolatedLook.value);
            syncInt("interpolatedLookLength", settings.interpolatedLookLength.value);
            syncBoolean("syncClientLook", settings.syncClientLook.value);
            syncBoolean("freeLook", settings.freeLook.value);
            syncBoolean("blockFreeLook", settings.blockFreeLook.value);
            syncBoolean("elytraFreeLook", settings.elytraFreeLook.value);
            syncBoolean("smoothLook", settings.smoothLook.value);
            syncBoolean("elytraSmoothLook", settings.elytraSmoothLook.value);
            syncInt("smoothLookTicks", settings.smoothLookTicks.value);
            syncBoolean("renderPath", settings.renderPath.value);
            syncBoolean("renderPathAsLine", settings.renderPathAsLine.value);
            syncBoolean("renderPathIgnoreDepth", settings.renderPathIgnoreDepth.value);
            syncBoolean("renderGoal", settings.renderGoal.value);
            syncBoolean("renderGoalAnimated", settings.renderGoalAnimated.value);
            syncBoolean("renderGoalIgnoreDepth", settings.renderGoalIgnoreDepth.value);
            syncBoolean("renderGoalXZBeacon", settings.renderGoalXZBeacon.value);
            syncBoolean("chatDebug", settings.chatDebug.value);
        } finally {
            syncing = false;
        }
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
    }

    @Button(title = "Go To Coordinates", description = "Set a goal and start pathfinding toward the configured coordinates.", text = "Go to", category = "Pathfinding", subcategory = "Go To")
    private void goToCoordinates() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goalX, goalY, goalZ));
        showMessage("Going to " + goalX + ", " + goalY + ", " + goalZ);
        Minecraft.getInstance().setScreen(null);
    }

    private void applySetting(String settingName, Object value) {
        if (syncing) {
            return;
        }
        try {
            SettingsUtil.parseAndApply(BaritoneAPI.getSettings(), settingName.toLowerCase(Locale.ROOT), String.valueOf(value));
            SettingsUtil.save(BaritoneAPI.getSettings());
        } catch (Exception ex) {
            showMessage("Failed to update " + settingName + ": " + ex.getMessage());
        }
    }

    private void syncBoolean(String propertyName, boolean value) {
        switch (propertyName) {
            case "interpolatedLook" -> interpolatedLook = value;
            case "syncClientLook" -> syncClientLook = value;
            case "freeLook" -> freeLook = value;
            case "blockFreeLook" -> blockFreeLook = value;
            case "elytraFreeLook" -> elytraFreeLook = value;
            case "smoothLook" -> smoothLook = value;
            case "elytraSmoothLook" -> elytraSmoothLook = value;
            case "renderPath" -> renderPath = value;
            case "renderPathAsLine" -> renderPathAsLine = value;
            case "renderPathIgnoreDepth" -> renderPathIgnoreDepth = value;
            case "renderGoal" -> renderGoal = value;
            case "renderGoalAnimated" -> renderGoalAnimated = value;
            case "renderGoalIgnoreDepth" -> renderGoalIgnoreDepth = value;
            case "renderGoalXZBeacon" -> renderGoalXZBeacon = value;
            case "chatDebug" -> chatDebug = value;
            default -> throw new IllegalArgumentException("Unknown boolean property " + propertyName);
        }
        if (tree != null) {
            getProperty(propertyName).setAsReferential(value);
        }
    }

    private void syncInt(String propertyName, int value) {
        switch (propertyName) {
            case "interpolatedLookLength" -> interpolatedLookLength = value;
            case "smoothLookTicks" -> smoothLookTicks = value;
            default -> throw new IllegalArgumentException("Unknown integer property " + propertyName);
        }
        if (tree != null) {
            getProperty(propertyName).setAsReferential(value);
        }
    }

    private void runCommand(String command) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
    }

    private void showMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal("[Hypervision] " + message), false);
        }
    }
}

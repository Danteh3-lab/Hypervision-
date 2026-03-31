package hypervision.fabric;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.utils.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import org.polyfrost.polyui.color.ColorUtils;
import org.polyfrost.polyui.color.PolyColor;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.polyfrost.oneconfig.api.config.v1.dsl.Tree_extensionsKt.setCategory;
import static org.polyfrost.oneconfig.api.config.v1.dsl.Tree_extensionsKt.setSubcategory;
import static org.polyfrost.oneconfig.api.config.v1.dsl.Tree_extensionsKt.setVisualizer;

final class HypervisionSettingsMenuBuilder {

    private static final String CATEGORY_LOOK = "Look";
    private static final String CATEGORY_RENDERING = "Rendering";
    private static final String CATEGORY_PATHFINDING = "Pathfinding";
    private static final String CATEGORY_INVENTORY = "Inventory & Tools";
    private static final String CATEGORY_BUILDING = "Building";
    private static final String CATEGORY_MINING = "Mining";
    private static final String CATEGORY_FOLLOW = "Follow";
    private static final String CATEGORY_ELYTRA = "Elytra";
    private static final String CATEGORY_NOTIFICATIONS = "Notifications & Debug";
    private static final String CATEGORY_CACHE = "Caching & World Data";
    private static final String CATEGORY_ADVANCED = "Advanced";

    private static final List<String> CATEGORY_ORDER = List.of(
            CATEGORY_LOOK,
            CATEGORY_RENDERING,
            CATEGORY_PATHFINDING,
            CATEGORY_INVENTORY,
            CATEGORY_BUILDING,
            CATEGORY_MINING,
            CATEGORY_FOLLOW,
            CATEGORY_ELYTRA,
            CATEGORY_NOTIFICATIONS,
            CATEGORY_CACHE,
            CATEGORY_ADVANCED
    );

    private static final Set<String> HIDDEN_KEYS = Set.of(
            "buildRepeat",
            "logger",
            "notifier",
            "toaster",
            "randomLooking",
            "randomLooking113",
            "interpolatedLook",
            "interpolatedLookLength"
    );

    private static final Set<String> READ_ONLY_TYPE_NAMES = Set.of(
            "java.util.List<net.minecraft.world.level.block.Block>",
            "java.util.List<net.minecraft.world.item.Item>",
            "java.util.List<java.lang.String>",
            "java.util.Map<net.minecraft.world.level.block.Block, java.util.List<net.minecraft.world.level.block.Block>>"
    );

    private static final Map<String, String> TITLE_OVERRIDES = createTitleOverrides();
    private static final Map<String, String> DESCRIPTION_OVERRIDES = createDescriptionOverrides();

    private HypervisionSettingsMenuBuilder() {
    }

    static void populate(Tree root, BooleanSupplier buildingDefaultBackup) {
        List<GeneratedNode> generated = new ArrayList<>();
        Settings settings = BaritoneAPI.getSettings();
        for (Settings.Setting<?> setting : settings.allSettings) {
            if (shouldHide(setting)) {
                continue;
            }
            GeneratedNode editable = createEditableNode(setting, buildingDefaultBackup);
            if (editable != null) {
                generated.add(editable);
                continue;
            }
            generated.addAll(createReadOnlyNodes(setting));
        }

        generated.stream()
                .sorted(Comparator
                        .comparingInt((GeneratedNode node) -> categoryIndex(node.category))
                        .thenComparing(node -> node.category)
                .thenComparing(node -> node.subcategory)
                .thenComparing(node -> node.sortKey))
                .forEach(node -> root.put(node.node));
    }

    static void refresh(Tree root) {
        if (root == null) {
            return;
        }
        Settings settings = BaritoneAPI.getSettings();
        root.onAllProps((path, property) -> {
            String advancedSettingName = property.getMetadata("hypervision:advancedSetting");
            if (advancedSettingName == null) {
                return;
            }
            Settings.Setting<?> setting = settings.byLowerName.get(advancedSettingName.toLowerCase(Locale.ROOT));
            if (setting == null) {
                return;
            }
            property.description = readOnlyDescription(setting);
        });
    }

    private static boolean shouldHide(Settings.Setting<?> setting) {
        return setting.isJavaOnly() || HIDDEN_KEYS.contains(setting.getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static GeneratedNode createEditableNode(Settings.Setting<?> setting, BooleanSupplier buildingDefaultBackup) {
        String settingName = setting.getName();
        String title = displayTitle(settingName);
        String description = DESCRIPTION_OVERRIDES.get(settingName);
        CategoryInfo category = categoryFor(settingName, false);
        Type type = setting.getType();
        Class<?> valueClass = setting.getValueClass();

        if (valueClass == Boolean.class) {
            Property<Boolean> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Boolean>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Boolean>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Boolean.class
            );
            setVisualizer(property, Visualizer.SwitchVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Integer.class) {
            Property<Integer> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Integer>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Integer>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Integer.class
            );
            setVisualizer(property, Visualizer.NumberVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Long.class) {
            Property<Long> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Long>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Long>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Long.class
            );
            setVisualizer(property, Visualizer.NumberVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Float.class) {
            Property<Float> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Float>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Float>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Float.class
            );
            setVisualizer(property, Visualizer.NumberVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Double.class) {
            Property<Double> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Double>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Double>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Double.class
            );
            setVisualizer(property, Visualizer.NumberVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == String.class) {
            Property<String> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<String>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<String>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    String.class
            );
            setVisualizer(property, Visualizer.TextVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Rotation.class) {
            Property<Rotation> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Rotation>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Rotation>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Rotation.class
            );
            setVisualizer(property, Visualizer.DropdownVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Mirror.class) {
            Property<Mirror> property = Properties.functional(
                    defaultAwareGetter((Settings.Setting<Mirror>) setting, buildingDefaultBackup),
                    setter((Settings.Setting<Mirror>) setting, buildingDefaultBackup),
                    settingName,
                    title,
                    description,
                    Mirror.class
            );
            setVisualizer(property, Visualizer.DropdownVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (valueClass == Color.class) {
            Property<PolyColor> property = Properties.functional(
                    () -> ColorUtils.asMutable(ColorUtils.toPolyColor((Color) currentValue((Settings.Setting<Color>) setting, buildingDefaultBackup))),
                    value -> {
                        if (buildingDefaultBackup.getAsBoolean()) {
                            return;
                        }
                        ((Settings.Setting<Color>) setting).value = ColorUtils.toJavaColor(value);
                        SettingsUtil.save(BaritoneAPI.getSettings());
                    },
                    settingName,
                    title,
                    description,
                    PolyColor.class
            );
            setVisualizer(property, Visualizer.ColorVisualizer.class);
            return configureGeneratedNode(property, category, title, settingName);
        }

        if (READ_ONLY_TYPE_NAMES.contains(type.getTypeName())) {
            return null;
        }

        return null;
    }

    private static List<GeneratedNode> createReadOnlyNodes(Settings.Setting<?> setting) {
        if (!READ_ONLY_TYPE_NAMES.contains(setting.getType().getTypeName())) {
            return List.of();
        }

        String settingName = setting.getName();
        String title = displayTitle(settingName);
        CategoryInfo category = categoryFor(settingName, true);
        List<GeneratedNode> nodes = new ArrayList<>();

        Property<Void> info = Properties.dummy(
                settingName + ".info",
                title,
                readOnlyDescription(setting)
        );
        setVisualizer(info, Visualizer.InfoVisualizer.class);
        info.addMetadata("hypervision:advancedSetting", settingName);
        nodes.add(configureGeneratedNode(info, category, title, settingName));

        Property<Void> reset = Properties.dummy(
                settingName + ".reset",
                "Reset to Default",
                "Restore " + title + " to its default value."
        );
        setVisualizer(reset, Visualizer.ButtonVisualizer.class);
        reset.addMetadata("runnable", (Runnable) () -> {
            setting.reset();
            SettingsUtil.save(BaritoneAPI.getSettings());
            HypervisionOneConfig.showMessage("Reset " + title + " to default");
        });
        nodes.add(configureGeneratedNode(reset, category, title + " Reset", settingName));

        Property<Void> copy = Properties.dummy(
                settingName + ".copy",
                "Copy Set Command",
                "Copy a usable set command for " + title + " to the clipboard."
        );
        setVisualizer(copy, Visualizer.ButtonVisualizer.class);
        copy.addMetadata("runnable", (Runnable) () -> copySetCommand(setting, title));
        nodes.add(configureGeneratedNode(copy, category, title + " Copy", settingName));

        return nodes;
    }

    private static void copySetCommand(Settings.Setting<?> setting, String title) {
        String value = safeValueString(setting, setting.value);
        String command = "set " + setting.getName() + " " + value;
        Minecraft.getInstance().keyboardHandler.setClipboard(command);
        HypervisionOneConfig.showMessage("Copied set command for " + title);
    }

    private static String readOnlyDescription(Settings.Setting<?> setting) {
        return "Setting key: " + setting.getName()
                + "\nType: " + SettingsUtil.settingTypeToString(setting)
                + "\nCurrent value: " + safeValueString(setting, setting.value);
    }

    private static <T> Supplier<T> defaultAwareGetter(Settings.Setting<T> setting, BooleanSupplier buildingDefaultBackup) {
        return () -> currentValue(setting, buildingDefaultBackup);
    }

    private static <T> T currentValue(Settings.Setting<T> setting, BooleanSupplier buildingDefaultBackup) {
        return buildingDefaultBackup.getAsBoolean() ? setting.defaultValue : setting.value;
    }

    private static <T> Consumer<T> setter(Settings.Setting<T> setting, BooleanSupplier buildingDefaultBackup) {
        return value -> {
            if (buildingDefaultBackup.getAsBoolean()) {
                return;
            }
            setting.value = value;
            SettingsUtil.save(BaritoneAPI.getSettings());
        };
    }

    private static GeneratedNode configureGeneratedNode(Property<?> property, CategoryInfo category, String sortKey, String alias) {
        setCategory(property, category.category);
        setSubcategory(property, category.subcategory);
        property.addMetadata("aliases", new ArrayList<>(List.of(alias)));
        return new GeneratedNode(property, category.category, category.subcategory, sortKey);
    }

    private static CategoryInfo categoryFor(String settingName, boolean advanced) {
        String lowerName = settingName.toLowerCase(Locale.ROOT);
        String category;
        String subcategory;

        if (lowerName.startsWith("elytra")) {
            category = CATEGORY_ELYTRA;
            if (lowerName.contains("render")) {
                subcategory = "Rendering";
            } else if (lowerName.contains("cache") || lowerName.contains("seed") || lowerName.contains("terrain")) {
                subcategory = "Pathing Data";
            } else if (lowerName.contains("durability") || lowerName.contains("firework") || lowerName.contains("landing")) {
                subcategory = "Safety";
            } else {
                subcategory = "Flight";
            }
        } else if (lowerName.startsWith("follow")) {
            category = CATEGORY_FOLLOW;
            subcategory = "Following";
        } else if (lowerName.startsWith("build") || lowerName.contains("schematic") || lowerName.equals("okifair")) {
            category = CATEGORY_BUILDING;
            if (lowerName.contains("schematic") || lowerName.contains("rotation") || lowerName.contains("mirror")) {
                subcategory = "Schematics";
            } else if (lowerName.contains("substitute") || lowerName.contains("ignore") || lowerName.contains("skip") || lowerName.contains("existing")) {
                subcategory = "Placement Rules";
            } else {
                subcategory = "Building";
            }
        } else if (lowerName.startsWith("legitmine") || lowerName.startsWith("mine") || lowerName.contains("mining") || lowerName.contains("ore") || lowerName.contains("internalmining") || lowerName.equals("forceinternalmining") || lowerName.equals("cancelongoalinvalidation")) {
            category = CATEGORY_MINING;
            if (lowerName.startsWith("legitmine")) {
                subcategory = "Legit Mining";
            } else if (lowerName.contains("internalmining") || lowerName.equals("forceinternalmining")) {
                subcategory = "Internal Mining";
            } else {
                subcategory = "Mining";
            }
        } else if (lowerName.startsWith("render") || lowerName.startsWith("color") || lowerName.startsWith("selection") || lowerName.startsWith("goal") || lowerName.contains("selection")) {
            category = CATEGORY_RENDERING;
            if (lowerName.contains("selection")) {
                subcategory = "Selection";
            } else if (lowerName.startsWith("color")) {
                subcategory = "Colors";
            } else {
                subcategory = "Path & Goal";
            }
        } else if (lowerName.contains("look") || lowerName.contains("rotation") || lowerName.contains("yaw") || lowerName.contains("pitch")) {
            category = CATEGORY_LOOK;
            if (lowerName.contains("freelook")) {
                subcategory = "Free Look";
            } else if (lowerName.contains("smooth")) {
                subcategory = "Compatibility";
            } else {
                subcategory = "Rotation";
            }
        } else if (lowerName.contains("inventory") || lowerName.contains("tool") || lowerName.contains("throwaway") || lowerName.contains("reach") || lowerName.contains("rightclick") || lowerName.contains("breakspeed") || lowerName.contains("sword")) {
            category = CATEGORY_INVENTORY;
            if (lowerName.contains("inventory")) {
                subcategory = "Inventory";
            } else if (lowerName.contains("tool") || lowerName.contains("sword")) {
                subcategory = "Tools";
            } else {
                subcategory = "Interaction";
            }
        } else if (lowerName.contains("cache") || lowerName.contains("chunk") || lowerName.contains("waypoint") || lowerName.contains("prune") || lowerName.contains("cached")) {
            category = CATEGORY_CACHE;
            subcategory = "Chunk Cache";
        } else if (lowerName.contains("notification") || lowerName.contains("chat") || lowerName.contains("log") || lowerName.contains("toast") || lowerName.contains("verbose")) {
            category = CATEGORY_NOTIFICATIONS;
            if (lowerName.contains("notification")) {
                subcategory = "Notifications";
            } else {
                subcategory = "Chat & Logging";
            }
        } else {
            category = CATEGORY_PATHFINDING;
            if (lowerName.contains("avoid")) {
                subcategory = "Avoidance";
            } else if (lowerName.contains("cost") || lowerName.contains("penalty")) {
                subcategory = "Costs & Penalties";
            } else if (lowerName.contains("fall") || lowerName.contains("parkour") || lowerName.contains("liquid") || lowerName.contains("water") || lowerName.contains("lava") || lowerName.contains("safe") || lowerName.contains("portal")) {
                subcategory = "Safety";
            } else if (lowerName.contains("arrival") || lowerName.contains("goal") || lowerName.contains("axis")) {
                subcategory = "Goals & Arrival";
            } else if (lowerName.contains("explore") || lowerName.contains("completion")) {
                subcategory = "Exploration";
            } else if (lowerName.contains("pathing") || lowerName.contains("backtrack") || lowerName.contains("timeout") || lowerName.contains("border") || lowerName.contains("improvement")) {
                subcategory = "Pathing Core";
            } else {
                subcategory = "Movement";
            }
        }

        if (advanced) {
            return new CategoryInfo(CATEGORY_ADVANCED, category);
        }
        return new CategoryInfo(category, subcategory);
    }

    private static int categoryIndex(String category) {
        int index = CATEGORY_ORDER.indexOf(category);
        return index >= 0 ? index : Integer.MAX_VALUE;
    }

    private static String displayTitle(String settingName) {
        String override = TITLE_OVERRIDES.get(settingName);
        if (override != null) {
            return override;
        }

        StringBuilder title = new StringBuilder();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < settingName.length(); i++) {
            char c = settingName.charAt(i);
            if (Character.isUpperCase(c) && word.length() > 0) {
                appendWord(title, word.toString());
                word.setLength(0);
            }
            word.append(c);
        }
        appendWord(title, word.toString());
        return title.toString();
    }

    private static void appendWord(StringBuilder title, String rawWord) {
        if (rawWord.isEmpty()) {
            return;
        }
        if (title.length() > 0) {
            title.append(' ');
        }

        String lower = rawWord.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "xz" -> title.append("X/Z");
            case "x", "y", "z" -> title.append(lower.toUpperCase(Locale.ROOT));
            case "elytra" -> title.append("Elytra");
            case "eta" -> title.append("ETA");
            case "hud" -> title.append("HUD");
            case "ui" -> title.append("UI");
            case "xzbeacon" -> title.append("X/Z Beacon");
            default -> title.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
        }
    }

    private static String safeValueString(Settings.Setting<?> setting, Object value) {
        try {
            @SuppressWarnings("unchecked")
            Settings.Setting<Object> typed = (Settings.Setting<Object>) setting;
            return SettingsUtil.settingValueToString(typed, value);
        } catch (Exception ignored) {
            return String.valueOf(value);
        }
    }

    private static Map<String, String> createTitleOverrides() {
        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("renderGoalXZBeacon", "Goal X/Z Beacon");
        overrides.put("rightClickContainerOnArrival", "Right Click Container On Arrival");
        overrides.put("elytraMinimumAvoidance", "Elytra Minimum Avoidance");
        overrides.put("elytraMinFireworksBeforeLanding", "Elytra Minimum Fireworks Before Landing");
        overrides.put("ticksBetweenInventoryMoves", "Ticks Between Inventory Moves");
        overrides.put("cachedChunksExpirySeconds", "Cached Chunks Expiry Seconds");
        overrides.put("pathingMaxChunkBorderFetch", "Pathing Max Chunk Border Fetch");
        overrides.put("selectionLineWidth", "Selection Line Width");
        overrides.put("selectionOpacity", "Selection Opacity");
        overrides.put("randomLooking113", "Random Looking 1.13");
        return overrides;
    }

    private static Map<String, String> createDescriptionOverrides() {
        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("chatDebug", "Print extra debug information to chat.");
        overrides.put("desktopNotifications", "Show desktop notifications for supported Hypervision events.");
        overrides.put("renderGoalXZBeacon", "Render X/Z goals with a beacon-style beam.");
        overrides.put("renderGoalAnimated", "Use Hypervision's animated goal rendering.");
        overrides.put("renderPathAsLine", "Show the path as a continuous line instead of boxes.");
        overrides.put("interpolatedLookLength", "How many ticks a full interpolated turn should take.");
        overrides.put("smoothLookTicks", "How many ticks are averaged for the smooth-look fallback.");
        return overrides;
    }

    private record GeneratedNode(Property<?> node, String category, String subcategory, String sortKey) {
    }

    private record CategoryInfo(String category, String subcategory) {
    }
}

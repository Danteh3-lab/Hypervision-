package hypervision.fabric;

import baritone.Baritone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.polyfrost.polyui.PolyUI;
import org.polyfrost.polyui.color.Colors;
import org.polyfrost.polyui.color.DarkTheme;
import org.polyfrost.polyui.component.Component;
import org.polyfrost.polyui.component.extensions.VisualsKt;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public final class HypervisionConfigUi {

    private HypervisionConfigUi() {
    }

    public static Screen createScreen() {
        HypervisionOneConfig.INSTANCE.syncFromBaritone();
        try {
            installThemeOverride();
            Class<?> screensKt = Class.forName("org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt");
            Class<?> configClass = Class.forName("org.polyfrost.oneconfig.api.config.v1.Config");
            Method createScreen = screensKt.getMethod("createScreen", configClass);
            Screen screen = (Screen) createScreen.invoke(null, HypervisionOneConfig.INSTANCE);
            applyBranding(screen);
            return screen;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to create Hypervision OneConfig screen", e);
        }
    }

    public static void open() {
        Minecraft.getInstance().setScreen(createScreen());
    }

    public static void open(Baritone ignored) {
        open();
    }

    private static void applyBranding(Screen screen) {
        PolyUI polyUI = extractPolyUi(screen);
        if (polyUI == null) {
            return;
        }
        Component root = polyUI.getMaster();
        Colors oldColors = polyUI.getColors();
        if (oldColors != null && oldColors != HypervisionTheme.INSTANCE) {
            VisualsKt.retheme(root, oldColors, HypervisionTheme.INSTANCE, false);
        }
        polyUI.setColors(HypervisionTheme.INSTANCE);
        root.recalculate();
    }

    private static void installThemeOverride() {
        Map<String, Colors> themes = extractRegisteredThemes();
        if (themes == null || themes.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Colors> entry : new ArrayList<>(themes.entrySet())) {
            if (entry.getValue() instanceof DarkTheme) {
                themes.put(entry.getKey(), HypervisionTheme.INSTANCE);
            }
        }
        themes.put(HypervisionTheme.INSTANCE.getName(), HypervisionTheme.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Colors> extractRegisteredThemes() {
        try {
            Field field = PolyUI.class.getDeclaredField("registeredThemes");
            field.setAccessible(true);
            return (Map<String, Colors>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }

    private static PolyUI extractPolyUi(Screen screen) {
        try {
            if (!"org.polyfrost.oneconfig.api.ui.v1.internal.wrappers.PolyUIScreen".equals(screen.getClass().getName())) {
                return null;
            }
            Field polyUiField = screen.getClass().getField("polyUI");
            return (PolyUI) polyUiField.get(screen);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
}

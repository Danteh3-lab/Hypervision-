package hypervision.fabric;

import org.polyfrost.polyui.color.ColorUtils;
import org.polyfrost.polyui.color.Colors;
import org.polyfrost.polyui.color.DarkTheme;

public final class HypervisionTheme extends DarkTheme {

    public static final HypervisionTheme INSTANCE = new HypervisionTheme();

    private static final Colors.Palette BRAND_FG = new Colors.Palette(
            ColorUtils.rgba(193, 36, 62),
            ColorUtils.rgba(175, 29, 53),
            ColorUtils.rgba(214, 51, 81),
            ColorUtils.rgba(193, 36, 62, 0.5F)
    );

    private static final Colors.Palette BRAND_ACCENT = new Colors.Palette(
            ColorUtils.rgba(58, 17, 25),
            ColorUtils.rgba(70, 20, 30),
            ColorUtils.rgba(92, 28, 40),
            ColorUtils.rgba(58, 17, 25, 0.5F)
    );

    private static final Colors.Brand BRAND = new Colors.Brand(BRAND_FG, BRAND_ACCENT);

    private static final Colors.Palette ON_BRAND_FG = new Colors.Palette(
            ColorUtils.rgba(255, 231, 236),
            ColorUtils.rgba(255, 231, 236, 0.85F),
            ColorUtils.rgba(255, 242, 245),
            ColorUtils.rgba(255, 242, 245, 0.5F)
    );

    private static final Colors.Palette ON_BRAND_ACCENT = new Colors.Palette(
            ColorUtils.rgba(219, 58, 87),
            ColorUtils.rgba(219, 58, 87, 0.85F),
            ColorUtils.rgba(168, 36, 60),
            ColorUtils.rgba(219, 58, 87, 0.5F)
    );

    private static final Colors.OnBrand ON_BRAND = new Colors.OnBrand(ON_BRAND_FG, ON_BRAND_ACCENT);

    private static final Colors.Component COMPONENT = new Colors.Component(
            new Colors.Palette(
                    ColorUtils.rgba(26, 34, 41),
                    ColorUtils.rgba(43, 22, 28, 0.92F),
                    ColorUtils.rgba(56, 25, 33),
                    ColorUtils.rgba(34, 44, 53, 0.5F)
            ),
            ColorUtils.rgba(0, 0, 0, 0.0F)
    );

    private static final Colors.Page PAGE = new Colors.Page(
            new Colors.Palette(
                    ColorUtils.rgba(17, 23, 28),
                    ColorUtils.rgba(21, 28, 34),
                    ColorUtils.rgba(14, 19, 23),
                    ColorUtils.rgba(17, 23, 28, 0.5F)
            ),
            ColorUtils.rgba(255, 255, 255, 0.1F),
            new Colors.Palette(
                    ColorUtils.rgba(17, 23, 28),
                    ColorUtils.rgba(31, 19, 24),
                    ColorUtils.rgba(14, 19, 23),
                    ColorUtils.rgba(26, 34, 41, 0.5F)
            ),
            ColorUtils.rgba(193, 36, 62, 0.14F),
            ColorUtils.rgba(193, 36, 62, 0.22F),
            ColorUtils.rgba(193, 36, 62, 0.12F),
            ColorUtils.rgba(193, 36, 62, 0.06F)
    );

    private static final Colors.Text TEXT = new Colors.Text(
            new Colors.Palette(
                    ColorUtils.rgba(242, 230, 234),
                    ColorUtils.rgba(242, 230, 234, 0.85F),
                    ColorUtils.rgba(250, 240, 243),
                    ColorUtils.rgba(250, 240, 243, 0.5F)
            ),
            new Colors.Palette(
                    ColorUtils.rgba(154, 134, 141),
                    ColorUtils.rgba(133, 112, 120),
                    ColorUtils.rgba(170, 150, 157),
                    ColorUtils.rgba(154, 134, 141, 0.5F)
            )
    );

    private HypervisionTheme() {
    }

    @Override
    public String getName() {
        return "hypervision-dark";
    }

    @Override
    public Colors.Brand getBrand() {
        return BRAND;
    }

    @Override
    public Colors.OnBrand getOnBrand() {
        return ON_BRAND;
    }

    @Override
    public Colors.Component getComponent() {
        return COMPONENT;
    }

    @Override
    public Colors.Page getPage() {
        return PAGE;
    }

    @Override
    public Colors.Text getText() {
        return TEXT;
    }
}

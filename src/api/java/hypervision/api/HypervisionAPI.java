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

package hypervision.api;

import hypervision.api.utils.SettingsUtil;

/**
 * Exposes the {@link IBaritoneProvider} instance and the {@link Settings} instance for API usage.
 *
 * @author Brady
 * @since 9/23/2018
 */
public final class HypervisionAPI {

    private static final IBaritoneProvider provider;
    private static final Settings settings;

    static {
        settings = new Settings();
        SettingsUtil.readAndApply(settings, SettingsUtil.SETTINGS_DEFAULT_NAME);

        try {
            provider = (IBaritoneProvider) Class.forName("hypervision.HypervisionProvider").newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static IBaritoneProvider getProvider() {
        return HypervisionAPI.provider;
    }

    public static Settings getSettings() {
        return HypervisionAPI.settings;
    }
}


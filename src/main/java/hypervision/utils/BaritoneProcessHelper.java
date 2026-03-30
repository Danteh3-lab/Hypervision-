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

package hypervision.utils;

import hypervision.Hypervision;
import hypervision.api.process.IBaritoneProcess;
import hypervision.api.utils.Helper;
import hypervision.api.utils.IPlayerContext;

public abstract class BaritoneProcessHelper implements IBaritoneProcess, Helper {

    protected final Hypervision hypervision;
    protected final IPlayerContext ctx;

    public BaritoneProcessHelper(Hypervision hypervision) {
        this.hypervision = hypervision;
        this.ctx = hypervision.getPlayerContext();
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}


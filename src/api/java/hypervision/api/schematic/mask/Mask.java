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

package hypervision.api.schematic.mask;

import hypervision.api.schematic.mask.operator.BinaryOperatorMask;
import hypervision.api.schematic.mask.operator.NotMask;
import hypervision.api.utils.BooleanBinaryOperators;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Brady
 */
public interface Mask {

    /**
     * @param x            The relative x position of the block
     * @param y            The relative y position of the block
     * @param z            The relative z position of the block
     * @param currentState The current state of that block in the world, may be {@code null}
     * @return Whether the given position is included in this mask
     */
    boolean partOfMask(int x, int y, int z, BlockState currentState);

    int widthX();

    int heightY();

    int lengthZ();

    default Mask not() {
        return new NotMask(this);
    }

    default Mask union(Mask other) {
        return new BinaryOperatorMask(this, other, BooleanBinaryOperators.OR);
    }

    default Mask intersection(Mask other) {
        return new BinaryOperatorMask(this, other, BooleanBinaryOperators.AND);
    }

    default Mask xor(Mask other) {
        return new BinaryOperatorMask(this, other, BooleanBinaryOperators.XOR);
    }
}


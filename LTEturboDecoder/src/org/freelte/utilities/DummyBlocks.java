/*
 * Copyright (C) 2013 Chris Deng
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freelte.utilities;
import java.util.*;

/**
 * create dummy collection to initialize blocks for LTE
 * such as interleaver.  <T> is to be defined so the
 * values set in the block capacity are null
 * @author Chris Deng
 */
public class DummyBlocks<T> {
    public List<T> get00Block(Integer size) {
        List<T> blk = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            T e = null;
            blk.add(e);
        }
        return blk;
    }
}

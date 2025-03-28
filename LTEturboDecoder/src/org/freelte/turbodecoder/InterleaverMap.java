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
package org.freelte.turbodecoder;

import java.util.ArrayList;
import java.util.List;
import org.freelte.utilities.ExceptionUtilities;

/**
 *
 * @author Chris Deng
 */
class InterleaverMap {
    private Integer Len;
    private Integer f1, f2;
    private List<Integer> I = new ArrayList<>();
    private ExceptionUtilities exu = new ExceptionUtilities();

    public InterleaverMap () {
        Len = LteSpecs.turboILSize;
        f1 = 263;
        f2 = 480;
        for (int i = 0; i<Len; i++) {
            Long n = f1.longValue() * i + f2.longValue() * i * i;
            Long m = n % Len.longValue();

            I.add( m.intValue() );
        }
    }
    public Integer getLen() {
        return Len;
    }
    public Integer forwardIndex(Integer inIndex) {
        Integer out = 0;
        try {
            out = I.get(inIndex);
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return out;
    }

}

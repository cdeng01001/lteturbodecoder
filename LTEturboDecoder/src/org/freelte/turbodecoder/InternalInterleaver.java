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

import java.util.List;
import org.freelte.utilities.DummyBlocks;
import org.freelte.utilities.ExceptionUtilities;

/**
 * The interleaver used in turbo coding.  Generic type should be either
 * Integer or Double.  Integer is for encoding, where bits are known as
 * hard quantized symbols.  Double is for decoding, where soft symbols
 * are extracted from received signal.
 *
 * See LTE spec for the interleaver design.
 * @author Chris Deng
 */
class InternalInterleaver<T> {
    private InterleaverMap I = new InterleaverMap();
    private ExceptionUtilities exu = new ExceptionUtilities();
    private DummyBlocks dbs = new DummyBlocks();

    /**
     *
     * @param a input vector to interleave
     * @return vector that is interleaved from input
     */
    public List<T> interleave(List<T> a) {
        List<T> l = dbs.get00Block(a.size());
        try {
            if (a.size() != I.getLen()) {
                throw new Exception("interleave vector is not IL size!");
            }
            for (int i = 0; i < I.getLen(); i++) {
                l.set(I.forwardIndex(i), a.get(i));
            }

        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return l;
    }

    /**
     * The inverse function of interleave.
     * @param a vector to deInterleave
     * @return vector that's deInterleaved from input
     */
    public List<T> deInterleave(List<T> a) {
        List<T> out = dbs.get00Block(a.size());
        try {
            if (a.size() != I.getLen()) {
                throw new Exception("deInterleave vector is not IL size!");
            }
            for (int i = 0; i < I.getLen(); i++) {
                out.set(i, a.get(I.forwardIndex(i)));
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return out;
    }

    /**
     *
     * @return size of interleave block
     */
    public Integer getLen() {
        return I.getLen();
    }

}

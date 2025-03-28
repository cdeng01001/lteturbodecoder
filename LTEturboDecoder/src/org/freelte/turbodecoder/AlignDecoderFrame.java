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
import java.util.*;
import org.freelte.utilities.ExceptionUtilities;

/**
 * A helper to LLR demod to extract the terminating bits and packing
 * method of LTE frame into standard logical vectors.
 * For example, the data bits vector d0 are extended with LTE termination
 * bits which should be unpacked and only the relevant d0 bits
 * are attached to d0 vector.
 * @author Chris Deng
 */
public class AlignDecoderFrame {
    private ExceptionUtilities exu = new ExceptionUtilities();
    private InternalInterleaver I = new InternalInterleaver();

    /**
     * Extracts the termination bits from the frame and
     * attach them into the proper vectors such that d0
     * termination bits are attached to the d0 vector.
     * @param f DecoderFrame output from LLR demod
     */
    public void align(DecoderFrame f) {
        //now all the 3 termination bits at special LTE locations
        //per LTE 5.1.3.2.2
        Integer K = LteSpecs.turboILSize;
        //llr0 K position is the correct K bit
        List<Double> d0ex = new ArrayList<>();
        this.transfer(f.llr0, d0ex, K, LteSpecs.ILplusTermSize);
        List<Double> d1ex = new ArrayList<>();
        this.transfer(f.llr1, d1ex, K, LteSpecs.ILplusTermSize);
        List<Double> d2ex = new ArrayList<>();
        this.transfer(f.llr2, d2ex, K, LteSpecs.ILplusTermSize);

        //LTE also transmit the IL term bits for data
        //the so called X'_K and X'_K+1, X'_K+2
        f.illr0 = I.interleave(f.llr0);

        //for the temporary *ex storage, 0 is K index, thus 1 = K + 1
        f.llr0.add(d0ex.get(0));
        f.llr0.add(d2ex.get(0));
        f.llr0.add(d1ex.get(1));

        f.llr1.add(d1ex.get(0));
        f.llr1.add(d0ex.get(1));
        f.llr1.add(d2ex.get(1));

        f.llr2.add(d1ex.get(2));
        f.llr2.add(d0ex.get(3));
        f.llr2.add(d2ex.get(3));

        //now add the LTE IL term bits
        f.illr0.add(d0ex.get(2));
        f.illr0.add(d2ex.get(2));
        f.illr0.add(d1ex.get(3));
    }

    /**
     * remove array from start (inclusive) to end (exclusive)
     * or [start, end)
     * from a to b
     * @param a List<Double> source
     * @param b List<Double> destination
     * @param start Integer
     * @param end Integer
     */
    private void transfer(List<Double> a, List<Double> b, int start, int end) {
        try {
            for (int i = start; i < end; i++) {
                b.add( a.remove(start) );
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
}

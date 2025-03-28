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
import org.freelte.utilities.DummyBlocks;
import org.freelte.utilities.ExceptionUtilities;
import org.freelte.random.WGNgen;
import org.freelte.random.BernoulliGen;
import java.util.*;

/**
 * Per symbol has a list of d for tx
 * d contains bits
 * p are parity bits
 * dn = d + wgn
 * pn = p + wgn
 * @author Chris Deng
 */
public class EncoderFrame {
    public List<Integer> d0;
    public List<Integer> d1;
    public List<Integer> d2;
    public List<Integer> d0term = new ArrayList<>();
    public List<Integer> d1term = new ArrayList<>();
    public List<Integer> d2term = new ArrayList<>();
    public List<Double> d0n;
    public List<Double> d1n;
    public List<Double> d2n;
    public Double var; //variance of wgn
    private ExceptionUtilities exu = new ExceptionUtilities();
    private DummyBlocks dbs = new DummyBlocks();
    /**
     * total frame length includes trellis termination bits
     * @param Length
     */
    public EncoderFrame() {
        Integer Length = LteSpecs.turboILSize;
        d0 = dbs.get00Block(Length);
        d1 = dbs.get00Block(Length);
        d2 = dbs.get00Block(Length);
        d0n = dbs.get00Block(Length);
        d1n = dbs.get00Block(Length);
        d2n = dbs.get00Block(Length);
    }

    /**
     * Use additive model for receive symbols
     * rx sym 1 = bit1 + r1 and so on for r2, r3
     * @param k index, 0 to frameLength-1
     * @param r0 superpose to bit 1
     * @param r1 superpose to parity 1, before interleaver
     * @param r2 superpose to parity 2, after interleaver
     */
    public void setTripleRxAdd(Integer k, Double r0, Double r1, Double r2) {
        try {
            this.d0n.set(k, this.d0n.get(k)+r0);
            this.d1n.set(k, this.d1n.get(k)+r1);
            this.d2n.set(k, this.d2n.get(k)+r2);
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }

}

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
package org.freelte.e2emodels;
import org.freelte.turbodecoder.LteSpecs;
import org.freelte.turbodecoder.EncoderFrame;
import java.util.*;
import org.freelte.random.BernoulliGen;
import org.freelte.utilities.ExceptionUtilities;

/**
 *
 * @author Chris Deng
 */
class TransmitGen {
    private ExceptionUtilities exu = new ExceptionUtilities();
    private BernoulliGen bg = new BernoulliGen(0.5);
    /**
     * generate Data bits only
     * put 0/1 into the data field
     * generates the entire frame and later the trellis terminating
     * bits will over write the last 6 bits
     * @param X
     */
    void genData(EncoderFrame X) {
        for (int i=0; i<LteSpecs.turboILSize; i++) {
            X.d0.set(i, bg.getValue());
        }
    }

    /**
     * output bits are modulated to digital +/- 1
     * so the signal power is normalized to 1.0
     * @param X
     */
    public void transmit(EncoderFrame X) {
        //generate random bits
        //this.genData(X);

        //FEC on data with turbo code
        //TurboEncoder tc = new TurboEncoder();
        //tc.encodeFrame(X);
        X.d0n = bpsk(X.d0);
        X.d1n = bpsk(X.d1);
        X.d2n = bpsk(X.d2);
    }

    private List<Double> bpsk(List<Integer> a) {
        List<Double> out = new ArrayList<>();
        try {
            for (int i = 0; i < a.size(); i++) {
                out.add( LteSpecs.BitSymbols.getSym(a.get(i)) );
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return out;
    }
}

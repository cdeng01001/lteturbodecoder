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
import org.freelte.turbodecoder.DecoderFrame;
import org.freelte.turbodecoder.EncoderFrame;
import org.freelte.utilities.ExceptionUtilities;
import java.util.*;

/**
 * BPSK demodulate an encoded frame, extract termination symbols
 * into unique storage identifiers
 * @author Chris Deng
 */
class BPSKDemodLLR {
    private ExceptionUtilities exu = new ExceptionUtilities();
    private Double var;

    public DecoderFrame demod(EncoderFrame x) {
        DecoderFrame F = new DecoderFrame(x);
        F.x = x;
        this.var = x.var;
        F.llr0 = llr(x.d0n);
        F.llr1 = llr(x.d1n);
        F.llr2 = llr(x.d2n);
        return F;
    }
    /**
     * LLR assume AWGN = log( p0 ) - log(p1)
     * log(p0) = log( exp(-(x-d0)^2 / 2*var) ), d0 = 1
     * log(p1) = log( exp(-(x-d1)^2 / 2*var) ), d1 = -1
     * so result = -(x-d0)^2 + (x-d1)^2 / 2*var
     *
     * Stops array at IL size.  end symbols are wacky LTE frame ending
     * @param a List<Double> of receive signal
     * @return List<Double>
     */
    private List<Double> llr(List<Double> a) {
        List<Double> out = new ArrayList<>();
        try {
            for (int i = 0; i < a.size(); i++) {
                out.add(this.llrSingle(a.get(i)));
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return out;
    }
    private Double llrSingle(Double a) {
        Double x0 = a - 1;
        Double x1 = a + 1;
        Double r = (-x0*x0 + x1*x1);// / (2.0*this.var);
        return r;
    }
}

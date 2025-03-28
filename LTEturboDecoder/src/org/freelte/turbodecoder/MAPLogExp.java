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

import java.util.Iterator;
import java.util.List;
import org.freelte.utilities.ExceptionUtilities;

/**
 * The primary computation is
 * log(a_k) = z + log( exp(log(a0@k-1)) + exp(log(a1@k-1)) )
 * the output is log(a_k) which can then iterate for the k+1 index
 *
 * a is a function of (index, bit estimate, state)
 * index is known to be an iteration so that's a known
 * bit estimate must be given for the k-1 index
 * so a0 means the bit=0 for k-1 and similarly for a1
 *
 * The iterated measures in 'a' are stored as log transform
 * so the actual input for a0 is log(a0) @ k-1 index
 *
 * 'z' is the factor of the gaussian estimate that is
 * (x - d)^2 / v^2, x is the soft receive signal, d is the bit estimate often
 * written as d_hat, v^2 is the variance of the noise, x = d_true + noise
 *
 * @author Chris Deng
 */
class MAPLogExp {

    /**
     * computes log(s) for BCJR, uses jacobi form for numerical stability
     * the past values could be causal or anticausal, k-i where i = 1, -1.
     * In the backward estimates, i = -1.
     * s is the state metric
     * @param logS0_past at k-i index, log(a0) of d=0
     * @param logS1_past at k-i index, log(a1) of d=1
     * @param z at k index, the gaussian argument
     * @return
     */
    static public Double getLogS(Double logS0_past, Double logS1_past, Double z) throws Exception {
        Double out;
        Double big, small;
        big = (logS0_past > logS1_past) ? logS0_past : logS1_past;
        small = (logS0_past > logS1_past) ? logS1_past : logS0_past;
        out = z + big + Math.log(1.0 + Math.exp(small - big));
        if (out.isNaN()) {
            out = 0.0;
            ExceptionUtilities exu = new ExceptionUtilities();
            exu.logger("MAPLogExp.getLoga", new Exception("computed NaN"));
            throw new Exception("computed NaN in MAP getLoga");
        }
        return out;
    }

    static public Double getLogExpGeneral(List<Double> v) {
        Double max = v.get(0);
        Iterator<Double> i = v.iterator();
        //find max of v
        while (i.hasNext()) {
            Double t = i.next();
            if (t > max) {
                max = t;
            }
        }
        //numerical stable computations of log( Sum(exp...) )
        Double sum = 0.0;
        i = v.iterator();
        while (i.hasNext()) {
            sum += Math.exp(i.next() - max);
        }
        return (max + Math.log(sum));
    }

}

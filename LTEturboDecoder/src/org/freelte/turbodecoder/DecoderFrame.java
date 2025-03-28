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
 * Contains the soft output from BCJR, the extrinsic info, and the LLR from demod
 *
 * Decoder out and ext info before the interleaver are postfix with 1. after the
 * interleaver use postfix 2
 *
 * All of the vectors are read & write accessable
 *
 * @author Chris Deng
 */
public class DecoderFrame {

    private ExceptionUtilities exu = new ExceptionUtilities();
    /**
     * pointer to EncoderFrame, set by demod object which is not public in demo
     * release.  useful for debugging.
     */
    public EncoderFrame x;
    /**
     * data bits copied from the EncoderFrame, used for BER/FER probes
     */
    public List<Integer> d0;
    //public List<Integer> states;
    /**
     * soft output of decoder 1
     */
    public List<Double> sod1; //output of dec1
    /**
     * soft output of decoder 2
     */
    public List<Double> sod2; //output of dec2
    /**
     * LLR of data bits w/ term bits so +3 bits longer than IL size
     */
    public List<Double> llr0;
    /**
     * LLR of parity 1, before interleaver, w/ +3 term bits
     */
    public List<Double> llr1;
    /**
     * LLR of parity 2, after interleaver, w/ +3 term bits
     */
    public List<Double> llr2;

    /**
     * interleaved of llr0 w/ term bits so +3 bits longer than IL size
     */
    public List<Double> illr0;

    /**
     * = llr1 + ext2, input to decoder 1
     */
    public List<Double> iDec1; //llr0 + ext2, (w/ term bits)

    /**
     * = sod1 - ext2, input to decoder 2
     */
    public List<Double> iDec2; //sod1 - ext2, input to dec2 (w/ term bits)
    /**
     * = sod2 - iDec2, extrinsic info from decoder 2, feedback to decoder 1
     */
    public List<Double> ext2; //llr2 - innov1
    /**
     * decoded bits, hard sliced to 0/1
     */
    public List<Integer> bitsHat;
    public Double var;

    /**
     * constructor
     *
     * @param X EncoderFrame
     */
    public DecoderFrame(EncoderFrame X) {
        this.init();
        this.var = X.var;
        this.d0 = X.d0;
    }

    private void init() {
        ext2 = new ArrayList<>();
        for (int i = 0; i < LteSpecs.turboILSize; i++) {
            ext2.add(0.0);
        }
    }

    /**
     * trim a [start, end)
     * @param a
     * @param start
     * @param end
     */
    public void trimmer(List<Double> a, int start, int end) {
        try {
            Integer endsize = a.size() - end + start;
            for (int i = 0; i < end-start; i++) {
                a.remove(start);
            }
            if (a.size() != endsize) {
                throw new Exception("Error in vector trimmer!!");
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
}

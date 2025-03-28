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

import org.freelte.utilities.ExceptionUtilities;
import java.util.*;

/**
 * Perform turbo decoding using BCJR and iterative decoding procedure.
 * @author Chris Deng
 */
public class TurboDecoder {
    private InternalInterleaver<Double> I = new InternalInterleaver<>();
    private DecoderFrame turboFrame;
    private BCJR bcjr;
    private ExceptionUtilities exu = new ExceptionUtilities();
    private Integer decodeCycle;
    private Integer maxIterations = 10;
    private Integer[] errHistory;
    /**
     * decodes one frame.  DecoderFrame must be properly formatted with
     * termination bits.  Use the AlignDecoderFrame object to format
     * the frame before passing it to decoding.
     * @return number of bit errors in this frame
     */
    public Integer decode(DecoderFrame f) {
        bcjr = new BCJR(LteSpecs.turboDecSize); //must match f.llr0.size()
        turboFrame = f;
        //use 3 errors from decode cycle, if error count doesn't change
        //early exit okay
        //perform turbo iterations
        decodeCycle = 0;
        errHistory = new Integer[3];
        Double b = .25; //f.var/4.0 was best
        do {
            f.iDec1 = this.vectorOp(f.ext2, f.llr0, "+");
            f.iDec1 = this.scaleVector(f.iDec1, b);
            f.sod1 = bcjr.softDecode(f, false);

            //create innovation, but remove excess term bits
            f.trimmer(f.sod1, LteSpecs.turboILSize, LteSpecs.turboDecSize);
            List<Double> innov = this.vectorOp(f.sod1, f.ext2, "-");
            innov = I.interleave(innov); //interleave
            //prep input to Dec2
            f.iDec2 = this.vectorOp(f.illr0, innov, "+");
            //scale iDec2 so it doesn't grow so much
            f.iDec2 = this.scaleVector(f.iDec2, b);
            //do dec2
            f.sod2 = bcjr.softDecode(f, true);

            //f.sod2 = this.scaleVector(f.sod2, .5);
            //update ext2, then excess term bits
            List<Double> ext2 = this.vectorOp(f.sod2, f.iDec2, "-");
            f.trimmer(ext2, LteSpecs.turboILSize, LteSpecs.turboDecSize);
            //deinterleave ext2
            turboFrame.ext2 = I.deInterleave(ext2);
            f.trimmer(f.sod2, LteSpecs.turboILSize, LteSpecs.turboDecSize);
            List<Double> softOut = I.deInterleave(f.sod2);
            f.bitsHat = this.estimateBits(softOut);
            this.errHistory[0] = this.getFrameErrors();

        } while (!this.earlyExit(errHistory));
        return errHistory[0];
    }
    private List<Double> scaleVector(List<Double> a, Double b) {
        List<Double> c = new ArrayList<>();
        c.add( Math.abs(b) );
        return this.vectorOp(a, c, "*");
    }
    private Integer getFrameErrors() {
        Integer count = 0;
        try {
            for (int i = 0; i < LteSpecs.turboILSize; i++) {
                if (turboFrame.d0.get(i) != turboFrame.bitsHat.get(i)) {
                    count++;
                }
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return count;
    }
    private boolean earlyExit(Integer[] e) {
        //propagate error register
        e[2] = e[1]; e[1] = e[0];

        if (++this.decodeCycle > this.maxIterations || e[0] == 0) {
            return true;
        } else {
            return false;
        }

    }
    private List<Integer> estimateBits(List<Double> so) {
        List<Integer> b = new ArrayList<>();
        try {
            for (int i = 0; i < so.size(); i++) {
                b.add( (so.get(i) > 0.0) ? 0 : 1 );
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return b;
    }
    private List<Double> vectorOp(List<Double> a, List<Double> b, String op) {
        List<Double> out = new ArrayList<>();
        try {
            Integer L;
            if (a.size() < b.size()) {
                L = a.size();
            } else {
                L = b.size();
            }
            if (op.equals("+")) {
                for (int i = 0; i < L; i++) {
                    out.add(a.get(i)+b.get(i));
                }
            } else if (op.equals("-")) {
                for (int i = 0; i < L; i++) {
                    out.add(a.get(i)-b.get(i));
                }
            } else if (op.equals("*")) {
                L = a.size(); //b is a scaler
                for (int i = 0; i < L; i++) {
                    out.add(a.get(i) * b.get(0));
                }
            } else {
                throw new Exception("Operator for _vectorOp is not recognized.");
            }

            //stretch out to longer of the two vectors
            //fill the expansion with the longer vector values
            if (out.size() < a.size()) {
                out.addAll( a.subList(out.size(), a.size()) );
            } else if (out.size() < b.size()) {
                out.addAll( b.subList(out.size(), b.size()) );
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return out;
    }
}

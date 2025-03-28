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
import org.freelte.utilities.DummyBlocks;
import org.freelte.utilities.ExceptionUtilities;

/**
 * Encodes bits using LTE Turbo encoder.  Frame is stored as EncoderFrame and
 * the termination sequence is stored at indices according to LTE spec.
 * @author Chris Deng
 */
public class TurboEncoder {
    private InternalInterleaver<Integer> I = new InternalInterleaver<>();
    private ConvCodeTriple cc1; //encoder before interleaver
    private ConvCodeTriple cc2; //encoder after interleaver
    private Trellis trellis = new Trellis();
    private final Integer K = LteSpecs.turboILSize; //same as LTE spec 5.1.3.2.2
    private ExceptionUtilities exu = new ExceptionUtilities();

    public TurboEncoder() {
        cc1 = new ConvCodeTriple();
        cc1.state = 0;
        cc2 = new ConvCodeTriple();
        cc2.state = 0;
    }
    /**
     * Encode the given frame.  parity bits are updated with turbo encoder
     * outputs.
     * @param X encoder frame with data bits already populated
     */
    public void encodeFrame(EncoderFrame X) {
        this.encode1(X);
        this.encode2(X);
        this.formFrame(X); //finalize the LTE term frame
    }
    /**
     * encode before the interleaver
     * updates to frame's parity bit
     *
     * Trellis termination is not packed exactly
     * in same time index as the EncoderFrame bits
     * so encoder1 stores the outputs to temporary
     * arrays in the object and after encoder2
     * gets its parities, all of the termination bits
     * are placed to the Encoded Frame according to
     * LTE 5.1.3.2.2
     *
     * @param X LteEncFrame, d2 parity is updated
     *
     */
    private void encode1(EncoderFrame X) {
        try {
            cc1.state = 0;
            for (int i=0; i<this.K; i++) {
                cc1 = trellis.forward(cc1.state, X.d0.get(i));
                X.d1.set(i, cc1.parity);
            }

            //termination bits of the trellis are 0's
            //the way LTE defines placement of the bits
            //is time sequence pack to the 3-bit triple
            //of the encoder output
            //see LTE 5.1.3.2.2
            Integer tmp = trellis.getTermfb(cc1.state); //get K-1 fb
            //once the switch closes, the state fb is 0
            //but the switch fb bit is tmp and used to gen next state
            cc1 = trellis.forward(cc1.state, tmp); //gen K
            //X.d0term.add( trellis.getTermfb(cc1.state)); //K
            X.d0term.add(tmp);
            X.d1term.add( cc1.parity); //K
            tmp = trellis.getTermfb(cc1.state);
            //update state K+1
            cc1 = trellis.forward(cc1.state, tmp); //K + 1
            X.d2term.add(tmp);
            X.d0term.add(cc1.parity);
            tmp = trellis.getTermfb(cc1.state);
            //to K+2
            cc1 = trellis.forward(cc1.state, tmp); //K+2
            X.d1term.add( tmp);
            X.d2term.add( cc1.parity);
            //should have ended at state 0, otherwise ERROR
            if (!cc1.state.equals(0)) {
                throw new Exception("Trellis not terminating to 0 state!");
            }
            //the rest of array is to store after interleaver
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
    private void encode2(EncoderFrame X) {
        try {
            cc2.state = 0;
            //first interleave all the data bits
            List<Integer> d = I.interleave(X.d0);
            for (int i=0; i<this.K; i++) {
                cc2 = trellis.forward(cc2.state, d.get(i));
                X.d2.set(i, cc2.parity);
            }

            //termination bits, bit packing of termination bits
            //see LTE 5.1.3.2.2
            Integer tmp = trellis.getTermfb(cc2.state); //termination feedback
            cc2 = trellis.forward(cc2.state, tmp); //gen K
            X.d0term.add(tmp); //x' K
            X.d1term.add(cc2.parity);
            tmp = trellis.getTermfb(cc2.state);
            //to K+1
            cc2 = trellis.forward(cc2.state, tmp);
            X.d2term.add( tmp);
            X.d0term.add( cc2.parity);
            tmp = trellis.getTermfb(cc2.state);
            //gen K+2
            cc2 = trellis.forward(cc2.state, tmp);
            X.d1term.add( tmp);
            X.d2term.add( cc2.parity);
            //state should have ended in 0
            if (!cc2.state.equals(0)) {
                throw new Exception("encoder2 didn't terminate in state 0");
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
    private void formFrame(EncoderFrame x) {
        try {
            x.d0.addAll(x.d0term);
            x.d0term.clear();
            x.d1.addAll(x.d1term);
            x.d1term.clear();
            x.d2.addAll(x.d2term);
            x.d2term.clear();

            if (x.d0.size() != LteSpecs.ILplusTermSize ||
                    x.d1.size() != LteSpecs.ILplusTermSize ||
                    x.d2.size() != LteSpecs.ILplusTermSize) {
                throw new Exception("Encoder Frame, final size not LTE IL+Term.");
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
}

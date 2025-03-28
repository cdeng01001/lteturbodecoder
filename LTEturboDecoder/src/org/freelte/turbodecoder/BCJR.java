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
 * forward estimates are stored as tensor of 3rd order of dimensions frameLength
 * X BitsAlphabetSize X NumStates
 *
 * frameLength can be obtained from the encoded frame BitsAlphabetSize = 2,
 * that's obvious NumStates = 8, for LTE conv. codes this can be get from
 * Trellis.getNumStates or from the LteSpecs.numStatesCC
 *
 * @author Chris Deng
 */
class BCJR {

    private Double[][][] tensorf; //forward tensor, log(f)
    private Double[][][] tensorb; //backward tensor, log(b)
    //help debugging
    //private Double[][] fSum; //sum all states for forward
    //private Double[][] bSum; //sum all states for backward

    private Double var;
    private Trellis trellis = new Trellis();
    private List<Double> dn; //receive data
    private List<Double> pn; //receive parity
    private ExceptionUtilities exu = new ExceptionUtilities();
    private final Double lowestLLR = -1.0e50;
    private Double[][][] bK;
    /**
     * construction
     *
     * @param frameLength
     */
    public BCJR(Integer FrameLength) {
        tensorf = new Double[FrameLength][2][Trellis.getNumStates()];
        tensorb = new Double[FrameLength][2][Trellis.getNumStates()];
        bK = new Double[4][][]; //for debugging use
        for (int i = 1; i <= 4; i++) {
            //bK[i-1] = tensorb[22+i];
            bK[i-1] = tensorb[FrameLength-i];
        }
    }
    /**
     * block estimate with BCJR and soft output
     *
     * @return soft output of estimates
     */
    public List<Double> softDecode(DecoderFrame X, boolean isInterleaved) {
        this.setBitAndParity(X, isInterleaved);
        return this.bcjrCore();
    }

    private List<Double> bcjrCore() {
        //compute forward and backward
        //populates the tensors
        this.forward();
        this.backward();
        //this._fbSum(); //for debug use. the _ prefix
        return this.getLLR();
    }
    private List<Double> getLLR() {
        //straight write of LLR is log( p(d=0) / p(d=1) )
        //since p(d=0/1) = sum( states of tensors )
        //log( sum(tensors) ) = sum( log(tensors) )
        //basically, the this.tensors are already computed
        //using BCJR and log() transformation
        //so we just need to use sum()
        //List<Double> p = dbs.get00Block(2);
        List<Double> p0 = new ArrayList<>();
        List<Double> p1 = new ArrayList<>();
        List<Double> llr = new ArrayList<>();
        try {
            //sum the states and place into p0
            for (int k = 0; k < LteSpecs.turboDecSize; k++) {
                p0.clear();
                p1.clear();
                for (int s = 0; s < Trellis.getNumStates(); s++) {
                    Double fo = tensorf[k][0][s];
                    //by Deng's convention, the forward state
                    //hold the forward branch data and backward
                    //state holds the backwards data
                    //so the fore and back are one index offset
                    Double ba = tensorb[k][0][s];
                    p0.add(fo+ba);
                    p1.add(tensorf[k][1][s] + tensorb[k][1][s]);
                }

                //llr.add( Math.log(p.get(0)) - Math.log(p.get(1)) );
                Double llr0 = MAPLogExp.getLogExpGeneral(p0);
                Double llr1 = MAPLogExp.getLogExpGeneral(p1);
                llr.add( llr0 - llr1 );
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return llr;
    }
    /**
     * compute all forward estimates, populate the entire forward tensor
     *
     * @param X Encoded Frame of LTE turbo 3-bit symbols
     * @param isInterleaved true if trellis after interleaver
     */
    private void setBitAndParity(DecoderFrame f, boolean isInterleaved) {
        this.var = f.var;
        if (isInterleaved) {
            this.dn = f.iDec2;
            this.pn = f.llr2;
        } else {
            this.dn = f.iDec1;
            this.pn = f.llr1;
        }
    }
    /**
     * compute all forward estimates, populate forward tensor
     */
    private void forward() {
        //first 3 trellis steps have many states that are
        //improbable
        for (int s = 0; s < Trellis.getNumStates(); s++) {
            for (int k = 0; k < 1; k++) {
                tensorf[k][0][s] = this.lowestLLR;
                tensorf[k][1][s] = this.lowestLLR;
            }
        }
        //at k=0, the trellis terminates at state 0 with certainty
        this.forwardState0(); //at k=0, the trellis starts at 0
        // all states are possible
        // strictly the trellis doesn't go through all states
        // until two more transitions but that's unmeas. loss
        for (int k = 1; k < dn.size(); k++) {
            this.allForwardBits(k);
        }
        //at this point, the branch metric was added to the last-1 state
        //but we know how it will toggle so we carry through until trellis
        //ends at 0, which is a given.

    }

    /**
     * loops allForwardStates, updates forward tensor
     *
     * @param k
     */
    private void allForwardBits(Integer k) {
        for (int b = 0; b < 2; b++) {
            for (int s = 0; s < LteSpecs.numStatesCC; s++) {
                this.updateForwardTensor(k, b, s);
            }
        }
    }

    /**
     * updates forward tensor with MAP
     *
     * @param k
     * @param b
     * @param s
     */
    private void updateForwardTensor(Integer k, Integer b, Integer s) {
        try {
            Double z = this.getArg(k, b, s);
            Integer s_past0 = trellis.backward(s, 0).state;
            Integer s_past1 = trellis.backward(s, 1).state;
            //tensors must normalize as trellis propagate
            //use the 0 state of past
            Double mapl = MAPLogExp.getLogS(tensorf[k - 1][0][s_past0],
                    tensorf[k - 1][1][s_past1], z);
            Double norm = tensorf[k-1][0][0];
            tensorf[k][b][s] =  mapl - norm;
        } catch (Exception e) {
            System.out.println(String.format("k=%d, b=%d, s=%d", k, b, s));
            exu.logger(this.getClass().getName(), e);
        }
    }

    private void forwardState0() {
        try {
            //at first iteration, state 0 propagate only two other states
            //b=0, next state = 0;
            //b=1, next state = 4;
            Double z = this.getArg(0, 0, 0);
            //b=0, goes from state 0 to 0 with certainty
            //nothing can be predicted of past values, so input to the
            //Log function are two equal likely branches, both using 0.0
            tensorf[0][0][0] = MAPLogExp.getLogS(0.0, 0.0, z);
            //b=1, goes from state 0 to 4 with certainty, and that's all
            //for the first trellis transitions
            z = this.getArg(0, 1, 0);
            tensorf[0][1][0] = MAPLogExp.getLogS(0.0, 0.0, z);
        } catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }
    private void backward() {
        for (int s = 0; s < Trellis.getNumStates(); s++) {
            for (int k = 0; k < 1; k++) {
                this.tensorb[dn.size() - 1 - k][0][s] = this.lowestLLR;
                this.tensorb[dn.size() - 1 - k][1][s] = this.lowestLLR;
            }
        }
        this.backwardStateLast0(); //K-1
        //this.backwardStateLast1(); //K-2
        //Last 2 is same as any other because in backward tensor
        //the last index assume final states 0 and 1 which must
        //terminate to state 0
        //this.backwardStateLast2();
        for (int k = dn.size() - 2; k >= 0; k--) {
            this.allBackwardBits(k);
        }
    }
    /**
     * update backward tensor last index K-1
     */
    private void backwardStateLast0() {
        try {
            //the ending state must be 0
            //so at last index, the two possible states
            //are 0 and 1. these states can go to 0
            Integer k = this.dn.size() - 1;
            Double z = this.getArg(k, 0, 0);
            tensorb[k][0][0] = MAPLogExp.getLogS(0.0, 0.0, z);
            //backward is a little weird

            //next the branch to state 0 with b=1
            //this must be from state 1 given trellis
            z = this.getArg(k, 1, 0);
            tensorb[k][1][1] = MAPLogExp.getLogS(0.0, 0.0, z);

        } catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }

    private void allBackwardBits(Integer k) {
        for (int b = 0; b < 2; b++) {
            for (int s = 0; s < Trellis.getNumStates(); s++) {
                this.updateBackwardTensor(k, b, s);
            }
        }
    }

    private void updateBackwardTensor(Integer k, Integer b, Integer s) {
        try {
            Double z = this.getArg(k, b, s);
            //in backward estimation, the b decides the future state
            Integer s_future = trellis.forward(s, b).state;
            //that future state could have two sets of metrics
            //the b=0 and b=1 where b is the future++ bit
            //so this is the part that is a little different
            //look than the forward tensor and that's because
            //LogS function is applied on the same future state
            //given different b's of the future state, i.e. b[k+1]
            //for the b of this state, i.e. b[k], it'll change
            //by outer loop and future state would change coorespondingly
            //and the corresponding metric is stored into a
            //different location of the tensor
            Double mapl = MAPLogExp.getLogS(tensorb[k+1][0][s_future],
                    tensorb[k+1][1][s_future], z);
            tensorb[k][b][s] = mapl - tensorb[k+1][0][0];
        } catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
    }

    /**
     *
     * @param k frame index
     * @param b input bit
     * @param s current state
     * @param dir true is forward, else backwards
     * @return
     */
    private Double getArg(Integer k, Integer b, Integer s) {
        ConvCodeTriple c;
        c = trellis.forward(s, b);
        Integer p = c.parity;
        Double parity = this.pn.get(k);
        Double bit = this.dn.get(k);
        Double bpsk_bit = LteSpecs.BitSymbols.getSym(b);
        //Double bpsk_bit = b.doubleValue();
        Double bpsk_parity = LteSpecs.BitSymbols.getSym(p);
        //Double bpsk_parity = p.doubleValue();
        Double z = MAPArg.computeArg(bit, bpsk_bit,
                parity, bpsk_parity, this.var);
        return z;
    }
}

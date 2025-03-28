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

/**
 *
 * @author Chris Deng
 */
public class LteSpecs {
    /**
     * turboTermLen is 4, the packing of 8 state conv. coders
     * of rate 1/3 turbo into the 3-bit symbols of turbo frame
     * gives extra 4 symbols in the turbo frame
     */
    public static final Integer turboTermLen = 4; //number of trellis termination bits
    /**
     * the termination length of the conv. code, 8 state is 3 shifts
     */
    public static final Integer ccTermLen = 3;
    /**
     * number of states of constituent code is 8
     */
    public static final Integer numStatesCC = 8;
    /**
     * Internal interleaver of turbo is 6144, longest of LTE
     * other interleavers are not yet implemented
     */
    public static final Integer turboILSize = 6144; //bits
    /**
     * Encoder Size 6148, which must contain at least IL bits + term. sym's
     */
    public static final Integer ILplusTermSize = 6148; //IL size + 4 symbols
    /**
     * Decoder frame extends exactly the bits, which is +3 more bits
     * the definition of LTE framing is +4 symbols.
     */
    public static final Integer turboDecSize = 6147; //IL size + 3 bits

    /**
     * Bit mapping used for LLR. Bit 0 = 1, 1 = -1.
     * The encoding informs the metric in MAP the scaling
     * of each bit estimate.
     */
    public static class BitSymbols {
        /**
         * give BPSK symbols from bits, 0->1, 1->-1
         * @param bit 0 or 1
         * @return 1 or -1, 0 -> 1, 1 -> -1
         */
        public static Double getSym(Integer bit) {
            return (bit == 0) ? 1.0 : -1.0;
        }
    }
}

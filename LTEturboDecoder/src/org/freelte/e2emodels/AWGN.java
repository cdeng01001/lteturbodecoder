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

import org.freelte.turbodecoder.EncoderFrame;
import org.freelte.random.WGNgen;

/**
 * Additive White Gaussian Noise
 * @author Chris Deng
 */
class AWGN {
    private WGNgen wgn;
    private Double var;

    public AWGN(Double snr) {
        // var = No/2, signal power is normalized to 1.0
        // so var = 1/2 * 1/snr
        this.var = 0.5 * Math.pow(10.0, -snr/10.0);
        wgn = new WGNgen(0.0, Math.sqrt(var));
    }
     /**
     * produce dn and pn
     * dn = d + wgn
     * pn = p + wgn
     * @param snr (dB) channel SNR w.r.t chips
     */
    public void addWGN(EncoderFrame X) {
        X.var = this.var;
        for (int i = 0; i<X.d0n.size(); i++) {
            X.setTripleRxAdd(i, wgn.getValue(), wgn.getValue(), wgn.getValue());
            //X.setTripleRxAdd(i, 0.0, 0.0, 0.0);
        }
    }
}

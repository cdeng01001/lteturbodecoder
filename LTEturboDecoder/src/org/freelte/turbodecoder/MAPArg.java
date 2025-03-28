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
 * contains the estimation-necessary argument that's part of MAP.
 *
 * @author Chris Deng
 */
class MAPArg {
    /**
     * the symbol bit and parity, d and p, must be symbols
     * from the modulation.  In this E2E sim, BPSK is used so
     * it should be 1 or -1.
     * @param dx
     * @param d
     * @param px
     * @param p
     * @param var
     * @return
     */
    static public Double computeArg(Double dx, Double d,
            Double px, Double p, Double var) {
        return (d*dx + p*px);// / var;
    }
}

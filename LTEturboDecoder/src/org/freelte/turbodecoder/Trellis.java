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
import java.lang.*;

/**
 *
 * @author Chris Deng
 */
class Trellis {
    /**
     * LTE trellis is the conv code
     * @return number of states for LTE trellis
     */
    public static Integer getNumStates() {
        return LteSpecs.numStatesCC;
    }
    /**
     * forward iterate once from state 0
     * @return Two states that propagated from state 0
     */
    static public List<Integer> getStatesFrom0() {
        List<Integer> s = new ArrayList<>();
        s.add(0);
        s.add(4);
        return s;
    }
    /**
     * This is the forward iterate twice from state 0
     * @return get Four states from states 0, 4
     */
    public static List<Integer> getStatesFrom04() {
        List<Integer> s = new ArrayList<>();
        //state 0 goes to 0 and 4
        s.add(0);
        s.add(4);
        //state 4 goes to 2 and 6
        s.add(2);
        s.add(6);
        return s;
    }
    /**
     * return states that toggle into 0
     * LTE is 0 and 1 states propagate into 0
     * @return List<Integer> states
     */
    public static List<Integer> getStatesTo0() {
        List<Integer> s = new ArrayList<>();
        s.add(0);
        s.add(1);
        return s;
    }
    /**
     * return states and propagate into 0 and 1
     * LTE trellis are 0,1,2,3 can propagate into 0 and 1
     * after these states, any of 8 states can propagate
     * into these four states
     * @return
     */
    public static List<Integer> getStatesTo01() {
        List<Integer> s = new ArrayList<>();
        s.add(0); s.add(1); s.add(2); s.add(3);
        return s;
    }
    /**
     * create new Triple of next state
     * @param state current state
     * @param bits input bits
     * @return
     */
    public ConvCodeTriple forward(Integer state, Integer bit) {
        ConvCodeTriple next = new ConvCodeTriple();
        Integer b = bit;
        ExceptionUtilities exu = new ExceptionUtilities();
        next.bit = b;
        try {
            switch (state) {
                case 0:
                    if (b == 0) {
                        next.state = 0;
                        next.parity = 0;
                    } else {
                        next.state = 4;
                        next.parity = 1;
                    }
                    break;
                case 1:
                    if (b == 0) {
                        next.state = 4;
                        next.parity = 0;
                    } else {
                        next.state = 0;
                        next.parity = 1;
                    }
                    break;
                case 2:
                    if (b == 0) {
                        next.state = 5;
                        next.parity = 1;
                    } else {
                        next.state = 1;
                        next.parity = 0;
                    }
                    break;
                case 3:
                    if (b == 0) {
                        next.state = 1;
                        next.parity = 1;
                    } else {
                        next.state = 5;
                        next.parity = 0;
                    }
                    break;
                case 4:
                    if (b == 0) {
                        next.state = 2;
                        next.parity = 1;
                    } else {
                        next.state = 6;
                        next.parity = 0;
                    }
                    break;
                case 5:
                    if (b == 0) {
                        next.state = 6;
                        next.parity = 1;
                    } else {
                        next.state = 2;
                        next.parity = 0;
                    }
                    break;
                case 6:
                    if (b == 0) {
                        next.state = 7;
                        next.parity = 0;
                    } else {
                        next.state = 3;
                        next.parity = 1;
                    }
                    break;
                case 7:
                    if (b == 0) {
                        next.state = 3;
                        next.parity = 0;
                    } else {
                        next.state = 7;
                        next.parity = 1;
                    }
                    break;
                default:
                    throw new Exception("Trellis state is valid LTE CC, can't go forward");
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return next;
    }

    /**
     * the feedback output is defined by the LTE 136-212
     * Figure 5.1.3-2 at the termination switch point
     *
     * @param state current state
     * @return Feedback to the termination switch
     */
    public Integer getTermfb(Integer state) {
        Integer s0 = state & 1;
        Integer s1 = state & 2;
        s1 = s1 >> 1; //move the 2nd bit to primary bit position
        return (s0 ^ s1); //fb structure
    }
    /**
     * The trellis is deterministic.  so given current state and
     * previous or back bit, the previous state can be found.
     *
     * @param state current state
     * @param b backward bit, or previous bit that made trellis to current state
     * @return previous state by going backwards
     */
    public ConvCodeTriple backward(Integer state, Integer b) {
        ConvCodeTriple back = new ConvCodeTriple();
        //Integer p = parities.get(0);
        Integer p = 0; //fake
        ExceptionUtilities exu = new ExceptionUtilities();

        try {
            switch (state) {
                case 0:
                    this.backStateCheck(back, state, b, p, 0, 0, 1, 1);
                    break;
                case 1:
                    this.backStateCheck(back, state, b, p, 1, 3, 0, 2);
                    break;
                case 2:
                    this.backStateCheck(back, state, b, p, 1, 4, 0, 5);
                    break;
                case 3:
                    this.backStateCheck(back, state, b, p, 0, 7, 1, 6);
                    break;
                case 4:
                    this.backStateCheck(back, state, b, p, 0, 1, 1, 0);
                    break;
                case 5:
                    this.backStateCheck(back, state, b, p, 1, 2, 0, 3);
                    break;
                case 6:
                    this.backStateCheck(back, state, b, p, 0, 5, 1, 4);
                    break;
                case 7:
                    this.backStateCheck(back, state, b, p, 0, 6, 1, 7);
                    break;
                default:
                    throw new Exception("Trellis state is invalid LTE CC, can't go backward.");
            }
        }
        catch (Exception e) {
            exu.logger(this.getClass().getName(), e);
        }
        return back;
    }


    /**
     * check the backward state, bs0, bs1
     * bs0 is the back state given bit=0
     * parity is unambiguous given the current state because
     * LTE CC is rate 1/2
     * similarly, bs1 is back state given bit=1
     *
     * @param back is ConvCodeTriple to get update
     * @param state is current state
     * @param b to check bit
     * @param p to check parity
     * @param p0 valid parity at b = 0
     * @param p1 valid parity at b = 1
     * @param bs0 valid backward state at b = 0,p
     * @param bs1 valid backward state at b = 1,p
     * @throws Exception
     */
    void backStateCheck(ConvCodeTriple back, Integer state, Integer b, Integer p,
            Integer p0, Integer bs0, Integer p1, Integer bs1) throws Exception {
        if (b == 0) { //&& p == p0
            back.state = bs0;
            back.bit = b;
            back.parity = p0;
        } else if (b == 1) { // && p == p1
            back.state = bs1;
            back.bit = b;
            back.parity = p1;
        } else {
            String s = "b,p pair of (" + b.toString() + "," + p.toString() + ") is invalid LTE trellis, state = " + state.toString();
            throw new Exception(s);
        }
    }
}

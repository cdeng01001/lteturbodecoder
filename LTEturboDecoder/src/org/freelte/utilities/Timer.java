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
package org.freelte.utilities;

import java.lang.*;
import java.util.*;

/**
 * Estimates the real time speed between time markers
 * @author Chris Deng
 */
public class Timer {
	private Double[] movAvg= new Double[100];
        private Integer movPtr = 0;
	private Long _tick = (long)0;
	private Date _clk;
	private String _latest_estimate = "starting...";
        private boolean movStart = false;
        private ExceptionUtilities exu = new ExceptionUtilities();

	public Timer() {
            _clk = new Date();
            _tick = _clk.getTime();
        }
        /**
         * starts the timer
         */
	public void tick() {
            _tick = _clk.getTime();
            if (movPtr == movAvg.length) {
                this.movStart = true;
            }
            movPtr = movPtr % movAvg.length;
	}
        /**
         * stops the timer and calculate speed = nBits / period
         * @param nBits number of bits from tick to tock period
         */
	public void tock(Integer nBits) {
            try {
                _clk = new Date(); //update current time
                Double timer = ((double)_clk.getTime() - (double)_tick); //ms
                if (timer < 1e-9) {
                    timer = 0.1; //speed is really fast use .1 ms
                }
                movAvg[movPtr++] = nBits.doubleValue() / timer;
                Double avgSpd = this.calcMovAvg();
                _latest_estimate = String.format("Inst. spd: %.2f Kbps @ %s \n", movAvg[movPtr-1], _clk.toString());
                _latest_estimate += String.format("Avg Spd: %.2f Kbps", avgSpd);
            }
            catch (Exception e) {
                exu.logger(this.getClass().getName(), e);
            }
	}
        /**
         * return the instant speed in Kbps = nBits / this_period[ms] and
         * the average speed of last 100 periods.
         * @return
         */
	public String getEstimate() {
            return _latest_estimate;
	}
        private Double calcMovAvg() {
            if (movStart) {
                Double s = 0.0;
                for (int i = 0; i < movAvg.length; i++) {
                    s += movAvg[i];
                }
                return s / movAvg.length;
            } else {
                return 0.0;
            }
        }

}

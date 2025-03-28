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
package org.freelte.random;
import org.uncommons.maths.random.BinomialGenerator;
import java.util.Random;

/**
 * Generates random 1 and -1. P(-1) = p
 * new random seeding for each new object
 * @author Chris Deng
 */
public class BernoulliGen {
    BinomialGenerator bi;

    /**
     * probaility of -1 = p
     * new Random seeding for each new object
     * @param p probability of -1
     */
    public BernoulliGen(Double p) {
        bi = new BinomialGenerator(1, p, new Random());
    }
    /**
     * outputs 0 or 1
     * @return
     */
    public Integer getValue() {
        return bi.nextValue();
    }
}

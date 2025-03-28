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

import java.util.Random;
import org.uncommons.maths.random.GaussianGenerator;

/**
 * White Gaussian Noise,  random seeding for each new object
 * @author Chris Deng
 */
public class WGNgen {
    GaussianGenerator g;
    /**
     * use Double
     * @param mean the mean
     * @param stddev, or sqrt(variance)
     */
    public WGNgen(Double mean, Double stddev) {
        g = new GaussianGenerator(mean, stddev, new Random());
    }
    /**
     * get next value
     * @return next random value of normal(mean,var)
     */
    public Double getValue() {
        return g.nextValue();
    }

}

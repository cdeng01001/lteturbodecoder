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

/**
 * Probe the simulation statistics
 * @author Chris Deng
 */
class Probe {
    private Integer frameErrorsStop;
    private Integer frameErrCount;
    private Integer bitErrCount;
    private Long frameCount;
    private Long framesSim; //frames to simulate
    /**
     * stop the e2e sim at given frame errors
     * @param errorsStop
     */
    public Probe(Integer errorsStop, Long frames2Sim) {
        this.frameErrorsStop = errorsStop;
        this.framesSim = frames2Sim;
        this.clear();
    }
    public void setFrameErrs2Stop(Integer errors) {
        this.frameErrorsStop = errors;
    }
    public void setFrame2Sim(Long frames2Sim) {
        this.framesSim = frames2Sim;
    }
    /**
     * keep looping until frame error count > stoppage
     * @param bitErrInFrame
     * @return true is keep going
     */
    public boolean tip(Integer bitErrInFrame) {
        this.bitErrCount += bitErrInFrame;
        this.frameCount++;
        if (bitErrInFrame > 0) frameErrCount++;
        if (frameErrCount >= frameErrorsStop ||
                (frameCount > framesSim && framesSim > 0) ) {
            return false;
        } else {
            return true;
        }
    }
    public Double getBER(Integer bitsPerFrame) {
        return bitErrCount.doubleValue() / (bitsPerFrame.doubleValue()*frameCount.doubleValue());
    }
    public Double getFER() {
        return frameErrCount.doubleValue() / frameCount.doubleValue();
    }
    public Long getFrames() {
        return frameCount;
    }
    private void clear() {
        this.frameErrCount = 0;
        this.bitErrCount = 0;
        this.frameCount = 0L;
    }
}

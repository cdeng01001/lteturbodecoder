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

import org.freelte.utilities.*;
import org.freelte.turbodecoder.TurboEncoder;
import org.freelte.turbodecoder.TurboDecoder;
import org.freelte.turbodecoder.LteSpecs;
import org.freelte.turbodecoder.EncoderFrame;
import org.freelte.turbodecoder.DecoderFrame;
import org.freelte.turbodecoder.AlignDecoderFrame;
//import java.util.List;
//import java.util.ArrayList;
//import java.lang.StringBuilder;
/**
 *
 * @author Chris Deng
 */
class E2ESim {
    private EncoderFrame x;
    private TransmitGen tx = new TransmitGen();
    private TurboEncoder enc = new TurboEncoder();
    private AWGN awgn; // = new AWGN();
    private BPSKDemodLLR demod = new BPSKDemodLLR();
    private AlignDecoderFrame alignDF = new AlignDecoderFrame();
    private TurboDecoder dec = new TurboDecoder();
    private Probe probe = new Probe(10,0L);
    private TextGUI gui = new TextGUI();
    private Timer timer = new Timer();

    /**
     * Runs the LTE Turbo Code simulation
     * using BPSK end to end modeling
     */
    public void run(ConfigSettings cfg) {
        gui.setVisible(true);
        awgn = new AWGN(cfg.snr);
        boolean goSim = false;
        probe.setFrameErrs2Stop(cfg.maxFrameErrs);
        probe.setFrame2Sim(cfg.maxFrames2Sim);
        do {
            timer.tick();
            x = new EncoderFrame(); //another frame
            tx.genData(this.x); //generate random/uniform 0,1 data
            enc.encodeFrame(x); //turbo encode d0 to rate 1/3, parities d1, d2
            tx.transmit(this.x); //all symbols are in bpsk +/-1, frame follows LTE term bits packing
            awgn.addWGN(this.x);
            DecoderFrame f = demod.demod(x); //LLR
            alignDF.align(f); //position term bits at their normal positions
            goSim = probe.tip( dec.decode(f) ); //BCJR and turbo decode
            timer.tock(LteSpecs.turboILSize);
            this.sysout();

        } while (goSim);
        gui.appendStatus("Close this window to shutdown simulator!");
        this.fileOut("./lte.out");
    }
    private String buildMsg() {
        StringBuilder status = new StringBuilder();
        status.append(String.format("BitER = %.1e",probe.getBER(6144))).append("\n");
        status.append(String.format("FER is: %.4f",probe.getFER())).append("\n");
        status.append("Frames Simulated: " + probe.getFrames().toString()).append("\n");
        status.append(timer.getEstimate()).append("\n");
        return status.toString();
    }
    private void sysout() {
        gui.updateStatus(this.buildMsg());
    }
    private void fileOut(String fn) {
        FileUtilities fu = new FileUtilities();
        fu.writeTextToFile(fn, buildMsg());
    }
}

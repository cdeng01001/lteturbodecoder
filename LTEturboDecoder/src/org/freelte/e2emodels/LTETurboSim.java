/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freelte.e2emodels;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Run demo end to end simulation of the LTE turbo codec.
 * Uses BPSK and AWGN as demo simulation.
 *
 * @author chrisd
 */
public class LTETurboSim {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //get config info
        ReadConfig reader = new ReadConfig();
        try {
            ConfigSettings cfg = reader.read(args);
            E2ESim sim = new E2ESim();
            sim.run(cfg);
        }
        catch (Exception e) {
            Logger.getLogger("main program").log(Level.SEVERE, null, e);
        }


    }

}

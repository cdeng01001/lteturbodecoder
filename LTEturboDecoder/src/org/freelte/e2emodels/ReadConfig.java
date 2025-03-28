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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
/**
 * crude config reader.
 * file format is line serial per parameter, # is comment line
 * comments not allowed on parameter line
 * sequence of params are: snr, max frame errors, max frames to sim.
 * 
 * @author Chris Deng
 */
class ReadConfig {
    public ConfigSettings read(String[] args) throws Exception {
        ConfigSettings cfg = new ConfigSettings();
        if (args[0].equals("-f")) {
            FileUtilities fu = new FileUtilities();
            List<String> fcontents = fu.readEntireTextFile(args[1]);
            int i = 0;
            for (String fline : fcontents) {
                //skip comment lines
                if (!fline.startsWith("#") && !fline.isEmpty()) {
                    switch (i++) {
                        case 0:
                            cfg.snr = Double.parseDouble(fline);
                            break;
                        case 1:
                            cfg.maxFrameErrs = Integer.parseInt(fline);
                            break;
                        case 2:
                            cfg.maxFrames2Sim = Long.parseLong(fline);
                            break;
                        default:
                            throw new Exception("invalid contents/format in cfg file " + args[1]);
                    }
                }
            }
        } else {
            //can't recognize switch
            TextGUI cfgMsg = new TextGUI();
            String msg = "Can't recognize this switch: " + args[0];
            msg += "\n usage: -f <cfg file name>";
            cfgMsg.updateStatus(msg);
            cfgMsg.appendStatus("close this GUI to exit program.");
        }
        return cfg;
    }
}

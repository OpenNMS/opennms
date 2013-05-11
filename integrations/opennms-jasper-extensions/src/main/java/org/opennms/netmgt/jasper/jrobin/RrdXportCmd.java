/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.jrobin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.jrobin.data.DataProcessor;
import org.jrobin.graph.RrdGraphConstants;

class RrdXportCmd extends RrdToolCmd implements RrdGraphConstants {
    private DataProcessor dproc;
    private List<XPort> xports;

    @Override
    String getCmdType() {
        return "xport";
    }

    @Override
    JRDataSource execute() throws RrdException, IOException {
        String startStr = getOptionValue("s", "start", DEFAULT_START);
        String endStr = getOptionValue("e", "end", DEFAULT_END);
        long span[] = Util.getTimestamps(startStr, endStr);
        dproc = new DataProcessor(span[0], span[1]);
        xports = new ArrayList<XPort>();
        long step = parseLong(getOptionValue(null, "step", "1"));
        int maxRows = parseInt(getOptionValue("m", "maxrows", "400"));
        long minStep = (long) Math.ceil((span[1] - span[0]) / (double) (maxRows - 1));
        step = Math.max(step, minStep);
        dproc.setFetchRequestResolution(step);
        dproc.setStep(step);
        String[] words = getRemainingWords();
        if (words.length < 1) {
            throw new RrdException("Incomplete XPORT command");
        }
        for (int i = 0; i < words.length; i++) {
            if (words[i].startsWith("DEF:")) {
                parseDef(words[i]);
            }
            else if (words[i].startsWith("CDEF:")) {
                parseCDef(words[i]);
            }
            else if (words[i].startsWith("XPORT:")) {
                parseXport(words[i]);
            }
            else {
                throw new RrdException("Invalid XPORT syntax: " + words[i]);
            }
        }
        JRDataSource result = xports.size() == 0 ? null : xport();
        println(xports.size() == 0 ? "No XPORT statement found, nothing done" : result.toString());
        return result;
    }

    private JRDataSource xport() throws IOException, RrdException {
        dproc.processData();
        long[] timestamps = dproc.getTimestamps();
        
        for(int i = 0; i < timestamps.length; i++) {
            timestamps[i] = timestamps[i] - dproc.getStep();
        }
        
        for (XPort xport : xports) {
            xport.values = dproc.getValues(xport.name);
        }
        return new JRobinDataSource(dproc.getStep(), timestamps, xports);
    }

    private void parseDef(String word) throws RrdException {
        // DEF:vname=rrd:ds-name:CF
        String[] tokens1 = new ColonSplitter(word).split();
        if (tokens1.length != 4) {
            throw new RrdException("Invalid DEF syntax: " + word);
        }
        String[] tokens2 = tokens1[1].split("=");
        if (tokens2.length != 2) {
            throw new RrdException("Invalid DEF syntax: " + word);
        }
        dproc.addDatasource(tokens2[0], tokens2[1], tokens1[2], tokens1[3]);
    }

    private void parseCDef(String word) throws RrdException {
        // CDEF:vname=rpn-expression
        String[] tokens1 = new ColonSplitter(word).split();
        if (tokens1.length != 2) {
            throw new RrdException("Invalid CDEF syntax: " + word);
        }
        String[] tokens2 = tokens1[1].split("=");
        if (tokens2.length != 2) {
            throw new RrdException("Invalid CDEF syntax: " + word);
        }
        dproc.addDatasource(tokens2[0], tokens2[1]);
    }

    private void parseXport(String word) throws RrdException {
        // XPORT:vname[:legend]
        String[] tokens = new ColonSplitter(word).split();
        if (tokens.length == 2 || tokens.length == 3) {
            XPort xport = new XPort(tokens[1], tokens.length == 3 ? tokens[2] : null);
            xports.add(xport);
        }
        else {
            throw new RrdException("Invalid XPORT syntax: " + word);
        }
    }

    static class XPort {
        String name, legend;
        double[] values;

        XPort(String name, String legend) {
            this.name = name;
            this.legend = legend != null ? legend : "";
        }
    }
}
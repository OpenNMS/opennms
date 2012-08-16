/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennms.nrtg.web.internal;

import java.util.ArrayList;
import java.util.List;
import org.opennms.netmgt.model.PrefabGraph;

/**
 *
 * @author tak
 */
public class NrtRrdCommandFormatter {
    
    public final static List<String> RRD_KEYWORDS = new ArrayList<String>() {{
        add("--");
        add("DEF");
        add("CDEF");
        add("LINE");
        add("GPRINT");
    }};
    
    private final String rrdGraphString;
    
    public NrtRrdCommandFormatter(final PrefabGraph prefabGraph) {
        String rrdGraphString = prefabGraph.getCommand();
     
        rrdGraphString = rrdGraphString.replace("\n", " ");
        
        rrdGraphString = "--rigid " + rrdGraphString;
        rrdGraphString = "--height=400 " + rrdGraphString;
        rrdGraphString = "--width=900 " + rrdGraphString;
        rrdGraphString = "--vertical-label=\"Current Connections\" " + rrdGraphString;
        rrdGraphString = "--watermark=\"NRTG Alpha 1.0\" " + rrdGraphString;

        // Escaping colons in rrd-strings rrd in javascript in java...
        rrdGraphString = rrdGraphString.replace("\\:", "\\\\\\\\:");
        rrdGraphString = rrdGraphString.replace("\\n", "");

        // Braking before commands
//        for (final String keyword : RRD_KEYWORDS) {
//            rrdGraphString = rrdGraphString.replace(" " + keyword, " \n" + keyword);
//        }

        // Escaping quotes in javascript in java
        rrdGraphString = rrdGraphString.replace("\"", "\\\\\"");

        this.rrdGraphString = rrdGraphString;
    }

    public String getRrdGraphString() {
        return this.rrdGraphString;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennms.nrtg.web.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opennms.netmgt.model.PrefabGraph;

/**
 *
 * @author Markus@OpenNMS.org
 */
public class NrtRrdCommandFormatter {
    
    public final static List<String> RRD_KEYWORDS = new ArrayList<String>() {{
        add("--");
        add("DEF");
        add("CDEF");
        add("LINE");
        add("GPRINT");
    }};
    
    private String rrdGraphString;
    
    private String rrdMetricsMapping;
    
    public NrtRrdCommandFormatter(final PrefabGraph prefabGraph) {
        this.generateGraphString(prefabGraph);
        this.generateMetricsMapping(prefabGraph);
    }
    
    private void generateGraphString(final PrefabGraph prefabGraph) {
        String s = prefabGraph.getCommand();
        
        //Overwrite height and width by cinematic ration 1x2.40
        s = "--height=400 " + s;
        s = "--width=960 " + s;

        if(!s.contains("--slope-mode")) {
            s = "--slope-mode " + s;
        }
        if(!s.contains("--watermark")) {
            s = "--watermark=\"NRTG Alpha 1.0\" " + s;
        }

        // Escaping colons in rrd-strings rrd in javascript in java...
        s = s.replace("\\:", "\\\\\\\\:");
        s = s.replace("\\n", "\\\\\\\\n");

        // Escaping quotes in javascript in java
        s = s.replace("\"", "\\\\\"");

        this.rrdGraphString = s;
    }
    
    private void generateMetricsMapping(final PrefabGraph prefabGraph) {
        final StringBuilder s = new StringBuilder();
        
        final String command = prefabGraph.getCommand();
        
        final Pattern pattern = Pattern.compile("DEF:.*?=(\\{.*?\\}):(.*?):");
        final Matcher matcher = pattern.matcher(command);

        final Map<String, String> rrdFileMapping = new HashMap<String, String>();
        while (matcher.find()) {
            rrdFileMapping.put(matcher.group(2), matcher.group(1));
        }
        
        final String[] metrics = prefabGraph.getMetricIds();
        final String[] columns = prefabGraph.getColumns();
        assert metrics.length == columns.length;
        
        for (int i = 0; i < metrics.length; i++) {
            if (i != 0) {
                s.append(", \n");
            }
            
            final String metric = metrics[i];
            final String column = columns[i];
            
            s.append(String.format("'%s': '%s:%s'", metric, rrdFileMapping.get(column), column));
                        
        }
        
        this.rrdMetricsMapping = s.toString();
    }

    public String getRrdGraphString() {
        return this.rrdGraphString;
    }

    public String getRrdMetricsMapping() {
        return rrdMetricsMapping;
    }
}

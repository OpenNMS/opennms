/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.web.internal;

import org.opennms.netmgt.model.PrefabGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus@OpenNMS.org
 */
public class NrtRrdCommandFormatter {

    public final static List<String> RRD_KEYWORDS = Arrays.asList(
        "--",
        "DEF",
        "CDEF",
        "LINE",
        "GPRINT"
    );

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

        if (!s.contains("--slope-mode")) {
            s = "--slope-mode " + s;
        }
        if (!s.contains("--watermark")) {
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

            //TODO Tak the MetricsMapping for the graphing must be protocol independent.
            final String metric = metrics[i].substring("SNMP_".length());
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

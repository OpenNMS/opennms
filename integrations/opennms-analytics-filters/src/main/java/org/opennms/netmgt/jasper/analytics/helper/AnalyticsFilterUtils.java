/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.analytics.helper;

import java.awt.Point;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * Helper class for converting RRD-based data sources to and from
 * table representations.
 *
 * @author jwhite
 */
public class AnalyticsFilterUtils {

    public static Point getRowsWithValues(Table<Integer, String, Double> table, String... columnNames) {
        int firstRowWithValues = -1, lastRowWithValues = -1;
        for (int k : table.rowKeySet()) {
            for (String columnName : columnNames) {
                Double value = table.get(k, columnName);
                
                if (value != null && !Double.isNaN(value)) {
                    if (firstRowWithValues < 0) {
                        firstRowWithValues = k;
                    }
                    lastRowWithValues = k;
                }
            }
        }

        return new Point(firstRowWithValues, lastRowWithValues);
    }

    /**
     * Parses an RRD analytics commands out of the query string.
     *
     * @deprecated Use {@link AnalyticsCommand} directly and do not parse RRD based query strings anymore.
     */
    @Deprecated
    private static final java.util.List<AnalyticsCommand> parseCmdsFromQueryString(String input) {
        final java.util.List<AnalyticsCommand> analyticsCommands = Lists.newArrayList();
        final Matcher m = Pattern.compile(
                AnalyticsCommand.CMD_IN_RRD_QUERY_STRING + ":([\\w]+)=([\\w]+)(:[^\\s]+)?").matcher(input);

        // Build commands with all of the matches
        while (m.find()) {
            String arguments[] = new String[0];
            if (m.group(3) != null) {
                arguments = m.group(3).substring(1).split(":");
            }
            AnalyticsCommand cmd = new AnalyticsCommand(
                    m.group(1), m.group(2), arguments);
            analyticsCommands.add(cmd);
        }

        return analyticsCommands;
    }

    /**
     * Creates an AnalyticsCommand from an RRD Query String.
     * This method is kept due to backwarts compabilities and is going to be removed in future releases.
     * However if you need to convert RRD-base queries containing "ANALYTICS" queries you can convert them using this
     * method.
     * @param input the RRD query string, containing multiple ANALYTICS statements.
     * @return The first ANALYTICS statement from the RRD query string converted to an {@link AnalyticsCommand}
     *
     * @deprecated  Use {@link AnalyticsCommand} directly and do not parse RRD based query strings anymore.
     */
    @Deprecated
    public static java.util.List<AnalyticsCommand> createFromQueryString(String input) {
        java.util.List<AnalyticsCommand> analyticsCommands = parseCmdsFromQueryString(input);
        return analyticsCommands;
    }

    /**
     * * Determines all of the field names from the query string.
     * @deprecated  Use {@link AnalyticsCommand} directly and do not parse RRD based query strings anymore.
     */
    @Deprecated
    public static Object[] extratFieldNames(String queryString) {
        java.util.List<String> fieldNames = Lists.newArrayList();
        Matcher m = Pattern.compile("XPORT:[\\w]+:([\\w]+)").matcher(queryString);
        while (m.find()) {
            fieldNames.add(m.group(1));
        }
        if (fieldNames.size() > 0) {
            fieldNames.add("timestamp");
            fieldNames.add("step");
            fieldNames.add("start");
            fieldNames.add("end");
        }
        return fieldNames.toArray(new String[]{});
    }
}

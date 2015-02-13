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

package org.opennms.netmgt.jasper.analytics;

import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * Allows an RRD data source to be modified by analytics modules.
 * 
 * The list of modules to run, and their options are set with
 * additional commands in the query string. These commands are
 * removed from the query string before being passed to the actual
 * data source.
 * 
 * The commands are of the form:
 *   ANALYTICS:moduleName=columnNameOrPrefix(:otherOptions)
 * where:
 *   - moduleName is a unique name for the module
 *   - columnNameOrPrefix identifies the name of the column, or the prefix of the
 *   column name if there are multiple where the additional values will be stored
 *   - otherOptions are optional and specific to the module in question
 *
 * Once the data source has been loaded, the modules are invoked
 * in the same order as they appear in the query stirng.
 *
 * The module factories are loaded at run-time using the
 * ServiceLoader paradigm.
 *
 * @author jwhite
 */
public class RrdDataSourceFilter {
    private final String m_originalQueryString;
    private final String m_rrdQueryString;
    private final String[] m_fieldNames;
    private final Pattern m_xportCommandPattern = Pattern.compile(
            "XPORT:[\\w]+:([\\w]+)");
    private final Pattern m_queryStringPattern = Pattern.compile(
            AnalyticsCommand.CMD_IN_RRD_QUERY_STRING + 
            ":([\\w]+)=([\\w]+)(:[^\\s]+)?");
    private final List<AnalyticsCommand> m_analyticsCommands = Lists.newArrayList();
    private static final ServiceLoader<FilterFactory> m_analyticsModules =
            ServiceLoader.load(FilterFactory.class);

    public RrdDataSourceFilter(String queryString) {
        m_originalQueryString = queryString;
        m_rrdQueryString = parseCmdsFromQs();
        m_fieldNames = parseFieldNamesFromQs();
    }

    /**
     * Determines all of the field names from the query string.
     */
    private String[] parseFieldNamesFromQs() {
        List<String> fieldNames = Lists.newArrayList();
        Matcher m = m_xportCommandPattern.matcher(m_rrdQueryString);
        while (m.find()) {
            fieldNames.add(m.group(1));
        }
        if (fieldNames.size() > 0) {
            fieldNames.add("Timestamp");
        }
        return fieldNames.toArray(new String[]{});
    }

    /**
     * Parses the analytics commands out of the query string
     */
    private String parseCmdsFromQs() {
        Matcher m = m_queryStringPattern.matcher(m_originalQueryString);

        // Build commands with all of the matches
        while(m.find()) {
            String arguments[] = new String[0];
            if (m.group(3) != null) {
                arguments = m.group(3).substring(1).split(":");
            }
            AnalyticsCommand cmd = new AnalyticsCommand(
                    m.group(1), m.group(2), arguments);
            m_analyticsCommands.add(cmd);
        }

        // Remove all of our matches/commands from the query string
        return m.replaceAll("").trim();
    }

    /**
     * Returns the processed query string suitable for passing
     * to the RRD data source.
     */
    public String getRrdQueryString() {
        return m_rrdQueryString;
    }

    /**
     * Filters the given data source by successively applying
     * all of the analytics commands.
     */
    public JRRewindableDataSource filter(JRRewindableDataSource ds) throws JRException {
        // Don't bother converting the ds to and from a table if there are no
        // commands to apply
        if (m_analyticsCommands.isEmpty()) {
            return ds;
        }
 
        // Convert the data source to a table, making it easier for modules to manipulate
        RowSortedTable<Integer, String, Double> dsAsTable = DataSourceUtils.fromDs(ds, m_fieldNames);

        // Apply the filter modules
        try {
            filter(dsAsTable);
        } catch (Exception e) {
            throw new JRException("Failed to enrich the data source.", e);
        }

        // Convert the resulting table back to a data source
        return DataSourceUtils.toDs(dsAsTable);
    }

    public void filter(RowSortedTable<Integer, String, Double> dsAsTable) throws Exception {
        for (AnalyticsCommand command : m_analyticsCommands) {
            Filter filter = getFilter(command);
            if (filter == null) {
                throw new JRException("No analytics module found for " + command);
            }
            filter.filter(dsAsTable);
        }
    }

    /**
     * Retrieves an Enricher that supports the given analytics command
     *
     * @return
     *   null if no suitable Enricher was found
     * @throws JRException
     */
    private Filter getFilter(AnalyticsCommand command) throws Exception {
        Filter filter = null;
        for (FilterFactory module : m_analyticsModules) {
            filter = module.getFilter(command);
            if (filter != null) {
                return filter;
            }
        }
        return null;
    }

    /**
     * Used for testing.
     */
    protected List<AnalyticsCommand> getAnalyticsCommands() {
        return m_analyticsCommands;
    }

    /**
     * Used for testing.
     */
    protected String[] getFieldNames() {
        return m_fieldNames;
    }
}

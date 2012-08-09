/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.nrt;

import org.opennms.netmgt.model.PrefabGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

/*
 * @author Christian Pape
 */
public class NrtUIMetricMappings {
    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtUIMetricMappings.class);

    private HashMap<String, NrtUIMetricMappingHelper> m_nrtUIMappingHelpers = new HashMap<String, NrtUIMetricMappingHelper>();
    private HashMap<String, String> m_metricMappingsTable = new HashMap<String, String>();

    private PrefabGraph m_prefabGraph;
    private String m_modifiedString;

    public NrtUIMetricMappings(PrefabGraph prefabGraph) {
        this.m_prefabGraph = prefabGraph;
        this.m_modifiedString = m_prefabGraph.getCommand().replaceAll("\\{\\w+\\}", "GRAPH").replaceAll("\"", "'");

        String[] datasources = m_prefabGraph.getColumns();
        String[] metrics = m_prefabGraph.getMetricIds();

        for (int i = 0; i < datasources.length; i++) {
            m_metricMappingsTable.put(datasources[i], metrics[i]);
        }

        parseRrdString();
    }

    private String getMetricForDatasource(String datasource) {
        return m_metricMappingsTable.get(datasource);
    }

    private void parseRrdString() {
        String arrByWhiteSpace[] = m_prefabGraph.getCommand().split(" ");
        for (String defString : arrByWhiteSpace) {
            if (defString.startsWith("DEF:")) {
                String arrByColon[] = defString.split(":");
                if (arrByColon.length < 4) {
                    logger.error("Error parsing DEF-section of RRD string: '{}'", defString);
                } else {
                    String arrByEqualSign[] = arrByColon[1].split("=");

                    if (arrByEqualSign.length > 2) {
                        logger.error("Error parsing vname of RRD string: '{}'", arrByColon[1]);
                    } else {
                        String vname = arrByEqualSign[0];
                        String datasource = arrByColon[2];
                        String consolidationFunction = arrByColon[3];
                        String metric = getMetricForDatasource(datasource);
                        if (metric == null) {
                            logger.error("Error looking up metric for datasource {}", datasource);
                        } else {
                            if (!m_nrtUIMappingHelpers.containsKey(metric))
                                m_nrtUIMappingHelpers.put(metric, new NrtUIMetricMappingHelper(metric));

                            m_nrtUIMappingHelpers.get(metric).addTarget(consolidationFunction, vname);
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, NrtUIMetricMappingHelper> getNrtUIObjectHelpers() {
        return m_nrtUIMappingHelpers;
    }

    public String getJavaScriptObjects() {
        StringBuffer stringBuffer = new StringBuffer();
        for (String metric : m_nrtUIMappingHelpers.keySet()) {
            stringBuffer.append("metricMappings[\"" + metric + "\"] = new MetricMapping(\"" + metric + "\");\n");

            Set<String> consolidationFunctions = m_nrtUIMappingHelpers.get(metric).getConsolidationFunctions();
            for (String consolidationFunction : consolidationFunctions) {
                Set<String> targets = m_nrtUIMappingHelpers.get(metric).getTargetsForConsolidationFunction(consolidationFunction);
                for (String target : targets) {
                    stringBuffer.append("metricMappings[\"" + metric + "\"].addTarget(\"" + consolidationFunction + "\", \"" + target + "\");\n");
                }
            }
        }
        return stringBuffer.toString();
    }

    public String getModifiedString() {
        return m_modifiedString;
    }
}

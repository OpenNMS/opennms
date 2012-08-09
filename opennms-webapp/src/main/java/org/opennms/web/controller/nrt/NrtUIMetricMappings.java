package org.opennms.web.controller.nrt;

import org.opennms.netmgt.model.PrefabGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 08.08.12
 * Time: 18:37
 * To change this template use File | Settings | File Templates.
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

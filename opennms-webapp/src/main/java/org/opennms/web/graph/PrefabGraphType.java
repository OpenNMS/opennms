package org.opennms.web.graph;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PrefabGraphType {
    private PrefabGraph[] m_queries;

    private Map<String, PrefabGraph> m_reportMap;

    private File m_rrdDirectory;

    private String m_defaultReport;

    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    public PrefabGraphType() {
        
    }
    
    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setRrdDirectory(File rrdDirectory) {
        m_rrdDirectory = rrdDirectory;
    }

    public File getRrdDirectory() {
        return m_rrdDirectory;
    }

    public void setDefaultReport(String defaultReport) {
        m_defaultReport = defaultReport;
    }

    public String getDefaultReport() {
        return m_defaultReport;
    }
    
    public void setReportMap(Map<String, PrefabGraph> reportMap) {
        m_reportMap = reportMap;
    }
    
    public Map<String, PrefabGraph> getReportMap() {
        return m_reportMap;
    }

    public PrefabGraph getQuery(String queryName) {
        return m_reportMap.get(queryName);
    }

    /**
     * Return a list of all known prefabricated graph definitions.
     */
    @Deprecated
    public PrefabGraph[] getQueries() {
        if (m_queries == null) {
            initQueries();
        }

        return m_queries;
    }

    private void initQueries() {
        Collection<PrefabGraph> values = m_reportMap.values();
        Iterator<PrefabGraph> iter = values.iterator();

        PrefabGraph[] graphs = new PrefabGraph[values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = iter.next();
        }

        m_queries = graphs;
    }

    public void setCommandPrefix(String commandPrefix) {
        m_commandPrefix = commandPrefix;
    }
    
    public String getCommandPrefix() {
        return m_commandPrefix;
    }

    public void setOutputMimeType(String outputMimeType) {
        m_outputMimeType = outputMimeType;
    }
    
    public String getOutputMimeType() {
        return m_outputMimeType;
    }

}

package org.opennms.web.graph;

import java.io.File;

public class AdhocGraphType {
    private PrefabGraph[] m_queries;

    private File m_rrdDirectory;

    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_titleTemplate;

    private String m_dataSourceTemplate;

    private String m_graphLineTemplate;

    public AdhocGraphType() {
        
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

    public void setTitleTemplate(String template) {
        m_titleTemplate = template;
    }

    public void setDataSourceTemplate(String template) {
        m_dataSourceTemplate = template;
    }

    public void setGraphLineTemplate(String template) {
        m_graphLineTemplate = template;
    }

    public String getDataSourceTemplate() {
        return m_dataSourceTemplate;
    }

    public String getGraphLineTemplate() {
        return m_graphLineTemplate;
    }

    public String getTitleTemplate() {
        return m_titleTemplate;
    }

}

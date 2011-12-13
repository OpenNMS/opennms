package org.opennms.features.reporting.model.jasperreport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;

@XmlRootElement(name = "simple-jasper-report")
public class SimpleJasperReportDefinition implements BasicReportDefinition,
        JasperReportDefinition {

    private String m_id;
    private String m_repositoryId;
    private String m_engine;
    private String m_template;
    private String m_description;
    private String m_displayName;
    private String m_reportService;
    private boolean m_online;
    
    @XmlElement(name = "description")
    public String getDescription() {
        return m_description;
    }
    @XmlElement(name = "display-name")
    public String getDisplayName() {
        return m_displayName;
    }
    @XmlElement(name = "engine")
    public String getEngine() {
        return m_engine;
    }
    @XmlElement(name = "id")
    public String getId() {
        return m_id;
    }
    @XmlElement(name = "online")
    public boolean getOnline() {
        return m_online;
    }
    @XmlElement(name = "report-service")
    public String getReportService() {
        return m_reportService;
    }
    @Override
    public String getRepositoryId() {
        return m_repositoryId;
    }
    @XmlElement(name = "template")
    public String getTemplate() {
        return m_template;
    }
    public void setDescription(String description) {
        m_description = description;
    }
    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
    public void setEngine(String engine) {
        m_engine = engine;
    }
    public void setId(String id) {
        m_id = id;
    }
    public void setOnline(boolean online) {
        m_online = online;
    }
    public void setReportService(String reportService) {
        m_reportService = reportService;
    }
    public void setTemplate(String template) {
        m_template = template;
    }
    @Override
    public String toString() {
        return "SimpleJasperReportDefinition [m_id=" + m_id
                + ", m_repositoryId=" + m_repositoryId + ", m_engine="
                + m_engine + ", m_template=" + m_template
                + ", m_description=" + m_description + ", m_displayName="
                + m_displayName + ", m_reportService=" + m_reportService
                + ", m_online=" + m_online + "]";
    }
}

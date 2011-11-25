package org.opennms.features.reporting.model.basicreport;

import javax.xml.bind.annotation.*;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report")
public class LegacyLocalReportDefinition implements BasicReportDefinition {

    /**
     * the name of this report as defined in engine
     *  configuration
     */
    private String id;

    /**
     * the name of this report as displayed in the webui
     *  
     */
    private String displayName;

    /**
     * the name of the engine to use to process and
     *  render this report
     */
    private String reportService;

    /**
     * report description
     */
    private String description;

    /**
     * determines if the report may be executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     */
    private boolean online;

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDescription()
     */
    @Override
    @XmlAttribute(name = "description")
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDisplayName()
     */
    @Override
    @XmlAttribute(name = "display-name")
    public String getDisplayName() {
        return this.displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getId()
     */
    @Override
    @XmlAttribute(name = "id")
    public String getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getOnline()
     */
    @Override
    @XmlAttribute(name = "online")
    public boolean getOnline() {
        return this.online;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getReportService()
     */
    @Override
    @XmlAttribute(name = "report-service")
    public String getReportService() {
        return this.reportService;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", reportService='" + reportService + '\'' +
                ", description='" + description + '\'' +
                ", online=" + online +
                '}';
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setReportService(java.lang.String)
     */
    @Override
    public void setReportService(String reportService) {
        this.reportService = reportService;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setOnline(boolean)
     */
    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
}

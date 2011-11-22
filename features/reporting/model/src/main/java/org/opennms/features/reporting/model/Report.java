package org.opennms.features.reporting.model;

import javax.xml.bind.annotation.*;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="report")
public class Report {

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

    /**
     * Returns the value of field 'description'. The field
     * 'description' has the following description: report
     * description
     * 
     * @return the value of field 'Description'.
     */
    @XmlAttribute(name = "description")
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the value of field 'displayName'. The field
     * 'displayName' has the following description: the name of
     * this report as displayed in the webui
     *  
     * 
     * @return the value of field 'DisplayName'.
     */
    @XmlAttribute(name = "display-name")
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the
     * following description: the name of this report as defined in
     * engine
     *  configuration
     * 
     * @return the value of field 'Id'.
     */
    @XmlAttribute(name = "id")
    public String getId() {
        return this.id;
    }

    /**
     * Returns the value of field 'online'. The field 'online' has
     * the following description: determines if the report may be
     * executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     * 
     * @return the value of field 'Online'.
     */
    @XmlAttribute(name = "online")
    public boolean getOnline() {
        return this.online;
    }

    /**
     * Returns the value of field 'reportService'. The field
     * 'reportService' has the following description: the name of
     * the engine to use to process and
     *  render this report
     * 
     * @return the value of field 'ReportService'.
     */
    @XmlAttribute(name = "report-service")
    public String getReportService() {
        return this.reportService;
    }

    /**
     * Returns the value of field 'online'. The field 'online' has
     * the following description: determines if the report may be
     * executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     * 
     * @return the value of field 'Online'.
     */
    public boolean isOnline() {
        return this.online;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setReportService(String reportService) {
        this.reportService = reportService;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.databaseReports;

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
    @XmlAttribute(name = "id")
    private String id;

    /**
     * the name of this report as displayed in the webui
     *  
     */
    @XmlAttribute(name = "display-name")
    private String displayName;

    /**
     * the name of the engine to use to process and
     *  render this report
     */
    @XmlAttribute(name = "report-service")
    private String reportService;

    /**
     * report description
     */
    @XmlAttribute(name = "description")
    private String description;

    /**
     * determines if the report may be executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     */
    @XmlAttribute(name = "online")
    private boolean online;

    /**
     * Returns the value of field 'description'. The field
     * 'description' has the following description: report
     * description
     * 
     * @return the value of field 'Description'.
     */
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

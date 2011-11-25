package org.opennms.features.reporting.model.basicreport;

//import javax.xml.bind.annotation.XmlAttribute;

public interface BasicReportDefinition {
    
    /**
     * Returns the value of field 'description'. The field
     * 'description' has the following description: report
     * description
     * 
     * @return the value of field 'Description'.
     */
//    @XmlAttribute(name = "description")
    public abstract String getDescription();

    /**
     * Returns the value of field 'displayName'. The field
     * 'displayName' has the following description: the name of
     * this report as displayed in the webui
     *  
     * 
     * @return the value of field 'DisplayName'.
     */
//    @XmlAttribute(name = "display-name")
    public abstract String getDisplayName();

    /**
     * Returns the value of field 'id'. The field 'id' has the
     * following description: the name of this report as defined in
     * engine
     *  configuration
     * 
     * @return the value of field 'Id'.
     */
//    @XmlAttribute(name = "id")
    public abstract String getId();

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
//    @XmlAttribute(name = "online")
    public abstract boolean getOnline();

    /**
     * Returns the value of field 'reportService'. The field
     * 'reportService' has the following description: the name of
     * the engine to use to process and
     *  render this report
     * 
     * @return the value of field 'ReportService'.
     */
//    @XmlAttribute(name = "report-service")
    public abstract String getReportService();

    public abstract void setId(String id);

    public abstract void setDisplayName(String displayName);

    public abstract void setReportService(String reportService);

    public abstract void setDescription(String description);

    public abstract void setOnline(boolean online);

}
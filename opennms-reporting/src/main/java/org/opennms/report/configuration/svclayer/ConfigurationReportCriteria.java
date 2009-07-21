package org.opennms.report.configuration.svclayer;

import java.util.Date;

public class ConfigurationReportCriteria {

    String theDate;
    String reportFormat;
    String reportEmail;
    String user;
    Date reportRequestDate;

    public String getTheDate() {
        return theDate;
    }

    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getReportEmail() {
        return reportEmail;
    }

    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    public ConfigurationReportCriteria(String _date, String _format, String _reportemail, String _user, Date _reportRequestDate){
        theDate = _date;
        reportFormat = _format;
        reportEmail = _reportemail;
        user=_user;
        reportRequestDate=_reportRequestDate;
        
    }
    
    public ConfigurationReportCriteria() {
        
    }

}

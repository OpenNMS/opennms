package org.opennms.report.inventory.svclayer;

import java.util.Date;


public class InventoryReportCriteria {

    String theDate;
    String theField;
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

    public String getTheField() {
        return theField;
    }

    public void setTheField(String theField) {
        this.theField = theField;
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

    public InventoryReportCriteria(String _date, String _field, String _format, String _reportemail, String _user, Date _reportRequestDate){
        theDate = _date;
        theField = _field;
        reportFormat = _format;
        reportEmail = _reportemail;
        user=_user;
        reportRequestDate = _reportRequestDate;
    }

    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }
    

    
}

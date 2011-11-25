package org.opennms.features.reporting.model.jasperreport;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jasper-reports")
public class LocalJasperReports {
    
    private List<LocalJasperReport> m_reportList = new ArrayList<LocalJasperReport>();
    
    @XmlElement(name = "report")
    public List<LocalJasperReport> getReportList() {
        return m_reportList;
    }
    
    public void setReportList(List<LocalJasperReport> reportList) {
        this.m_reportList = reportList;
    }    
}

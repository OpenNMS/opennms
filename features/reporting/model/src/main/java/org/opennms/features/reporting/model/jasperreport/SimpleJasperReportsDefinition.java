package org.opennms.features.reporting.model.jasperreport;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "simple-jasper-reports")
public class SimpleJasperReportsDefinition {
    
    private List<SimpleJasperReportDefinition> m_reportList = new ArrayList<SimpleJasperReportDefinition>();
    
    @XmlElement(name = "simple-jasper-report")
    public List<SimpleJasperReportDefinition> getReportList() {
        return m_reportList;
    }
    
    public void setReportList(List<SimpleJasperReportDefinition> reportList) {
        this.m_reportList = reportList;
    }    
}

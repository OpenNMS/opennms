package org.opennms.features.reporting.model.basicreport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Report Configuration for local reports
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "local-reports")
public class LegacyLocalReportsDefinition {

    private List<BasicReportDefinition> m_reportList = new ArrayList<BasicReportDefinition>();

    @XmlElement(name = "report")
    public List<BasicReportDefinition> getReportList() {
        return m_reportList;
    }

    public void setReportList(List<BasicReportDefinition> reportList) {
        this.m_reportList = reportList;
    }
}

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

    private List<LegacyLocalReportDefinition> m_reportList = new ArrayList<LegacyLocalReportDefinition>();

    @XmlElement(name = "report")
    public List<LegacyLocalReportDefinition> getReportList() {
        return m_reportList;
    }

    public void setReportList(List<LegacyLocalReportDefinition> reportList) {
        this.m_reportList = reportList;
    }
}

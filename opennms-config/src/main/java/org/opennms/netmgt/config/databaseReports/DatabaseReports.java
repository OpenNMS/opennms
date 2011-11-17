package org.opennms.netmgt.config.databaseReports;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Report Configuration for database reports
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "database-reports")
public class DatabaseReports {

    private List<Report> reportList = new ArrayList<Report>();

    @XmlElement(name="report")
    public List<Report> getReportList() {
        return reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.reportList = reportList;
    }
}

package org.opennms.features.reporting.reportrepository;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.Report;
/**
 * Created by IntelliJ IDEA.
 * User: indigo
 * Date: 11/17/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ReportRepository {

    public List<Report> getOnlineReports();

    public List<Report> getAllReports();

    public List<Report> getReportsByReportProvider(ReportProvider provider);

    public List<Report> getOnlineReportsByReportProvider(ReportProvider provider);

    public List<ReportProvider> getAllReportProvider();

    public List<ReportProvider> getReportProviderList ();

    public void setReportProviderList(Collection<ReportProvider> reportProviderList);
}

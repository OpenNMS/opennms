package org.opennms.features.reporting.reportrepository;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: indigo
 * Date: 11/17/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ReportRepository {

    public Collection<String> getOnlineReports();

    public Collection<String> getAllReport();

    public Collection<String> getReportsByReportProvider(ReportProvider provider);

    public Collection<String> getOnlineReportsByReportProvider(ReportProvider provider);

    public Collection<ReportProvider> getAllReportProvider();

    public Collection<ReportProvider> getReportProviderList ();

    public void setReportProviderList(Collection<ReportProvider> reportProviderList);
}

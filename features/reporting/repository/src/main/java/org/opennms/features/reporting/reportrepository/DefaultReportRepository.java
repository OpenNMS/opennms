package org.opennms.features.reporting.reportrepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: indigo
 * Date: 11/17/11
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultReportRepository implements ReportRepository{
    private Collection<ReportProvider> m_reportProviderList;


    public DefaultReportRepository() {
        this.m_reportProviderList = new ArrayList<ReportProvider>();
    }

    public ArrayList<String> getOnlineReports() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ArrayList<String> getAllReport() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<String> getReportsByReportProvider(ReportProvider provider) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<String> getOnlineReportsByReportProvider(ReportProvider provider) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ArrayList<ReportProvider> getAllReportProvider() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setReportProviderList(Collection<ReportProvider> reportProviderList) {
        this.m_reportProviderList = reportProviderList;
    }

    public Collection<ReportProvider> getReportProviderList() {
        return this.m_reportProviderList;
    }
}

package org.opennms.features.reporting.reportrepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.databaseReports.Report;

/**
 * Created by IntelliJ IDEA. User: indigo Date: 11/17/11 Time: 1:26 PM To
 * change this template use File | Settings | File Templates.
 */
public class DefaultReportRepository implements ReportRepository {
    private ArrayList<ReportProvider> m_reportProviderList;

    public DefaultReportRepository() {
        this.m_reportProviderList = new ArrayList<ReportProvider>();
    }

    public ArrayList<Report> getOnlineReports() {
        ArrayList<Report> reports = new ArrayList<Report>();
        {
            Report report0 = new Report();
            report0.setId("0");
            report0.setDisplayName("FooDisplayName");
            report0.setDescription("FooDescription");
            report0.setOnline(true);
            report0.setReportService("FooReportService");
            reports.add(report0);
        }

        {
            Report report1 = new Report();
            report1.setId("1");
            report1.setDisplayName("BarDisplayName");
            report1.setDescription("BarDescription");
            report1.setOnline(true);
            report1.setReportService("BarReportService");
            reports.add(report1);
        }
        
        {
            Report report2 = new Report();
            report2.setId("2");
            report2.setDisplayName("SauerkrautDisplayName");
            report2.setDescription("SauerkrautDescription");
            report2.setOnline(false);
            report2.setReportService("SauerkrautReportService");
            reports.add(report2);
        }
        
        return reports;
    }

    public List<Report> getAllReports() {
        ArrayList<Report> reports = new ArrayList<Report>();
        {
            Report report0 = new Report();
            report0.setId("0");
            report0.setDisplayName("FooDisplayName");
            report0.setDescription("FooDescription");
            report0.setOnline(true);
            report0.setReportService("FooReportService");
            reports.add(report0);
        }

        {
            Report report1 = new Report();
            report1.setId("1");
            report1.setDisplayName("BarDisplayName");
            report1.setDescription("BarDescription");
            report1.setOnline(true);
            report1.setReportService("BarReportService");
            reports.add(report1);
        }
        return reports;
    }

    public ArrayList<Report> getReportsByReportProvider(
            ReportProvider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<Report> getOnlineReportsByReportProvider(
            ReportProvider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<ReportProvider> getAllReportProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setReportProviderList(
            Collection<ReportProvider> reportProviderList) {
        this.m_reportProviderList = (ArrayList<ReportProvider>) reportProviderList;
    }

    public ArrayList<ReportProvider> getReportProviderList() {
        return this.m_reportProviderList;
    }
}

package org.opennms.features.reporting.repository;

import java.util.List;

import org.opennms.feature.reporting.dao.DefaultLocalReportsDao;
import org.opennms.features.reporting.model.Report;

public class DefaultReportRepository implements ReportRepository {
    
    private DefaultLocalReportsDao m_databaseReportsDao = new DefaultLocalReportsDao();
    
    
    @Override
    public List<Report> getReports() {
        return m_databaseReportsDao.getOnlineReports();
    }

    @Override
    public List<Report> getOnlineReports() {
        return m_databaseReportsDao.getOnlineReports();
    }

    @Override
    public String getReportService(String id) {
        return m_databaseReportsDao.getReportService(id);
    }

    @Override
    public String getDisplayName(String id) {
        return m_databaseReportsDao.getDisplayName(id);
    }
    
}

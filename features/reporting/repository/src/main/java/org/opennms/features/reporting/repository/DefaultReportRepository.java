package org.opennms.features.reporting.repository;

import java.util.List;

import org.opennms.feature.reporting.dao.DefaultLocalReportsDao;
import org.opennms.feature.reporting.dao.jasper.DefaultLocalJasperReportsDao;
import org.opennms.features.reporting.model.Report;

public class DefaultReportRepository implements ReportRepository {
    
    private DefaultLocalReportsDao m_defaultLocalReportsDao = new DefaultLocalReportsDao();
    
    private DefaultLocalJasperReportsDao m_defaultLocalJasperReportsDao = new DefaultLocalJasperReportsDao();
    
    
    @Override
    public List<Report> getReports() {
        return m_defaultLocalReportsDao.getOnlineReports();
    }

    @Override
    public List<Report> getOnlineReports() {
        return m_defaultLocalReportsDao.getOnlineReports();
    }

    @Override
    public String getReportService(String id) {
        return m_defaultLocalReportsDao.getReportService(id);
    }

    @Override
    public String getDisplayName(String id) {
        return m_defaultLocalReportsDao.getDisplayName(id);
    }

    @Override
    public String getTemplateLocation(String reportId) {
        return m_defaultLocalJasperReportsDao.getTemplateLocation(reportId);
    }

    @Override
    public String getEngine(String reportId) {
        return m_defaultLocalJasperReportsDao.getEngine(reportId);
    }
    
}

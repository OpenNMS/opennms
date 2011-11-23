package org.opennms.features.reporting.repository.local;

import java.io.InputStream;
import java.util.List;

import org.opennms.features.reporting.dao.LegacyLocalReportsDao;
import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.model.Report;
import org.opennms.features.reporting.repository.ReportRepository;

public class LegacyLocalReportRepository implements ReportRepository {
    
    private LocalReportsDao m_localReportsDao = new LegacyLocalReportsDao();
    
    private LocalJasperReportsDao m_localJasperReportsDao = new LegacyLocalJasperReportsDao();
    
    @Override
    public List<Report> getReports() {
        return m_localReportsDao.getReports();
    }

    @Override
    public List<Report> getOnlineReports() {
        return m_localReportsDao.getOnlineReports();
    }

    @Override
    public String getReportService(String id) {
        return m_localReportsDao.getReportService(id);
    }

    @Override
    public String getDisplayName(String id) {
        return m_localReportsDao.getDisplayName(id);
    }

    @Override
    public String getEngine(String id) {
        return m_localJasperReportsDao.getEngine(id);
    }

    @Override
    public InputStream getTemplateStream(String id) {
        return m_localJasperReportsDao.getTemplateStream(id);
    }

}

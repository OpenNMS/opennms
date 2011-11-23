package org.opennms.features.reporting.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.feature.reporting.dao.BasicReportDataProvider;
import org.opennms.feature.reporting.dao.DefaultLocalReportsDao;
import org.opennms.feature.reporting.dao.jasper.DefaultLocalJasperReportsDao;
import org.opennms.feature.reporting.dao.jasper.JasperReportDataProvider;
import org.opennms.feature.reporting.dao.remote.DefaultRemoteRepository;
import org.opennms.feature.reporting.dao.remote.RemoteReportDataProvider;
import org.opennms.features.reporting.model.Report;

public class DefaultReportRepository implements ReportRepository {

    private BasicReportDataProvider m_defaultLocalReportsDao = new DefaultLocalReportsDao();

    private JasperReportDataProvider m_defaultLocalJasperReportsDao = new DefaultLocalJasperReportsDao();

    private RemoteReportDataProvider m_defaultRemote = new DefaultRemoteRepository();

    @Override
    public List<Report> getReports() {
        List<Report> results = new ArrayList<Report>();
        results.addAll(m_defaultLocalReportsDao.getReports());
        results.addAll(m_defaultRemote.getReports());
        return results;
    }

    @Override
    public List<Report> getOnlineReports() {
        List<Report> results = new ArrayList<Report>();
        results.addAll(m_defaultLocalReportsDao.getOnlineReports());
        results.addAll(m_defaultRemote.getOnlineReports());
        return results;
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        if (id.startsWith("connect_")) {
            result = m_defaultRemote.getReportService(id);
        } else {
            result = m_defaultLocalReportsDao.getReportService(id);
        }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        if (id.startsWith("connect_")) {
            result = m_defaultRemote.getDisplayName(id);
        } else {
            result = m_defaultLocalReportsDao.getDisplayName(id);
        }
        return result;
    }

    @Override
    public String getEngine(String id) {
        String result = "";
        if (id.startsWith("connect_")) {
            result = m_defaultRemote.getEngine(id);
        } else {
            result = m_defaultLocalJasperReportsDao.getEngine(id);
        }
        return result;
    }

    @Override
    public InputStream getTemplateStream(String id) {
        InputStream templateStream = null;
        if (id.startsWith("connect_")) {
            templateStream = m_defaultRemote.getTemplateStream(id);
        } else {
            templateStream = m_defaultLocalJasperReportsDao.getTemplateStream(id);
        }
        return templateStream;
    }
}

package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.model.Report;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;

public class GlobalReportRepository implements ReportRepository {
    
    private final String REMOTE_ID_PREFIX = "REMOTE_";
    
    private ReportRepository m_localReportRepo = new LegacyLocalReportRepository();
    
    private ReportRepository m_dummyRemoteRepo = new DefaultRemoteRepository();
    
    //TODO Tak: Stamp prefixe into reportIDs for each Repository
    
    @Override
    public List<Report> getReports() {
        List<Report> results = new ArrayList<Report>();
        results.addAll(m_dummyRemoteRepo.getReports());
        results.addAll(m_localReportRepo.getReports());
        return results;
    }

    @Override
    public List<Report> getOnlineReports() {
        List<Report> results = new ArrayList<Report>();
        results.addAll(m_dummyRemoteRepo.getOnlineReports());
        results.addAll(m_localReportRepo.getOnlineReports());
        return results;
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        if (id.startsWith(REMOTE_ID_PREFIX)) {
            result = m_dummyRemoteRepo.getReportService(id);
        } else {
            result = m_localReportRepo.getReportService(id);
        }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        if (id.startsWith(REMOTE_ID_PREFIX)) {
            result = m_dummyRemoteRepo.getDisplayName(id);
        } else {
            result = m_localReportRepo.getDisplayName(id);
        }
        return result;
    }

    @Override
    public String getEngine(String id) {
        String result = "";
        if (id.startsWith(REMOTE_ID_PREFIX)) {
            result = m_dummyRemoteRepo.getEngine(id);
        } else {
            result = m_localReportRepo.getEngine(id);
        }
        return result;
    }

    @Override
    public InputStream getTemplateStream(String id) {
        InputStream templateStream = null;
        if (id.startsWith(REMOTE_ID_PREFIX)) {
            templateStream = m_dummyRemoteRepo.getTemplateStream(id);
        } else {
            templateStream = m_localReportRepo.getTemplateStream(id);
        }
        return templateStream;
    }
}

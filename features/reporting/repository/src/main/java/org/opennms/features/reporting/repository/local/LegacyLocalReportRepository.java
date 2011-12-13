package org.opennms.features.reporting.repository.local;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.dao.LegacyLocalReportsDao;
import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;

public class LegacyLocalReportRepository implements ReportRepository {
    
    private LocalReportsDao m_localReportsDao = new LegacyLocalReportsDao();
    
    private LocalJasperReportsDao m_localJasperReportsDao = new LegacyLocalJasperReportsDao();
    
    private final String REPOSITORY_ID = "local";
    
    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        for (BasicReportDefinition report : m_localReportsDao.getReports()) {
            report.setId(REPOSITORY_ID + "_" + report.getId());
            resultList.add(report);
        }
        return resultList;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
        for (BasicReportDefinition report : m_localReportsDao.getOnlineReports()) {
            report.setId(REPOSITORY_ID + "_" + report.getId());
            resultList.add(report);
        }
        return resultList;
    }

    @Override
    public String getReportService(String id) {
        id = id.substring(id.indexOf("_") +1);
        return m_localReportsDao.getReportService(id);
    }

    @Override
    public String getDisplayName(String id) { 
        id = id.substring(id.indexOf("_") +1);
        return m_localReportsDao.getDisplayName(id);
    }

    @Override
    public String getEngine(String id) { 
        id = id.substring(id.indexOf("_") +1);
        return m_localJasperReportsDao.getEngine(id);
    }

    @Override
    public InputStream getTemplateStream(String id) { 
        id = id.substring(id.indexOf("_") +1);
        return m_localJasperReportsDao.getTemplateStream(id);
    }

    @Override
    public String getRepositoryId() {
        return REPOSITORY_ID;
    }
}

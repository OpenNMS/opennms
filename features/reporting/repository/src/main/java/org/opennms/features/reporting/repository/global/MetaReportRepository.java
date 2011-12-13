package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;

public interface MetaReportRepository {
    
    public List<BasicReportDefinition> getAllReports();
    public List<BasicReportDefinition> getAllOnlineReports();
    public List<BasicReportDefinition> getReports(String repoId);
    public List<BasicReportDefinition> getOnlineReports(String repoId);
    public String getReportService(String reportId);
    public String getDisplayName(String reportId);
    public String getEngine(String reportId);
    public InputStream getTemplateStream(String reportId);
    public List<ReportRepository> getRepositoryList();
    public void addReportRepositoy(ReportRepository repository);
    public ReportRepository getRepositoryById(String repoId);
}

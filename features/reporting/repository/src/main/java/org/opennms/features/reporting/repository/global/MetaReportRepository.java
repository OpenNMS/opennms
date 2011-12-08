package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;

public interface MetaReportRepository {
    
    public List<BasicReportDefinition> getReports(String repoId);
    public List<BasicReportDefinition> getOnlineReports(String repoId);
    public String getReportService(String reportId, String repoId);
    public String getDisplayName(String reportId, String repoId);
    public String getEngine(String reportId, String repoId);
    public InputStream getTemplateStream(String reportId, String repoId);
    public List<ReportRepository> getRepositoryList();
    public void addReportRepositoy(ReportRepository repository);
}

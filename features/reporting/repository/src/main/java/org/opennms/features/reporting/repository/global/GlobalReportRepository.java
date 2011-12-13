package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;

public class GlobalReportRepository implements MetaReportRepository {

    private List<ReportRepository> repositoryList = new ArrayList<ReportRepository>();

    public GlobalReportRepository() {
        repositoryList.add(new LegacyLocalReportRepository());
        repositoryList.add(new DefaultRemoteRepository());
    }

    @Override
    public List<BasicReportDefinition> getAllReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : repositoryList) {
            results.addAll(repository.getReports());
        }
        return results;
    }
    
    @Override
    public List<BasicReportDefinition> getReports(String repoId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null) {
            results.addAll(repo.getReports());
        }
        return results;
    }
 
    @Override
    public List<BasicReportDefinition> getAllOnlineReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : repositoryList) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }
    
    @Override
    public List<BasicReportDefinition> getOnlineReports(String repoId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null ) {
            results.addAll(repo.getOnlineReports());
        }
        return results;
    }
    
    @Override 
    public String getReportService(String reportId) {
        String result = "";
        ReportRepository repo = this.getRepositoryForReport(reportId);
        if (repo != null) {
            result = repo.getReportService(reportId);
        }
        return result;
    }
    
    @Override
    public String getDisplayName(String reportId) {
        String result = "";
        ReportRepository repo = this.getRepositoryForReport(reportId);
        if (repo != null) {
            result = repo.getDisplayName(reportId);
        }
        return result;
    }
    
    @Override
    public String getEngine(String reportId) {
        String result = "";
        ReportRepository repo = this.getRepositoryForReport(reportId);
        if (repo != null) {
            result = repo.getEngine(reportId);
        }
        return result;
    }
    
    @Override
    public InputStream getTemplateStream(String reportId) {
        InputStream templateStream = null;
        ReportRepository repo = this.getRepositoryForReport(reportId);
        if (repo != null) {
            templateStream = repo.getTemplateStream(reportId);
        }
        return templateStream;
    }

    @Override
    public List<ReportRepository> getRepositoryList() {
        return repositoryList;
    }

    @Override
    public void addReportRepositoy(ReportRepository repository) {
        repositoryList.add(repository);
    }
    
    @Override
    public ReportRepository getRepositoryById(String repoId) {
        ReportRepository resultRepo = null;
        for (ReportRepository repo : repositoryList) {
            if (repoId.equals(repo.getRepositoryId())) {
                return resultRepo;
            }
        }
        return resultRepo;
    }
    
    private ReportRepository getRepositoryForReport(String reportId) {
        String repoId = reportId.substring(0, reportId.indexOf("_"));
        return this.getRepositoryById(repoId);
    }
}

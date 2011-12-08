package org.opennms.features.reporting.repository.global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;

public class GlobalReportRepository implements ReportRepository,
        MetaReportRepository {

    private List<ReportRepository> repositoryList = new ArrayList<ReportRepository>();

    public GlobalReportRepository() {
        repositoryList.add(new LegacyLocalReportRepository());
        repositoryList.add(new DefaultRemoteRepository());
    }

    // TODO Tak: Stamp prefixe into reportIDs for each Repository
    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repo : repositoryList) {
            results.addAll(repo.getReports());
        }
        return results;
    }

    public List<BasicReportDefinition> getReports(String repoId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null) {
            results.addAll(repo.getReports());
        }
        return results;
    }
    
    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repo : repositoryList) {
            results.addAll(repo.getOnlineReports());
        }
        return results;
    }

    public List<BasicReportDefinition> getOnlineReports(String repoId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null ) {
            results.addAll(repo.getOnlineReports());
        }
        return results;
    }
    
    public String getReportService(String reportId, String repoId) {
        String result = "";
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null) {
            result = repo.getReportService(reportId);
        }
        return result;
    }

    public String getDisplayName(String reportId, String repoId) {
        String result = "";
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null) {
            result = repo.getDisplayName(reportId);
        }
        return result;
    }

    public String getEngine(String reportId, String repoId) {
        String result = "";
        ReportRepository repo = this.getRepositoryById(repoId);
        if (repo != null) {
            result = repo.getEngine(reportId);
        }
        return result;
    }

    public InputStream getTemplateStream(String reportId, String repoId) {
        InputStream templateStream = null;
        ReportRepository repo = this.getRepositoryById(repoId);
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

    private ReportRepository getRepositoryById(String repoId) {
        ReportRepository resultRepo = null;
        for (ReportRepository repo : repositoryList) {
            if (repoId.equals(repo.getRepositoryId())) {
                return resultRepo;
            }
        }
        return resultRepo;
    }

    @Override
    public String getRepositoryId() {
        return "GlobalRepository";
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        // TODO Tak: ReportRepository vs MetaReportRepository
        // if (id.startsWith(REMOTE_ID_PREFIX)) {
        // result = m_dummyRemoteRepo.getReportService(id);
        // } else {
        // result = m_localReportRepo.getReportService(id);
        // }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        // TODO Tak: ReportRepository vs MetaReportRepository
        // if (id.startsWith(REMOTE_ID_PREFIX)) {
        // result = m_dummyRemoteRepo.getDisplayName(id);
        // } else {
        // result = m_localReportRepo.getDisplayName(id);
        // }
        return result;
    }

    @Override
    public String getEngine(String id) {
        String result = "";
        // TODO Tak: ReportRepository vs MetaReportRepository
        // if (id.startsWith(REMOTE_ID_PREFIX)) {
        // result = m_dummyRemoteRepo.getEngine(id);
        // } else {
        // result = m_localReportRepo.getEngine(id);
        // }
        return result;
    }

    @Override
    public InputStream getTemplateStream(String id) {
        InputStream templateStream = null;
        // TODO Tak: ReportRepository vs MetaReportRepository
        // if (id.startsWith(REMOTE_ID_PREFIX)) {
        // templateStream = m_dummyRemoteRepo.getTemplateStream(id);
        // } else {
        // templateStream = m_localReportRepo.getTemplateStream(id);
        // }
        return templateStream;
    }
}

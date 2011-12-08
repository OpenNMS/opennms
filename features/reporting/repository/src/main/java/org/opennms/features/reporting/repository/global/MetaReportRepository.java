package org.opennms.features.reporting.repository.global;

import java.util.List;

import org.opennms.features.reporting.repository.ReportRepository;

public interface MetaReportRepository {
    
    public List<ReportRepository> getRepositoryList();
    public void addReportRepositoy(ReportRepository repository);
    
}

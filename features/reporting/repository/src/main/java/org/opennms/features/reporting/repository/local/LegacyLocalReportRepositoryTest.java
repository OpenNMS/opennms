package org.opennms.features.reporting.repository.local;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;

public class LegacyLocalReportRepositoryTest {
    ReportRepository m_repo = new LegacyLocalReportRepository();
    
    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "src/test/resources");
    }
    
    @Test
    public void reportIdsWitchRepositoryIdsTest() {
        assertEquals("local", m_repo.getRepositoryId());
        BasicReportDefinition report = m_repo.getReports().get(0);
//        System.out.println(report.getId());
//        System.out.println(m_repo.getDisplayName(report.getId()));
    }
    
    @Test
    public void getReportsTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        for (BasicReportDefinition report : reports) {
            System.out.println(report.getId() + "\t" + report.getDisplayName() + "\t" + report.getReportService());
        }
    }
    
    
}
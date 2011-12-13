package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;

public class DefaultRemoteRepositoryTest {
    
    private DefaultRemoteRepository m_repo;

    @Before
    public void setup(){
        System.setProperty("opennms.home", "/opt/opennms");
        m_repo = new DefaultRemoteRepository();
    }
    
    @Test
    public void getOnlineReports() {
        List<BasicReportDefinition> reports = m_repo.getOnlineReports();
        System.out.println("getOnlineReports");
        for(BasicReportDefinition report : reports) {
            System.out.println(report);
        }
    }

    @Test
    public void getReports() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        System.out.println("getReports");
        for(BasicReportDefinition report : reports) {
            System.out.println(report);
        }
    }
    
    @Test public void reportIdsStartWithRepositoryIdTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        //System.out.println("reportIdsStartWithRepositoryIdTest");
        for(BasicReportDefinition report : reports) {
            assertTrue(report.getId().startsWith(m_repo.getRepositoryId()));
            //System.out.println(report.getId());
            //System.out.println(report.getRepositoryId() + "\n");
        }
    }
    
    @Test
    public void reportIdsWithRepoIdgetMappedToRemoteReportTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        for(BasicReportDefinition report : reports) {
            assertTrue(m_repo.getDisplayName(report.getId()).length() > 0);
            //System.out.println(report.getId() + "\t" + m_repo.getDisplayName(report.getId()));
        }
    }
}

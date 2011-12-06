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
//TODO Tak: tests deprecated, write new tests
//    @Test
//    public void getReportsTest() {
//        assertEquals("jdbc", m_repo.getEngine("4"));
//    }
//
//    @Test
//    public void getDisplayTest() {
//        assertEquals("Eat my shorts!", m_repo.getDisplayName("4"));
//    } 
}

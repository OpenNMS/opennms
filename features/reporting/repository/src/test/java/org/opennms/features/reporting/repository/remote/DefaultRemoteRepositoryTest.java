package org.opennms.features.reporting.repository.remote;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.features.reporting.model.Report;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;

public class DefaultRemoteRepositoryTest {
    
    private DefaultRemoteRepository m_repo;

    @BeforeClass
    public void setup(){
        System.setProperty("opennms.home", "/opt/opennms");
        m_repo = new DefaultRemoteRepository();
    }
    
    @Test
    public void getOnlineReports() {
        ArrayList<Report> reports = (ArrayList<Report>) m_repo.getOnlineReports();
        System.out.println("getOnlineReports");
        for(Report report : reports) {
            System.out.println(report);
        }
    }

    @Test
    public void getReports() {
        ArrayList<Report> reports = (ArrayList<Report>) m_repo.getReports();
        System.out.println("getReports");
        for(Report report : reports) {
            System.out.println(report);
        }
    }
    
    @Test
    public void getReportsTest() {
        assertEquals("jdbc", m_repo.getEngine("4"));
    }

    @Test
    public void getDisplayTest() {
        assertEquals("Eat my shorts!", m_repo.getDisplayName("4"));
    } 
}

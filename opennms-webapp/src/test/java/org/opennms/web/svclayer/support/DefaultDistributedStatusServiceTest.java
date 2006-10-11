package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.Util;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SimpleWebTable.Cell;

import junit.framework.TestCase;

public class DefaultDistributedStatusServiceTest extends TestCase {
    private DefaultDistributedStatusService m_service = new DefaultDistributedStatusService();
    
    private PollerConfig m_pollerConfig = createMock(PollerConfig.class);
    private MonitoredServiceDao m_monitoredServiceDao = createMock(MonitoredServiceDao.class);
    private LocationMonitorDao m_locationMonitorDao = createMock(LocationMonitorDao.class); 
    private ApplicationDao m_applicationDao = createMock(ApplicationDao.class);
    private CategoryDao m_categoryDao = createMock(CategoryDao.class);

    private OnmsMonitoringLocationDefinition m_locationDefinition1;
    private OnmsMonitoringLocationDefinition m_locationDefinition2;
    private OnmsMonitoringLocationDefinition m_locationDefinition3;
    private OnmsLocationMonitor m_locationMonitor1;
    private OnmsLocationMonitor m_locationMonitor2;
    private OnmsLocationMonitor m_locationMonitor3;
    private OnmsApplication m_application1;
    private OnmsApplication m_application2;
    
    private Package m_pkg;
    private ServiceSelector m_selector;
    private Collection<OnmsMonitoredService> m_services;
    private OnmsNode m_node;
        
    protected void setUp() {
        m_service.setPollerConfig(m_pollerConfig);
        m_service.setMonitoredServiceDao(m_monitoredServiceDao);
        m_service.setLocationMonitorDao(m_locationMonitorDao);
        m_service.setApplicationDao(m_applicationDao);
        m_service.setCategoryDao(m_categoryDao);
        
        m_locationDefinition1 = new OnmsMonitoringLocationDefinition("Raleigh", "raleigh", "OpenNMS NC");
        m_locationDefinition2 = new OnmsMonitoringLocationDefinition("Durham", "durham", "OpenNMS NC");
        m_locationDefinition3 = new OnmsMonitoringLocationDefinition("Columbus", "columbus", "OpenNMS OH");

        m_application1 = new OnmsApplication();
        m_application1.setLabel("Application 1");

        m_locationMonitor1 = new OnmsLocationMonitor();
        m_locationMonitor2 = new OnmsLocationMonitor();
        m_locationMonitor3 = null; // Test the case where there is no monitor for this location

        m_pkg = new Package();

        List<String> serviceNames = new ArrayList<String>();
        serviceNames.add("ICMP");
        serviceNames.add("DNS");
        serviceNames.add("HTTP");
        serviceNames.add("HTTPS");
        m_selector = new ServiceSelector("IPADDR IPLIKE *.*.*.*", serviceNames);
        
        m_services = new HashSet<OnmsMonitoredService>();
        
        m_node = new OnmsNode();
        String ip = "1.1.1.1";
        m_node.setLabel("Node 1");
        
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("ICMP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("DNS")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("HTTP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("HTTPS")));

        Set<OnmsMonitoredService> applicationServices1 = new HashSet<OnmsMonitoredService>();
        applicationServices1.add(findMonitoredService(m_services, ip, "HTTP"));
        applicationServices1.add(findMonitoredService(m_services, ip, "HTTPS"));
        m_application1.setMemberServices(applicationServices1);
        
        m_application2 = new OnmsApplication();
        m_application2.setLabel("Application 2");
        m_application2.setMemberServices(applicationServices1);
        
    }
    
    public void testFindLocationSpecificStatusNullLocation() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("locationName cannot be null"));
        try {
            m_service.findLocationSpecificStatus(null, m_application1.getLabel());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusNullApplication() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("applicationLabel cannot be null"));
        try {
            m_service.findLocationSpecificStatus(m_locationDefinition1.getName(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatus() {
        expectEverything();
        
        replayEverything();
        
        List<OnmsLocationSpecificStatus> status =
            m_service.findLocationSpecificStatus(m_locationDefinition1.getName(),
                                                 m_application1.getLabel());

        verifyEverything();
        
        assertEquals("status list size", 2, status.size());
    }
    
    public void testCreateStatus() {
        expectEverything();
        
        // Once for each service that we display 
        expect(m_categoryDao.findByNode(m_node)).andReturn(null);
        expect(m_categoryDao.findByNode(m_node)).andReturn(null);
        
        replayEverything();
        m_service.createStatusTable(m_locationDefinition1.getName(),
                                    m_application1.getLabel());
        
        verifyEverything();
    }
    
    public void testCreateFacilityStatusTable() {
        List<OnmsMonitoringLocationDefinition> locationDefinitions =
            new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        
        List<OnmsApplication> applications =
            new LinkedList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        
        Collection<OnmsLocationSpecificStatus> statuses =
            new LinkedList<OnmsLocationSpecificStatus>();
        
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        expect(m_applicationDao.findAll()).andReturn(applications);
        expect(m_locationMonitorDao.getAllMostRecentStatusChanges()).andReturn(statuses);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(0))).andReturn(m_locationMonitor1);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(1))).andReturn(m_locationMonitor2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(2))).andReturn(m_locationMonitor3);
        
        replayEverything();
        SimpleWebTable table = m_service.createFacilityStatusTable();
        verifyEverything();
        
        System.out.print(table.toString());
    }
    
    public void expectEverything() {
        expect(m_applicationDao.findByLabel("Application 1")).andReturn(m_application1);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition1.getName())).andReturn(m_locationDefinition1);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition1)).andReturn(m_locationMonitor1);
        expect(m_pollerConfig.getPackage("raleigh")).andReturn(m_pkg);
        expect(m_pollerConfig.getServiceSelectorForPackage(m_pkg)).andReturn(m_selector);
        expect(m_monitoredServiceDao.findMatchingServices(m_selector)).andReturn(m_services);
        for (OnmsMonitoredService service : m_application1.getMemberServices()) {
            expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1, service)).andReturn(new OnmsLocationSpecificStatus(m_locationMonitor1, service, PollStatus.available()));
        }
    }

    public void replayEverything() {
        replay(m_pollerConfig);
        replay(m_monitoredServiceDao);
        replay(m_locationMonitorDao);
        replay(m_applicationDao);
        replay(m_categoryDao);
    }

    public void verifyEverything() {
        verify(m_pollerConfig);
        verify(m_monitoredServiceDao);
        verify(m_locationMonitorDao);
        verify(m_applicationDao);
        verify(m_categoryDao);
    }

    
    public OnmsMonitoredService findMonitoredService(Collection<OnmsMonitoredService> services, String interfaceIp, String serviceName) {
        for (OnmsMonitoredService service : services) {
            if (interfaceIp.equals(service.getIpAddress())
                    && serviceName.equals(service.getServiceName())) {
                return service;
            }
        }
        
        fail("Could not find service \"" + serviceName + "\" on interface \""
             + interfaceIp + "\"");
        
        // This will never be reached due to the above fail()
        return null;
    }

}

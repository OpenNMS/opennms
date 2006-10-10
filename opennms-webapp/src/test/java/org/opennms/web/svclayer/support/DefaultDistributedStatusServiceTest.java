package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

import junit.framework.TestCase;

public class DefaultDistributedStatusServiceTest extends TestCase {
    private DefaultDistributedStatusService m_service = new DefaultDistributedStatusService();
    
    private PollerConfig m_pollerConfig = createMock(PollerConfig.class);
    private MonitoredServiceDao m_monitoredServiceDao = createMock(MonitoredServiceDao.class);
    private LocationMonitorDao m_locationMonitorDao = createMock(LocationMonitorDao.class); 
    private ApplicationDao m_applicationDao = createMock(ApplicationDao.class);
    private CategoryDao m_categoryDao = createMock(CategoryDao.class);

    private OnmsMonitoringLocationDefinition m_locationDefinition;
    private OnmsApplication m_application;
    private OnmsLocationMonitor m_locationMonitor;
    
    private Package m_pkg;
    private ServiceSelector m_selector;
    private Collection<OnmsMonitoredService> m_services;
    private OnmsMonitoredService m_icmpService;
    private OnmsMonitoredService m_dnsService;
    private OnmsMonitoredService m_httpService;
    private OnmsMonitoredService m_httpsService;
    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_applicationServices;
        
    protected void setUp() {
        m_service.setPollerConfig(m_pollerConfig);
        m_service.setMonitoredServiceDao(m_monitoredServiceDao);
        m_service.setLocationMonitorDao(m_locationMonitorDao);
        m_service.setApplicationDao(m_applicationDao);
        m_service.setCategoryDao(m_categoryDao);
        
        m_locationDefinition = makeOnmsMonitoringLocationDefinition();
        m_application = makeOnmsApplication();

        m_locationMonitor = new OnmsLocationMonitor();

        m_pkg = new Package();

        List<String> serviceNames = new ArrayList<String>();
        serviceNames.add("ICMP");
        serviceNames.add("DNS");
        serviceNames.add("HTTP");
        serviceNames.add("HTTPS");
        m_selector = new ServiceSelector("IPADDR IPLIKE *.*.*.*", serviceNames);
        
        m_services = new HashSet<OnmsMonitoredService>();
        
        m_node = new OnmsNode();
        String ip = "1.2.3.4";
        m_node.setLabel("some node");
        
        m_icmpService = new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("ICMP"));
        m_services.add(m_icmpService);
        
        m_dnsService = new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("DNS"));
        m_services.add(m_dnsService);
        
        m_httpService = new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("HTTP"));
        m_services.add(m_httpService);
        
        m_httpsService = new OnmsMonitoredService(new OnmsIpInterface(ip, m_node), new OnmsServiceType("HTTPS"));
        m_services.add(m_httpsService);

        m_applicationServices = new HashSet<OnmsMonitoredService>();
        m_applicationServices.add(m_httpService);
        m_applicationServices.add(m_httpsService);
        m_application.setMemberServices(m_applicationServices);
        
    }
    
    public void testFindLocationSpecificStatusNullLocation() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("locationName cannot be null"));
        try {
            m_service.findLocationSpecificStatus(null, makeOnmsApplication().getLabel());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusNullApplication() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("applicationLabel cannot be null"));
        try {
            m_service.findLocationSpecificStatus(makeOnmsMonitoringLocationDefinition().getName(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatus() {
        expectEverything();
        
        replayEverything();
        
        List<OnmsLocationSpecificStatus> status =
            m_service.findLocationSpecificStatus(m_locationDefinition.getName(),
                                                 m_application.getLabel());

        verifyEverything();
        
        assertEquals("status list size", 2, status.size());
    }
    
    public void testCreateStatus() {
        expectEverything();
        
        // Once for each service that we display 
        expect(m_categoryDao.findByNode(m_node)).andReturn(null);
        expect(m_categoryDao.findByNode(m_node)).andReturn(null);
        
        replayEverything();
        m_service.createStatusTable(m_locationDefinition.getName(),
                                    m_application.getLabel());
        
        verifyEverything();
    }
    
    public void expectEverything() {
        expect(m_applicationDao.findByLabel("some application")).andReturn(m_application);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition("some location")).andReturn(m_locationDefinition);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition)).andReturn(m_locationMonitor);
        expect(m_pollerConfig.getPackage("default-remote")).andReturn(m_pkg);
        expect(m_pollerConfig.getServiceSelectorForPackage(m_pkg)).andReturn(m_selector);
        expect(m_monitoredServiceDao.findMatchingServices(m_selector)).andReturn(m_services);
        for (OnmsMonitoredService service : m_applicationServices) {
            expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor, service)).andReturn(new OnmsLocationSpecificStatus(m_locationMonitor, service, PollStatus.available()));
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


    private OnmsApplication makeOnmsApplication() {
        OnmsApplication application = new OnmsApplication();
        application.setLabel("some application");
        return application;
    }
    
    private OnmsMonitoringLocationDefinition makeOnmsMonitoringLocationDefinition() {
        OnmsMonitoringLocationDefinition location = new OnmsMonitoringLocationDefinition();
        location.setName("some location");
        location.setPollingPackageName("default-remote");
        return location;
    }

}

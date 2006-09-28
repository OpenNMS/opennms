package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Filter;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatusChange;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.remote.support.DefaultPollerBackEnd;

public class PollerBackEndTest extends TestCase {
    
    private DefaultPollerBackEnd m_backEnd = new DefaultPollerBackEnd();
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private PollerConfig m_pollerConfig;
    private OnmsMonitoringLocationDefinition m_locationDefinition;
    private Collection<OnmsMonitoringLocationDefinition> m_locations;
    private Package m_package;
    private Service m_httpSvcConfig;
    private Service m_dnsSvcConfig;
    private ServiceSelector m_serviceSelector;
    private OnmsMonitoredService m_httpService;
    private OnmsMonitoredService m_dnsService;
    private OnmsLocationMonitor m_locationMonitor;
    private OnmsMonitoredService[] m_monServices;
    private OnmsLocationSpecificStatusChange m_httpCurrentStatus;
    private OnmsLocationSpecificStatusChange m_dnsCurrentStatus;
    
    protected void setUp() throws Exception {
        
        m_locMonDao = createMock(LocationMonitorDao.class);
        m_monSvcDao = createMock(MonitoredServiceDao.class);
        m_pollerConfig = createMock(PollerConfig.class);
        
        m_backEnd = new DefaultPollerBackEnd();
        m_backEnd.setLocationMonitorDao(m_locMonDao);
        m_backEnd.setMonitoredServiceDao(m_monSvcDao);
        m_backEnd.setPollerConfig(m_pollerConfig);
        
        m_backEnd.afterPropertiesSet();

        // set up some objects that can be used to mock up the tests
        m_locationDefinition = new OnmsMonitoringLocationDefinition();
        m_locationDefinition.setArea("Oakland");
        m_locationDefinition.setName("OAK");
        m_locationDefinition.setPollingPackageName("OAKPackage");
        
        m_locations = Collections.singleton(m_locationDefinition);
        
        m_package = createPackage(m_locationDefinition.getPollingPackageName(), "ipaddr = '192.168.1.1'");
        m_httpSvcConfig = addService(m_package, "HTTP", 1234, "url", "http://www.opennms.org");
        m_dnsSvcConfig = addService(m_package, "DNS", 5678, "hostname", "www.opennms.org");
        m_serviceSelector = new ServiceSelector(m_package.getFilter().getContent(), Arrays.asList(new String[]{ "HTTP", "DNS" }));

        m_locationMonitor = new OnmsLocationMonitor();
        m_locationMonitor.setId(1);
        m_locationMonitor.setLocationDefinition(m_locationDefinition);
        
        NetworkBuilder builder = new NetworkBuilder(new OnmsDistPoller("localhost", "127.0.0.1"));
        OnmsNode node = builder.addNode("testNode");
        node.setId(1);
        OnmsIpInterface iface = builder.addInterface("192.168.1.1").getInterface();
        iface.setId(1);
        m_httpService = builder.addService(new OnmsServiceType("HTTP"));
        m_httpService.setId(1);
        m_dnsService = builder.addService(new OnmsServiceType("DNS"));
        m_dnsService.setId(2);
        
        
        
        m_monServices = new OnmsMonitoredService[] { m_httpService, m_dnsService };
        
        long now = System.currentTimeMillis();
        
        PollStatus httpResult = PollStatus.available(1000L);
        httpResult.setTimestamp(now - 300000);

        m_httpCurrentStatus = new OnmsLocationSpecificStatusChange(m_locationMonitor, m_httpService, httpResult);
        m_httpCurrentStatus.setId(1);

        PollStatus dnsResult = PollStatus.unavailable("Non responsive");
        dnsResult.setTimestamp(now - 300000);

        m_dnsCurrentStatus = new OnmsLocationSpecificStatusChange(m_locationMonitor, m_dnsService, dnsResult);
        m_dnsCurrentStatus.setId(2);

    }
    
    public void testGetMonitoringLocations() {
        
        expect(m_locMonDao.findAllMonitoringLocationDefinitions()).andReturn(m_locations);
        
        replay(m_locMonDao);
        
        Collection<OnmsMonitoringLocationDefinition> returned = m_backEnd.getMonitoringLocations();
        
        verify(m_locMonDao);
        
        assertEquals(m_locations, returned);
        
    }
    
    public void testRegisterLocationMonitor() {
        
 
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);
        m_locMonDao.save(isA(OnmsLocationMonitor.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                mon.setId(1);
                return null;
            }
            
        });
        
        replay(m_locMonDao);
        
        int locationMonitorId = m_backEnd.registerLocationMonitor(m_locationDefinition.getName());
        
        verify(m_locMonDao);
        
        assertEquals(1, locationMonitorId);
        
        
        
    }
    
    // reportResult test variations
    // what if we cant' find the locationMonitor with that ID
    // what if we can't find the service with that ID
    // what if we can't find a current status
    // what if the new status is the same as the current status
    // what if they are different
    // test for saving rrd data
    // do I send events for status changed
    public void testStatusChangeFromUpToDown() {
        
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(1)).andReturn(m_httpService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_httpService)).andReturn(m_httpCurrentStatus);
        
        final PollStatus newStatus = PollStatus.unavailable("Test Down");
        
        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatusChange.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationSpecificStatusChange statusChange = (OnmsLocationSpecificStatusChange)getCurrentArguments()[0];
                assertEquals(m_locationMonitor, statusChange.getLocationMonitor());
                assertEquals(m_httpService, statusChange.getMonitoredService());
                assertEquals(newStatus.getStatusCode(), statusChange.getStatus().getStatusCode());
                assertEquals(newStatus.getResponseTime(), statusChange.getStatus().getResponseTime());
                assertEquals(newStatus.getReason(), statusChange.getStatus().getReason());
                
                return null;
            }
            
        });
        
        replay(m_locMonDao, m_monSvcDao, m_pollerConfig);
        
        m_backEnd.reportResult(1, 1, newStatus);
        
        verify(m_locMonDao, m_monSvcDao, m_pollerConfig);
    }
    
    public void testGetPollerConfiguration() {
        
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(m_locationMonitor);
        
        expect(m_pollerConfig.getPackage(m_locationDefinition.getPollingPackageName())).andReturn(m_package);
        expect(m_pollerConfig.getServiceSelectorForPackage(m_package)).andReturn(m_serviceSelector);
        expect(m_pollerConfig.getServiceInPackage("HTTP", m_package)).andReturn(m_httpSvcConfig);
        expect(m_pollerConfig.getServiceInPackage("DNS", m_package)).andReturn(m_dnsSvcConfig);
        
        expect(m_monSvcDao.findMatchingServices(m_serviceSelector)).andReturn(Arrays.asList(m_monServices));
        
        replay(m_locMonDao, m_monSvcDao, m_pollerConfig);
        
        PollerConfiguration config = m_backEnd.getPollerConfiguration(m_locationMonitor.getId());
        
        verify(m_locMonDao, m_monSvcDao, m_pollerConfig);
        
        assertNotNull(config);
        assertEquals(2, config.getConfigurationForPoller().length);
        assertEquals(m_httpService, config.getConfigurationForPoller()[0].getMonitoredService());
        assertEquals(m_dnsService, config.getConfigurationForPoller()[1].getMonitoredService());
        assertEquals(5678, config.getConfigurationForPoller()[1].getPollModel().getPollInterval());
        assertTrue(config.getConfigurationForPoller()[1].getMonitorConfiguration().containsKey("hostname"));
    }

    private Package createPackage(String pkgName, String filterRule) {
        Package pkg = new Package();
        pkg.setName(pkgName);
        pkg.setFilter(new Filter());
        pkg.getFilter().setContent(filterRule);
        return pkg;
    }

    private Service addService(Package pkg, String serviceName, int serviceInterval, String... parms) {
        
        // assume that parms are key then value pairs
        assertTrue(parms.length % 2 == 0);
        
        Service service = new Service();
        service.setName(serviceName);
        service.setInterval(serviceInterval);
        
        for(int i = 0; i < parms.length-1; i+=2) {
            String key = parms[i];
            String value = parms[i+1];
            addParameter(service, key, value);
        }

        pkg.addService(service);
        return service;
    }    
    
    private void addParameter(Service service, String key, String value) {
        Parameter param = new Parameter();
        param.setKey(key);
        param.setValue(value);
        service.addParameter(param);
    }

    

}

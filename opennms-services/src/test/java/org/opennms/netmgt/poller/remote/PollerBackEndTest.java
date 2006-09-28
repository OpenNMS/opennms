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
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.remote.support.DefaultPollerBackEnd;

public class PollerBackEndTest extends TestCase {
    
    private DefaultPollerBackEnd m_backEnd = new DefaultPollerBackEnd();
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private PollerConfig m_pollerConfig;
    
    protected void setUp() throws Exception {
        
        m_locMonDao = createMock(LocationMonitorDao.class);
        m_monSvcDao = createMock(MonitoredServiceDao.class);
        m_pollerConfig = createMock(PollerConfig.class);
        
        m_backEnd = new DefaultPollerBackEnd();
        m_backEnd.setLocationMonitorDao(m_locMonDao);
        m_backEnd.setMonitoredServiceDao(m_monSvcDao);
        m_backEnd.setPollerConfig(m_pollerConfig);
        
        m_backEnd.afterPropertiesSet();
        
    }
    
    public void testGetMonitoringLocations() {
        
        Collection<OnmsMonitoringLocationDefinition> locations = new ArrayList<OnmsMonitoringLocationDefinition>();
        
        expect(m_locMonDao.findAllMonitoringLocationDefinitions()).andReturn(locations);
        
        replay(m_locMonDao);
        
        Collection<OnmsMonitoringLocationDefinition> returned = m_backEnd.getMonitoringLocations();
        
        verify(m_locMonDao);
        
        assertEquals(locations, returned);
        
    }
    
    public void testRegisterLocationMonitor() {
        
        OnmsMonitoringLocationDefinition def = new OnmsMonitoringLocationDefinition();
        def.setArea("Oakland");
        def.setName("OAK");
        
 
        expect(m_locMonDao.findMonitoringLocationDefinition("OAK")).andReturn(def);
        m_locMonDao.save(isA(OnmsLocationMonitor.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                mon.setId(1);
                return null;
            }
            
        });
        
        replay(m_locMonDao);
        
        int locationMonitorId = m_backEnd.registerLocationMonitor(def.getName());
        
        verify(m_locMonDao);
        
        assertEquals(1, locationMonitorId);
        
        
        
    }
    
    public void testGetPollerConfiguration() {
        
        String pkgName = "OAKPackage";

        OnmsMonitoringLocationDefinition def = new OnmsMonitoringLocationDefinition();
        def.setArea("Oakland");
        def.setName("OAK");
        def.setPollingPackageName(pkgName);
        
        String filterRule = "ipaddr = '192.168.1.1'";
        Package pkg = createPackage(pkgName, filterRule);
        Service http = addService(pkg, "HTTP", 1234, "url", "http://www.opennms.org");
        Service dns = addService(pkg, "DNS", 5678, "hostname", "www.opennms.org");
        
        String[] svcNames = { "HTTP", "DNS" };
        
        ServiceSelector selector = new ServiceSelector(filterRule, Arrays.asList(svcNames));
        
        OnmsMonitoredService[] monServices = new OnmsMonitoredService[2];
        
        NetworkBuilder builder = new NetworkBuilder(new OnmsDistPoller("localhost", "127.0.0.1"));
        builder.addNode("testNode");
        builder.addInterface("192.168.1.1");
        monServices[0] = builder.addService(new OnmsServiceType("HTTP"));
        monServices[1] = builder.addService(new OnmsServiceType("DNS"));
        
        OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setId(1);
        mon.setLocationDefinition(def);
        
        expect(m_locMonDao.get(mon.getId())).andReturn(mon);
        
        expect(m_pollerConfig.getPackage(pkgName)).andReturn(pkg);
        expect(m_pollerConfig.getServiceSelectorForPackage(pkg)).andReturn(selector);
        expect(m_pollerConfig.getServiceInPackage("HTTP", pkg)).andReturn(http);
        expect(m_pollerConfig.getServiceInPackage("DNS", pkg)).andReturn(dns);
        
        expect(m_monSvcDao.findMatchingServices(selector)).andReturn(Arrays.asList(monServices));
        
        replay(m_locMonDao, m_monSvcDao, m_pollerConfig);
        
        PollerConfiguration config = m_backEnd.getPollerConfiguration(mon.getId());
        
        verify(m_locMonDao, m_monSvcDao, m_pollerConfig);
        
        assertNotNull(config);
        assertEquals(2, config.getConfigurationForPoller().length);
        assertEquals(monServices[0], config.getConfigurationForPoller()[0].getMonitoredService());
        assertEquals(monServices[1], config.getConfigurationForPoller()[1].getMonitoredService());
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

package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.opennms.core.utils.TimeKeeper;
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
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.remote.support.DefaultPollerBackEnd;
import org.quartz.Scheduler;

public class PollerBackEndTest extends TestCase {
    
    private final class StatusChecker implements IAnswer<Object> {
        private OnmsLocationSpecificStatus m_status;

        private StatusChecker(OnmsLocationSpecificStatus status) {
            m_status = status;
        }

        public Object answer() throws Throwable {
            OnmsLocationSpecificStatus status = (OnmsLocationSpecificStatus)getCurrentArguments()[0];
            assertEquals(m_status.getLocationMonitor(), status.getLocationMonitor());
            assertEquals(m_status.getMonitoredService(), status.getMonitoredService());
            assertEquals(m_status.getPollResult().getStatusCode(), status.getPollResult().getStatusCode());
            assertEquals(m_status.getPollResult().getResponseTime(), status.getPollResult().getResponseTime());
            assertEquals(m_status.getPollResult().getReason(), status.getPollResult().getReason());
            
            return null;
        }
    }

    static final int UNRESPONSIVE_TIMEOUT = 300000;

    // the class under test
    private DefaultPollerBackEnd m_backEnd = new DefaultPollerBackEnd();
    
    // mock objects that the class will call
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private PollerConfig m_pollerConfig;
    private Scheduler m_scheduler;
    private TimeKeeper m_timeKeeper;
    
    // helper objects used to respond from the mock objects
    private OnmsMonitoringLocationDefinition m_locationDefinition;
    private Package m_package;
    private ServiceSelector m_serviceSelector;
    private OnmsLocationMonitor m_locationMonitor;
    
    private Service m_httpSvcConfig;
    private Service m_dnsSvcConfig;
    private OnmsMonitoredService m_httpService;
    private OnmsMonitoredService m_dnsService;
    private OnmsMonitoredService[] m_monServices;
    private OnmsLocationSpecificStatus m_httpCurrentStatus;
    private OnmsLocationSpecificStatus m_dnsCurrentStatus;

    private Date m_startTime;
    
    protected void setUp() throws Exception {
        
        
        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndTest-home");
        
        m_locMonDao = createMock(LocationMonitorDao.class);
        m_monSvcDao = createMock(MonitoredServiceDao.class);
        m_pollerConfig = createMock(PollerConfig.class);
        m_scheduler = createMock(Scheduler.class);
        m_timeKeeper = createMock(TimeKeeper.class);
        
        m_backEnd = new DefaultPollerBackEnd();
        m_backEnd.setLocationMonitorDao(m_locMonDao);
        m_backEnd.setMonitoredServiceDao(m_monSvcDao);
        m_backEnd.setPollerConfig(m_pollerConfig);
        m_backEnd.setTimeKeeper(m_timeKeeper);
        m_backEnd.setUnresponsiveTimeout(UNRESPONSIVE_TIMEOUT);
        
        m_startTime = new Date(System.currentTimeMillis() - 1000);
        expect(m_timeKeeper.getCurrentDate()).andReturn(m_startTime);
        replay(m_timeKeeper);
        m_backEnd.afterPropertiesSet();
        verify(m_timeKeeper);
        reset(m_timeKeeper);
        

        // set up some objects that can be used to mock up the tests
        
        // the location definition
        m_locationDefinition = new OnmsMonitoringLocationDefinition();
        m_locationDefinition.setArea("Oakland");
        m_locationDefinition.setName("OAK");
        m_locationDefinition.setPollingPackageName("OAKPackage");
        
        m_package = createPackage(m_locationDefinition.getPollingPackageName(), "ipaddr = '192.168.1.1'");
        m_serviceSelector = new ServiceSelector(m_package.getFilter().getContent(), Arrays.asList(new String[]{ "HTTP", "DNS" }));

        m_httpSvcConfig = addService(m_package, "HTTP", 1234, "url", "http://www.opennms.org");
        m_dnsSvcConfig = addService(m_package, "DNS", 5678, "hostname", "www.opennms.org");

        m_locationMonitor = new OnmsLocationMonitor();
        m_locationMonitor.setId(1);
        m_locationMonitor.setDefinitionName(m_locationDefinition.getName());
        
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
        httpResult.setTimestamp(new Date(now - 300000));

        m_httpCurrentStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_httpService, httpResult);
        m_httpCurrentStatus.setId(1);

        PollStatus dnsResult = PollStatus.unavailable("Non responsive");
        dnsResult.setTimestamp(new Date(now - 300000));

        m_dnsCurrentStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, dnsResult);
        m_dnsCurrentStatus.setId(2);

    }
    
    public void testGetMonitoringLocations() {
        
        Collection<OnmsMonitoringLocationDefinition> locations = Collections.singleton(m_locationDefinition);
        
        expect(m_locMonDao.findAllMonitoringLocationDefinitions()).andReturn(locations);
        
        replayMocks();
        
        Collection<OnmsMonitoringLocationDefinition> returned = m_backEnd.getMonitoringLocations();
        
        verifyMocks();
        
        assertEquals(locations, returned);
        
    }
    
    public void testRegisterLocationMonitor() {
        
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);

        m_locMonDao.save(isA(OnmsLocationMonitor.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                mon.setId(1);
                assertEquals(OnmsLocationMonitor.MonitorStatus.REGISTERED, mon.getStatus());
                return null;
            }
            
        });
        
        replayMocks();
        
        int locationMonitorId = m_backEnd.registerLocationMonitor(m_locationDefinition.getName());
        
        verifyMocks();
        
        assertEquals(1, locationMonitorId);
        
    }
    
    // reportResult test variations
    // what if we cant' find the locationMonitor with that ID
    // what if we can't find the service with that ID
    // what if we can't find a current status
    // do I send events for status changed
    public void testStatusChangeFromUpToDown() {
        
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(1)).andReturn(m_httpService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_httpService)).andReturn(m_httpCurrentStatus);
        
        final PollStatus newStatus = PollStatus.unavailable("Test Down");
        
        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_httpService, newStatus);
        
        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));

        replayMocks();
        
        m_backEnd.reportResult(1, 1, newStatus);
        
        verifyMocks();

    }
    
    public void testStatusChangeFromDownToUp() {
        
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(m_dnsCurrentStatus);
        
        final PollStatus newStatus = PollStatus.available(1234);
        
        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, newStatus);
        
        m_pollerConfig.saveResponseTimeData(null, m_dnsService, 1234, m_package);
        
        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));
        
        replayMocks();
        
        m_backEnd.reportResult(1, 2, newStatus);
        
        verifyMocks();
        
    }
    
    public void testStatusWhenNoneKnown() {
        
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(null);
        
        final PollStatus newStatus = PollStatus.available(1234);
        
        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, newStatus);
        
        m_pollerConfig.saveResponseTimeData(null, m_dnsService, 1234, m_package);
        
        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));
        
        replayMocks();
        
        m_backEnd.reportResult(1, 2, newStatus);
        
        verifyMocks();
        
    }

    public void testStatusDownWhenDown() {
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(m_dnsCurrentStatus);
        
        final PollStatus newStatus = PollStatus.unavailable("Still Down");
        
        // expect no status changes
        // expect no performance data
        
        replayMocks();
        
        m_backEnd.reportResult(1, 2, newStatus);
        
        verifyMocks();
    }
    
    public void testStatusUpWhenUp() {
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(1)).andReturn(m_httpService);
        
        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_httpService)).andReturn(m_httpCurrentStatus);
        
        final PollStatus newStatus = PollStatus.available(1776);
        
        // expect to save performance data
        m_pollerConfig.saveResponseTimeData(null, m_httpService, 1776, m_package);
        
        // expect no status change
        
        replayMocks();
        
        m_backEnd.reportResult(1, 1, newStatus);
        
        verifyMocks();
    }

    public void testGetPollerConfiguration() {
        
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(m_locationMonitor);
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);
        
        expect(m_pollerConfig.getPackage(m_locationDefinition.getPollingPackageName())).andReturn(m_package);
        expect(m_pollerConfig.getServiceSelectorForPackage(m_package)).andReturn(m_serviceSelector);
        expect(m_pollerConfig.getServiceInPackage("HTTP", m_package)).andReturn(m_httpSvcConfig);
        expect(m_pollerConfig.getServiceInPackage("DNS", m_package)).andReturn(m_dnsSvcConfig);
        
        expect(m_monSvcDao.findMatchingServices(m_serviceSelector)).andReturn(Arrays.asList(m_monServices));
        
        replayMocks();
        
        PollerConfiguration config = m_backEnd.getPollerConfiguration(m_locationMonitor.getId());
        
        verifyMocks();
        
        assertNotNull(config);
        assertEquals(m_startTime, config.getConfigurationTimestamp());
        assertEquals(2, config.getConfigurationForPoller().length);
        assertEquals(m_httpService, config.getConfigurationForPoller()[0].getMonitoredService());
        assertEquals(m_dnsService, config.getConfigurationForPoller()[1].getMonitoredService());
        assertEquals(5678, config.getConfigurationForPoller()[1].getPollModel().getPollInterval());
        assertTrue(config.getConfigurationForPoller()[1].getMonitorConfiguration().containsKey("hostname"));
    }
    
    public void testPollerStarting() {
        
        expectLocationMonitorStatusChanged(MonitorStatus.STARTED);
        
        replayMocks();

        m_backEnd.pollerStarting(1);
        
        verifyMocks();
    }

    private void expectLocationMonitorStatusChanged(final MonitorStatus expectedStatus) {
        final Date now = new Date();
        expect(m_timeKeeper.getCurrentDate()).andReturn(now);
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(m_locationMonitor);
        m_locMonDao.update(m_locationMonitor);
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                assertEquals(expectedStatus, mon.getStatus());
                assertEquals(now, mon.getLastCheckInTime());
                return null;
            }
            
        });
    }
    
    public void testPollerStopping() {

        expectLocationMonitorStatusChanged(MonitorStatus.STOPPED);
        
        replayMocks();

        m_backEnd.pollerStopping(1);
        
        verifyMocks();
    }
    
    public void testPollerCheckingIn() {
        
        Date configDate = m_startTime;

        expectLocationMonitorStatusChanged(m_locationMonitor.getStatus());
        
        replayMocks();

        assertFalse("Expect configs to be up to date", m_backEnd.pollerCheckingIn(1, configDate));

        verifyMocks();
        
        expect(m_timeKeeper.getCurrentDate()).andReturn(new Date());
        replayMocks();
        m_backEnd.configurationUpdated();
        verifyMocks();
        
        expectLocationMonitorStatusChanged(m_locationMonitor.getStatus());
        
        replayMocks();

        assertTrue("Expect configs to be out of date", m_backEnd.pollerCheckingIn(1, configDate));
        
        verifyMocks();
    }
    
    public void testTimeOutOnCheckin() {
        final Date now = new Date();

        m_locationMonitor.setStatus(MonitorStatus.STARTED);
        m_locationMonitor.setLastCheckInTime(new Date(now.getTime() - UNRESPONSIVE_TIMEOUT - 100));
        
        expect(m_locMonDao.findAll()).andReturn(Collections.singleton(m_locationMonitor));
        
        expect(m_timeKeeper.getCurrentDate()).andReturn(now);

        m_locMonDao.update(m_locationMonitor);
        expectLastCall().andAnswer(new IAnswer<Object>() {
        
            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                assertEquals(MonitorStatus.UNRESPONSIVE, mon.getStatus());
                assertTrue(mon.getLastCheckInTime().before(new Date(now.getTime() - UNRESPONSIVE_TIMEOUT)));
                return null;
            }
            
        });
        
        replayMocks();

        m_backEnd.checkforUnresponsiveMonitors();
        
        verifyMocks();
    }

    private void verifyMocks() {
        verify(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper);
        reset(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper);
    }

    private void replayMocks() {
        replay(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper);
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

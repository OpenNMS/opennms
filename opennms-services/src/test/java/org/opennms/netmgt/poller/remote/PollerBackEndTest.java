
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Filter;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.support.DefaultPollerBackEnd;
import org.opennms.netmgt.xml.event.Event;
import org.quartz.Scheduler;

public class PollerBackEndTest extends TestCase {

    public static class EventEquals implements IArgumentMatcher {

        private Event m_expected;

        EventEquals(Event value) {
            m_expected = value;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append(m_expected);
        }

        public boolean matches(Object argument) {
            Event actual = (Event)argument;
            if (m_expected == null) {
                return actual == null;
            }

            return (
                    m_expected.getUei().equals(actual.getUei()) &&
                    nullSafeEquals(m_expected.getSource(), actual.getSource()) &&
                    m_expected.getNodeid() == actual.getNodeid() &&
                    nullSafeEquals(m_expected.getInterface(), actual.getInterface())  &&
                    nullSafeEquals(m_expected.getService(), actual.getService()) &&
                    EventUtils.getLongParm(m_expected, EventConstants.PARM_LOCATION_MONITOR_ID, -1) 
                    == EventUtils.getLongParm(actual, EventConstants.PARM_LOCATION_MONITOR_ID, -1)
            );
        }

        private boolean nullSafeEquals(Object a, Object b) {
            return (a == null ? b == null : a.equals(b));
        }

    }

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

    static final int DISCONNECTED_TIMEOUT = 300000;

    // the class under test
    private DefaultPollerBackEnd m_backEnd = new DefaultPollerBackEnd();
    // mock objects that the class will call
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private PollerConfig m_pollerConfig;
    private Scheduler m_scheduler;
    private TimeKeeper m_timeKeeper;

    private EventIpcManager m_eventIpcManager;
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


    private HashMap<String, String> m_pollerDetails;

    private void addParameter(Service service, String key, String value) {
        Parameter param = new Parameter();
        param.setKey(key);
        param.setValue(value);
        service.addParameter(param);
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

    private void anticipateDisconnectedEvent() {
        anticipateEvent(createDisconnectedEvent());
    }

    private void anticipateEvent(Event e) {
        m_eventIpcManager.sendNow(eq(e));
    }

    private void anticipateMonitorStarted() {
        anticipateEvent(createMonitorStartedEvent());
    }

    private void anticipateMonitorRegisteredEvent() {
        anticipateEvent(createMonitorRegisteredEvent());
    }

    private void anticipateMonitorStoppedEvent() {
        anticipateEvent(createMonitorStoppedEvent());
    }

    private Event createDisconnectedEvent() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI, "PollerBackEnd")
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, 1);

        Event e = eventBuilder.getEvent();
        return e;
    }

    private Event createMonitorRegisteredEvent() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.LOCATION_MONITOR_REGISTERED_UEI, "PollerBackEnd")
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, 1);

        Event e = eventBuilder.getEvent();
        return e;
    }

    private Event createMonitorStartedEvent() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.LOCATION_MONITOR_STARTED_UEI, "PollerBackEnd")
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, 1);

        Event e = eventBuilder.getEvent();
        return e;
    }

    private Event createMonitorStoppedEvent() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.LOCATION_MONITOR_STOPPED_UEI, "PollerBackEnd")
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, 1);

        return eventBuilder.getEvent();
    }

    private Package createPackage(String pkgName, String filterRule) {
        Package pkg = new Package();
        pkg.setName(pkgName);
        pkg.setFilter(new Filter());
        pkg.getFilter().setContent(filterRule);
        return pkg;
    }

    private Event createReconnectedEvent() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.LOCATION_MONITOR_RECONNECTED_UEI, "PollerBackEnd");
        eventBuilder.addParam(EventConstants.PARM_LOCATION_MONITOR_ID, 1);

        return eventBuilder.getEvent();
    }

    private Event eq(Event e) {
        EasyMock.reportMatcher(new EventEquals(e));
        return null;

    }

    private void expectLocationMonitorStarted() {
        final Date now = new Date();
        expect(m_timeKeeper.getCurrentDate()).andReturn(now);
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(m_locationMonitor);
        m_locMonDao.update(m_locationMonitor);
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                assertEquals(MonitorStatus.STARTED, mon.getStatus());
                assertEquals(now, mon.getLastCheckInTime());
                assertEquals(m_pollerDetails, mon.getDetails());
                return null;
            }

        });
    }

    private void expectLocationMonitorStatusChanged(final MonitorStatus oldStatus, final MonitorStatus expectedStatus) {
        final Date now = new Date();
        if (oldStatus != null) {
            switch (oldStatus) {
            case DISCONNECTED:
            case STARTED:
                expect(m_timeKeeper.getCurrentTime()).andReturn(now.getTime());
            }
        }
        expect(m_timeKeeper.getCurrentDate()).andReturn(now);
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(m_locationMonitor);
        m_locMonDao.update(m_locationMonitor);
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                assertEquals("unexpected new monitor state", expectedStatus, mon.getStatus());
                assertEquals(now, mon.getLastCheckInTime());
                return null;
            }

        });
    }

    private void replayMocks() {
        replay(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper, m_eventIpcManager);
    }

    protected void setUp() throws Exception {


        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndTest-home");

        m_locMonDao = createMock(LocationMonitorDao.class);
        m_monSvcDao = createMock(MonitoredServiceDao.class);
        m_pollerConfig = createMock(PollerConfig.class);
        m_scheduler = createMock(Scheduler.class);
        m_timeKeeper = createMock(TimeKeeper.class);
        m_eventIpcManager = createMock(EventIpcManager.class);

        m_backEnd = new DefaultPollerBackEnd();
        m_backEnd.setLocationMonitorDao(m_locMonDao);
        m_backEnd.setMonitoredServiceDao(m_monSvcDao);
        m_backEnd.setPollerConfig(m_pollerConfig);
        m_backEnd.setTimeKeeper(m_timeKeeper);
        m_backEnd.setEventIpcManager(m_eventIpcManager);
        m_backEnd.setDisconnectedTimeout(DISCONNECTED_TIMEOUT);

        
        m_startTime = new Date(System.currentTimeMillis() - 600000);
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
        builder.addNode("testNode").setId(1);
        builder.addInterface("192.168.1.1").setId(1);
        m_httpService = builder.addService(new OnmsServiceType("HTTP"));
        m_httpService.setId(1);
        m_dnsService = builder.addService(new OnmsServiceType("DNS"));
        m_dnsService.setId(2);

        m_monServices = new OnmsMonitoredService[] { m_httpService, m_dnsService };

        long now = System.currentTimeMillis();

        PollStatus httpResult = PollStatus.available(1000.0);
        httpResult.setTimestamp(new Date(now - 300000));

        m_httpCurrentStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_httpService, httpResult);
        m_httpCurrentStatus.setId(1);

        PollStatus dnsResult = PollStatus.unavailable("Non responsive");
        dnsResult.setTimestamp(new Date(now - 300000));

        m_dnsCurrentStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, dnsResult);
        m_dnsCurrentStatus.setId(2);


        m_pollerDetails = new HashMap<String, String>();
        m_pollerDetails.put("os.name", "WonkaOS");
        m_pollerDetails.put("os.version", "1.2.3");


    }

    public void testGetMonitoringLocations() {

        List<OnmsMonitoringLocationDefinition> locations = Collections.singletonList(m_locationDefinition);

        expect(m_locMonDao.findAllMonitoringLocationDefinitions()).andReturn(locations);

        replayMocks();

        Collection<OnmsMonitoringLocationDefinition> returned = m_backEnd.getMonitoringLocations();

        verifyMocks();

        assertEquals(locations, returned);

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
        assertNotNull(config.getPolledServices());
        assertEquals(2, config.getPolledServices().length);
        //Because the config is sorted DNS will change from index 1 to index 0;
        assertEquals(m_dnsService.getServiceName(), config.getPolledServices()[0].getSvcName());
        assertEquals(m_httpService.getServiceName(), config.getPolledServices()[1].getSvcName());
        assertEquals(5678, config.getPolledServices()[0].getPollModel().getPollInterval());
        assertTrue(config.getPolledServices()[0].getMonitorConfiguration().containsKey("hostname"));
    }

    public void testGetPollerConfigurationForDeletedMonitor() {
        expect(m_locMonDao.get(m_locationMonitor.getId())).andReturn(null);

        replayMocks();

        PollerConfiguration config = m_backEnd.getPollerConfiguration(m_locationMonitor.getId());

        verifyMocks();

        assertNotNull(config);
        assertTrue(m_startTime.after(config.getConfigurationTimestamp()));
        assertNotNull(config.getPolledServices());
        assertEquals(0, config.getPolledServices().length);
    }


    public void testGetServiceMonitorLocators() {

        Collection<ServiceMonitorLocator> locators = new ArrayList<ServiceMonitorLocator>();

        expect(m_pollerConfig.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR)).andReturn(locators);

        replayMocks();

        Collection<ServiceMonitorLocator> results = m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR);

        verifyMocks();

        assertSame(locators, results);


    }

    private void testGlobalConfigChange(MonitorStatus oldStatus, MonitorStatus newStatus, Event e) {

        verifyPollerCheckingIn(MonitorStatus.STARTED, MonitorStatus.STARTED, MonitorStatus.STARTED);
        updateConfiguration();
        verifyPollerCheckingIn(oldStatus, newStatus, MonitorStatus.CONFIG_CHANGED, e);
    }

    public void testGlobalConfigChangeFromDisconnected() {
        testGlobalConfigChange(MonitorStatus.DISCONNECTED, MonitorStatus.STARTED, createReconnectedEvent());		
    }

    public void testGlobalConfigChangeFromStarted() {
        testGlobalConfigChange(MonitorStatus.STARTED, MonitorStatus.STARTED, null);
    }

    public void testPollerCheckingInFromDisconnected() {
        verifyPollerCheckingIn(MonitorStatus.DISCONNECTED, MonitorStatus.STARTED, MonitorStatus.STARTED, createReconnectedEvent());
    }

    public void testPollerCheckingInFromPaused() {
        verifyPollerCheckingIn(MonitorStatus.PAUSED, MonitorStatus.PAUSED, MonitorStatus.PAUSED);
    }

    public void testPollerCheckingInFromStarted() {
        verifyPollerCheckingIn(MonitorStatus.STARTED, MonitorStatus.STARTED, MonitorStatus.STARTED);
    }

    public void testPollerCheckingInFromConfigChanged() {
        verifyPollerCheckingIn(MonitorStatus.CONFIG_CHANGED, MonitorStatus.STARTED, MonitorStatus.CONFIG_CHANGED);
    }

    public void testPollerStarting() {

        anticipateMonitorStarted();

        expectLocationMonitorStarted();

        replayMocks();

        m_backEnd.pollerStarting(1, m_pollerDetails);

        verifyMocks();
    }

    public void testPollerStopping() {

        anticipateMonitorStoppedEvent();

        expectLocationMonitorStatusChanged(null, MonitorStatus.STOPPED);

        replayMocks();

        m_backEnd.pollerStopping(1);

        verifyMocks();
    }
    
    public void testPollerStoppingWithBadLocationMonitorId() {
        expect(m_locMonDao.get(1)).andReturn(null);
        
        replayMocks();
        m_backEnd.pollerStopping(1);
        verifyMocks();
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
        anticipateMonitorRegisteredEvent();

        replayMocks();

        int locationMonitorId = m_backEnd.registerLocationMonitor(m_locationDefinition.getName());

        verifyMocks();

        assertEquals(1, locationMonitorId);

    }
    
    public void testReportResultWithBadLocationMonitorId() {
        expect(m_locMonDao.get(1)).andReturn(null);
        
        replayMocks();
        m_backEnd.reportResult(1, 1, PollStatus.up());
        verifyMocks();
    }

    public void testReportResultWithBadServiceId() {
        expect(m_locMonDao.get(1)).andReturn(new OnmsLocationMonitor());
        expect(m_monSvcDao.get(1)).andReturn(null);
        
        replayMocks();
        m_backEnd.reportResult(1, 1, PollStatus.up());
        verifyMocks();
    }
    
    public void testReportResultWithNullPollResult() {
    	expect(m_locMonDao.get(1)).andThrow(new RuntimeException("crazy location monitor exception"));

        replayMocks();
        m_backEnd.reportResult(1, 1, null);
        verifyMocks();
    }

    public void testStatusChangeFromDownToUp() {

        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);

        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(m_dnsCurrentStatus);

        // called when saving performance data
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);
        expect(m_pollerConfig.getPackage(m_locationDefinition.getPollingPackageName())).andReturn(m_package);

        final PollStatus newStatus = PollStatus.available(1234.0);

        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, newStatus);

        m_pollerConfig.saveResponseTimeData(Integer.toString(m_locationMonitor.getId()), m_dnsService, 1234, m_package);

        // TODO: make anticipate method
        EventBuilder eventBuilder = new EventBuilder(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, "PollerBackEnd")
        .setMonitoredService(m_dnsService)
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, "1");

        m_eventIpcManager.sendNow(eq(eventBuilder.getEvent()));

        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));

        replayMocks();

        m_backEnd.reportResult(1, 2, newStatus);

        verifyMocks();

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

        // TODO: make anticipate method
        EventBuilder eventBuilder = new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "PollerBackEnd")
        .setMonitoredService(m_httpService)
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, "1");


        m_eventIpcManager.sendNow(eq(eventBuilder.getEvent()));

        final PollStatus newStatus = PollStatus.unavailable("Test Down");

        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_httpService, newStatus);

        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));

        replayMocks();

        m_backEnd.reportResult(1, 1, newStatus);

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

    public void testStatusDownWhenNoneKnown() {

        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);

        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(null);

        final PollStatus newStatus = PollStatus.unavailable("where'd he go?");

        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, newStatus);

        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));

        // expect a status change if the node is now down and we didn't know before
        EventBuilder eventBuilder = new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "PollerBackEnd")
        .setMonitoredService(m_dnsService)
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, "1");


        m_eventIpcManager.sendNow(eq(eventBuilder.getEvent()));

        replayMocks();

        m_backEnd.reportResult(1, 2, newStatus);

        verifyMocks();

    }

    public void testStatusUpWhenNoneKnown() {

        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(2)).andReturn(m_dnsService);

        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_dnsService)).andReturn(null);

        // called when saving performance data
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);
        expect(m_pollerConfig.getPackage(m_locationDefinition.getPollingPackageName())).andReturn(m_package);

        final PollStatus newStatus = PollStatus.available(1234.0);

        OnmsLocationSpecificStatus expectedStatus = new OnmsLocationSpecificStatus(m_locationMonitor, m_dnsService, newStatus);

        m_pollerConfig.saveResponseTimeData(Integer.toString(m_locationMonitor.getId()), m_dnsService, 1234, m_package);

        m_locMonDao.saveStatusChange(isA(OnmsLocationSpecificStatus.class));
        expectLastCall().andAnswer(new StatusChecker(expectedStatus));

        replayMocks();

        m_backEnd.reportResult(1, 2, newStatus);

        verifyMocks();

    }

    public void testStatusUpWhenUp() {
        expect(m_locMonDao.get(1)).andReturn(m_locationMonitor);
        expect(m_monSvcDao.get(1)).andReturn(m_httpService);

        expect(m_locMonDao.getMostRecentStatusChange(m_locationMonitor, m_httpService)).andReturn(m_httpCurrentStatus);

        final PollStatus newStatus = PollStatus.available(1776.0);

        // called when saving performance data
        expect(m_locMonDao.findMonitoringLocationDefinition(m_locationDefinition.getName())).andReturn(m_locationDefinition);
        expect(m_pollerConfig.getPackage(m_locationDefinition.getPollingPackageName())).andReturn(m_package);

        // expect to save performance data
        m_pollerConfig.saveResponseTimeData(Integer.toString(m_locationMonitor.getId()), m_httpService, 1776, m_package);

        // expect no status change

        replayMocks();

        m_backEnd.reportResult(1, 1, newStatus);

        verifyMocks();
    }

    public void testTimeOutOnCheckin() {
        final Date now = new Date();

        m_locationMonitor.setStatus(MonitorStatus.STARTED);
        m_locationMonitor.setLastCheckInTime(new Date(now.getTime() - DISCONNECTED_TIMEOUT - 100));

        expect(m_locMonDao.findAll()).andReturn(Collections.singletonList(m_locationMonitor));

        expect(m_timeKeeper.getCurrentDate()).andReturn(now);

        anticipateDisconnectedEvent();

        m_locMonDao.update(m_locationMonitor);
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                OnmsLocationMonitor mon = (OnmsLocationMonitor)getCurrentArguments()[0];
                assertEquals(MonitorStatus.DISCONNECTED, mon.getStatus());
                assertTrue(mon.getLastCheckInTime().before(new Date(now.getTime() - DISCONNECTED_TIMEOUT)));
                return null;
            }

        });

        replayMocks();

        m_backEnd.checkForDisconnectedMonitors();

        verifyMocks();
    }

    private void updateConfiguration() {
        expect(m_timeKeeper.getCurrentDate()).andReturn(new Date());
        replayMocks();

        m_backEnd.configurationUpdated();

        verifyMocks();
    }

    private void verifyMocks() {
        verify(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper, m_eventIpcManager);
        reset(m_locMonDao, m_monSvcDao, m_pollerConfig, m_scheduler, m_timeKeeper, m_eventIpcManager);
    }

    private void verifyPollerCheckingIn(MonitorStatus oldStatus, MonitorStatus newStatus, MonitorStatus result) {
        verifyPollerCheckingIn(oldStatus, newStatus, result, null);
    }


    private void verifyPollerCheckingIn(MonitorStatus oldStatus, MonitorStatus newStatus, MonitorStatus result, Event e) {
        m_locationMonitor.setStatus(oldStatus);
        expectLocationMonitorStatusChanged(oldStatus, newStatus);

        if (e != null) {
            anticipateEvent(e);
        }

        replayMocks();

        assertEquals("Unexpected result state", result, m_backEnd.pollerCheckingIn(1, m_startTime));

        verifyMocks();
    }


}

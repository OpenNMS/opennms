package org.opennms.netmgt.poller.remote;


import static org.easymock.EasyMock.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.remote.support.DefaultPollerFrontEnd;

public class PollerFrontEndTest extends TestCase {
	
    private List m_mocks = new ArrayList();
    
	private DefaultPollerFrontEnd m_frontEnd;
    private PollerBackEnd m_backEnd;
    private PollerSettings m_settings;
    private PollService m_pollService;

    private PropertyChangeListener m_registrationListener;
    private ServicePollStateChangedListener m_polledServiceListener;
    private ConfigurationChangedListener m_configChangeListener;
    
    private DemoPollerConfiguration m_pollerConfiguration;



	@Override
	protected void setUp() throws Exception {
		
		m_backEnd = createMock(PollerBackEnd.class);
        m_settings = createMock(PollerSettings.class);
        m_pollService = createMock(PollService.class);
        m_registrationListener = createMock(PropertyChangeListener.class);
        m_polledServiceListener = createMock(ServicePollStateChangedListener.class);
        m_configChangeListener = createMock(ConfigurationChangedListener.class);
        
        m_pollerConfiguration = new DemoPollerConfiguration();
		
		m_frontEnd = new DefaultPollerFrontEnd();
        m_frontEnd.setPollerBackEnd(m_backEnd);
        m_frontEnd.setPollerSettings(m_settings);
        m_frontEnd.setPollService(m_pollService);
		
	}
	
    public void testRegisterNewMonitor() throws Exception {
        
        // once in afterPropertiesSet
		expect(m_settings.getMonitorId()).andReturn(null);
        
        // once in isRegistered
        expect(m_settings.getMonitorId()).andReturn(null);
        
        // register a new monitor and save the id
        expect(m_backEnd.registerLocationMonitor("OAK")).andReturn(1);
        m_settings.setMonitorId(1);
        PropertyChangeEvent registrationEvent = new PropertyChangeEvent(m_frontEnd, "registered", false, true);
        m_registrationListener.propertyChange(eq(registrationEvent));
        
        // another call to isRegistered;
        expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(new DemoPollerConfiguration()).atLeastOnce();
        
        expect(m_backEnd.pollerStarting(1)).andReturn(true);
        
        replayMocks();
        
        m_frontEnd.afterPropertiesSet();
        
        m_frontEnd.addPropertyChangeListener(m_registrationListener);
        
        assertFalse(m_frontEnd.isRegistered());
        
        m_frontEnd.register("OAK");
        
        assertTrue(m_frontEnd.isRegistered());
        
        verifyMocks();
        
	}
    
    public void testAlreadyRegistered() throws Exception {
        
        // once in afterPropertiesSet
        expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
        
        // since the poller is registered we immediately request the pollerConfig
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(new DemoPollerConfiguration());
        
        // expect the monitor to start
        expect(m_backEnd.pollerStarting(1)).andReturn(true);
        
        replayMocks();
        
        m_frontEnd.afterPropertiesSet();
        
        assertTrue(m_frontEnd.isRegistered());
        
        verifyMocks();
    }
    
    public void testSetInitialPollTime() throws Exception {
        
        Date start = new Date(1200000000000L);
        
        expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
        expect(m_backEnd.pollerStarting(1)).andReturn(true);
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(m_pollerConfiguration);

        replayMocks();
        
        m_frontEnd.afterPropertiesSet();
        
        int polledServiceId = m_pollerConfiguration.getFirstId();
        
        m_frontEnd.setInitialPollTime(polledServiceId, start);
        
        assertEquals(start, m_frontEnd.getServicePollState(polledServiceId).getNextPollTime());
        
        verifyMocks();
    }
    
    
    public void testPoll() throws Exception {
        
        expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
        expect(m_backEnd.pollerStarting(1)).andReturn(true);
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(m_pollerConfiguration);
        
        PollStatus up = PollStatus.available(1234);
        expect(m_pollService.poll(m_pollerConfiguration.getFirstService())).andReturn(up);
        
        m_backEnd.reportResult(1, m_pollerConfiguration.getFirstId(), up);
        
        ServicePollStateChangedEvent e = new ServicePollStateChangedEvent(m_pollerConfiguration.getFirstService(), 0);
        m_polledServiceListener.pollStateChange(eq(e));
        expectLastCall().atLeastOnce();

        replayMocks();
        
        m_frontEnd.afterPropertiesSet();
        
        m_frontEnd.addServicePollStateChangedListener(m_polledServiceListener);
        
        m_frontEnd.pollService(m_pollerConfiguration.getFirstId());
        
        ServicePollState pollState = m_frontEnd.getServicePollState(m_pollerConfiguration.getFirstId());
        
        verifyMocks();

        assertEquals(PollStatus.SERVICE_AVAILABLE, pollState.getLastPoll().getStatusCode());
                
    }
    
    public void testConfigCheck() throws Exception {
        
        expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
        expect(m_backEnd.pollerStarting(1)).andReturn(true);
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(m_pollerConfiguration);
        
        expect(m_backEnd.pollerCheckingIn(1, m_pollerConfiguration.getConfigurationTimestamp())).andReturn(true);
        
        DemoPollerConfiguration newPollerConfiguration = new DemoPollerConfiguration();
        expect(m_backEnd.getPollerConfiguration(1)).andReturn(newPollerConfiguration);
        
        PropertyChangeEvent e = new PropertyChangeEvent(m_frontEnd, "configuration", m_pollerConfiguration.getConfigurationTimestamp(), newPollerConfiguration.getConfigurationTimestamp());
        m_configChangeListener.configurationChanged(eq(e));
        
        
        replayMocks();
        
        m_frontEnd.afterPropertiesSet();

        m_frontEnd.addConfigurationChangedListener(m_configChangeListener);
        
        m_frontEnd.checkConfig();

        verifyMocks();
}
    
    

    @SuppressWarnings("unchecked")
    private <T> T createMock(Class<T> name) {
        T mock = EasyMock.createMock(name);
        m_mocks.add(mock);
        return mock;
    }

    private void verifyMocks() {
        EasyMock.verify(m_mocks.toArray());
    }

    private void replayMocks() {
        EasyMock.replay(m_mocks.toArray());
    }
    
    public static class PropertyChangeEventEquals implements IArgumentMatcher {
        
        private PropertyChangeEvent m_expected;

        PropertyChangeEventEquals(PropertyChangeEvent value) {
            m_expected = value;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append(m_expected);
        }

        public boolean matches(Object argument) {
            PropertyChangeEvent actual = (PropertyChangeEvent)argument;
            if (m_expected == null) {
                return actual == null;
            }
            
            return (
                m_expected.getSource() == actual.getSource()
                && m_expected.getPropertyName().equals(actual.getPropertyName())
                && m_expected.getOldValue().equals(actual.getOldValue())
                && m_expected.getNewValue().equals(actual.getNewValue())
                );
        }
        
    }
    
    public static class PolledServiceChangeEventEquals implements IArgumentMatcher {
        
        private ServicePollStateChangedEvent m_expected;

        PolledServiceChangeEventEquals(ServicePollStateChangedEvent value) {
            m_expected = value;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append(m_expected);
        }

        public boolean matches(Object argument) {
            ServicePollStateChangedEvent actual = (ServicePollStateChangedEvent)argument;
            if (m_expected == null) {
                return actual == null;
            }
            
            return (
                m_expected.getSource() == actual.getSource()
                && m_expected.getIndex() == actual.getIndex()
                );
        }
        
    }
    

    private PropertyChangeEvent eq(PropertyChangeEvent e) {
        EasyMock.reportMatcher(new PropertyChangeEventEquals(e));
        return null;
        
    }

    
    private ServicePollStateChangedEvent eq(ServicePollStateChangedEvent e) {
        EasyMock.reportMatcher(new PolledServiceChangeEventEquals(e));
        return null;
        
    }

}

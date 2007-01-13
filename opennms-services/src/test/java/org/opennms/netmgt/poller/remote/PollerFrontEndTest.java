//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import static org.springframework.util.ObjectUtils.nullSafeEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.opennms.netmgt.config.DefaultServiceMonitorLocator;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.monitors.HttpMonitor;
import org.opennms.netmgt.poller.remote.support.DefaultPollerFrontEnd;

public class PollerFrontEndTest extends TestCase {

	public static class PolledServiceChangeEventEquals implements
			IArgumentMatcher {

		private ServicePollStateChangedEvent m_expected;

		PolledServiceChangeEventEquals(ServicePollStateChangedEvent value) {
			m_expected = value;
		}

		public void appendTo(StringBuffer buffer) {
			buffer.append(m_expected);
		}

		public boolean matches(Object argument) {
			ServicePollStateChangedEvent actual = (ServicePollStateChangedEvent) argument;
			if (m_expected == null) {
				return actual == null;
			}

			return (m_expected.getSource() == actual.getSource() && m_expected
					.getIndex() == actual.getIndex());
		}

	}

	public static class PropertyChangeEventEquals implements IArgumentMatcher {

		private PropertyChangeEvent m_expected;

		PropertyChangeEventEquals(PropertyChangeEvent value) {
			m_expected = value;
		}

		public void appendTo(StringBuffer buffer) {
			buffer.append(m_expected);
			buffer.append(" property=");
			buffer.append(m_expected.getPropertyName());
			buffer.append(", oldValue=");
			buffer.append(m_expected.getOldValue());
			buffer.append(", newValue=");
			buffer.append(m_expected.getNewValue());
			
		}

		public boolean matches(Object argument) {
			PropertyChangeEvent actual = (PropertyChangeEvent) argument;
			if (m_expected == actual) return true;
			
			if (m_expected == null) {
				return actual == null;
			}

			return (m_expected.getSource() == actual.getSource()
					&& m_expected.getPropertyName().equals(actual.getPropertyName())
					&& nullSafeEquals(m_expected.getOldValue(), actual.getOldValue()) 
					&& nullSafeEquals(m_expected.getNewValue(), actual.getNewValue()));
		}
		
		

	}

	private PollerBackEnd m_backEnd;

	private ConfigurationChangedListener m_configChangeListener;

	private DefaultPollerFrontEnd m_frontEnd;

	private List m_mocks = new ArrayList();

	private DemoPollerConfiguration m_oldPollerConfiguration;

	private ServicePollStateChangedListener m_polledServiceListener;
	private DemoPollerConfiguration m_pollerConfiguration;

	private PollService m_pollService;

	private Integer m_registeredId;

	private PropertyChangeListener m_registrationListener;

	private PollStatus m_serviceStatus;

	private PollerSettings m_settings;
	
	private boolean m_started;

	public void testAfterPropertiesSetWhenNotRegistered() throws Exception {
		testAfterPropertiesSetWithRegisteredId(null);
	}

	public void testAfterPropertiesSetWhenRegistered() throws Exception {
		testAfterPropertiesSetWithRegisteredId(1);
	}

	public void testAlreadyRegistered() throws Exception {

		setRegistered();
		
		anticipateAfterPropertiesSet();
		
		anticipateIsRegistered();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		assertTrue(m_frontEnd.isRegistered());

		verifyMocks();
	}

	public void testConfigCheck() throws Exception {
		
		setRegistered();
		
		anticipateAfterPropertiesSet();
		
		setPollConfig(new DemoPollerConfiguration());
		
		anticipateCheckConfig();
		
		replayMocks();

		m_frontEnd.afterPropertiesSet();

		m_frontEnd.checkConfig();

		verifyMocks();
	}
	
	public void testDetails() {
		Map<String, String> details = m_frontEnd.getDetails();
		assertPropertyEquals("os.name", details);
		assertPropertyEquals("os.version", details);
	}

	public void testIsRegistered() {

		// test in the unregistered state
		testIsRegistered(null, false);

		// test in the registered state
		testIsRegistered(1, true);
	}

	public void testNotYetRegistered() throws Exception {
		setRegisteredId(null);
		
		anticipateAfterPropertiesSet();
		
		anticipateIsRegistered();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		assertFalse(m_frontEnd.isRegistered());

		verifyMocks();
	}

	public void testPoll() throws Exception {
		
		setRegistered();
		
		anticipateAfterPropertiesSet();
		
		anticipatePollService();
		
		anticipateGetServicePollState();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		m_frontEnd.pollService(pollConfig().getFirstId());

		ServicePollState pollState = m_frontEnd
				.getServicePollState(pollConfig().getFirstId());

		verifyMocks();

		assertEquals(PollStatus.SERVICE_AVAILABLE, pollState.getLastPoll()
				.getStatusCode());

	}

	public void testRegisterNewMonitor() throws Exception {

		anticipateAfterPropertiesSet();

		anticipateIsRegistered();
		
		anticiapateRegister();
		
		anticipateIsRegistered();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		assertFalse(m_frontEnd.isRegistered());

		m_frontEnd.register("OAK");

		assertTrue(m_frontEnd.isRegistered());

		verifyMocks();

	}

	public void testSetInitialPollTime() throws Exception {

		Date start = new Date(1200000000000L);
		
		setRegistered();
		
		anticipateAfterPropertiesSet();

		int polledServiceId = pollConfig().getFirstId();

		anticipateSetInitialPollTime();

		anticipateGetServicePollState();

//		expect(m_settings.getMonitorId()).andReturn(1).atLeastOnce();
//
//		anticipateNewConfig(pollConfig());
//
//		expect(m_backEnd.pollerStarting(1, getPollerDetails())).andReturn(true);

		replayMocks();

		m_frontEnd.afterPropertiesSet();


		m_frontEnd.setInitialPollTime(polledServiceId, start);

		assertEquals(start, m_frontEnd.getServicePollState(polledServiceId)
				.getNextPollTime());

		verifyMocks();
	}

	public void testStop() throws Exception {
		
		setRegistered();

		anticipateAfterPropertiesSet();
		
		anticipateStop();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		assertTrue(m_frontEnd.isStarted());

		m_frontEnd.stop();

		assertFalse(m_frontEnd.isStarted());

		verifyMocks();
	}

	@Override
	protected void setUp() throws Exception {

		m_backEnd = createMock(PollerBackEnd.class);
		m_settings = createMock(PollerSettings.class);
		m_pollService = createMock(PollService.class);
		m_registrationListener = createMock(PropertyChangeListener.class);
		m_polledServiceListener = createMock(ServicePollStateChangedListener.class);
		m_configChangeListener = createMock(ConfigurationChangedListener.class);

		setPollConfig(new DemoPollerConfiguration());
		m_oldPollerConfiguration = null;

		m_frontEnd = new DefaultPollerFrontEnd();
		m_frontEnd.setPollerBackEnd(m_backEnd);
		m_frontEnd.setPollerSettings(m_settings);
		m_frontEnd.setPollService(m_pollService);
		
		m_frontEnd.addConfigurationChangedListener(m_configChangeListener);
		m_frontEnd.addPropertyChangeListener(m_registrationListener);
		m_frontEnd.addServicePollStateChangedListener(m_polledServiceListener);
		
		m_serviceStatus = PollStatus.available(1234L);

	}

	private void anticiapateRegister() {

		anticipateRegisterLocationMonitor();
		
		anticipateInitializePollState();
		
		anticipateFireRegistered();

	}

	private void anticipateAfterPropertiesSet() {
		anticipateIsRegistered();

		if (getRegisteredId() == null)
			return;

		anticipateInitializePollState();

	}

	private void anticipateAssertConfigured() {
		anticipateAssertRegistered();
	}

	private void anticipateAssertRegistered() {
		anticipateIsRegistered();
	}

	private void anticipateCheckConfig() {
		anticipateIsRegistered();
		anticipateAssertConfigured();
		
		anticipateGetMonitorId();
		
		anticipatePollerCheckingIn();
		
		anticipateInitializePollState();
	}

	private void anticipateDoPoll() {
		anticipateAssertRegistered();
		anticipateGetPolledService();
		
		expect(m_pollService.poll(pollConfig().getFirstService())).andReturn(m_serviceStatus);
		
	}

	private void anticipateFireConfigurationChangeEvent() {
		PropertyChangeEvent e = new PropertyChangeEvent(m_frontEnd,
				"configuration", 
				(oldConfig() == null ? null :oldConfig().getConfigurationTimestamp()), 
				(pollConfig() == null ? null : pollConfig().getConfigurationTimestamp()));
		m_configChangeListener.configurationChanged(eq(e));
	}

	private void anticipateFirePropertyChangeEvent(String property, Object oldValue, Object newValue) {
		PropertyChangeEvent e= new PropertyChangeEvent(m_frontEnd, property, oldValue, newValue);
		m_registrationListener.propertyChange(eq(e));
	}

	private void anticipateFireRegistered() {
		anticipateFirePropertyChangeEvent("registered", false, true);
	}

	private void anticipateFireServicePollStateChanged() {
		ServicePollStateChangedEvent e = new ServicePollStateChangedEvent(pollConfig().getFirstService(), 0);
		m_polledServiceListener.pollStateChange(eq(e));
	}
	
	private void anticipateGetConfiguration() {
		expect(m_backEnd.getPollerConfiguration(1)).andReturn(pollConfig());
	}
	
	private void anticipateGetMonitorId() {
		expect(m_settings.getMonitorId()).andReturn(getRegisteredId());
	}
	
	private void anticipateGetPolledService() {
		anticipateAssertRegistered();
		anticipateGetServicePollState();
	}

	private void anticipateGetServicePollState() {
		anticipateAssertRegistered();
	}

	private void anticipateInitializePollState() {
		anticipateStart();

		anticipatePollServiceSetMonitorLocators();

		anticipateGetMonitorId();

		anticipateGetConfiguration();
		
		anticipateFireConfigurationChangeEvent();
		
		anticipatePolledServicesInitialized();
	}

	private void anticipateIsRegistered() {
		anticipateGetMonitorId();
	}

	private void anticipateNewConfig(DemoPollerConfiguration pollConfig) {
		anticipatePollServiceSetMonitorLocators();

		m_pollService.initialize(isA(PolledService.class));
		expectLastCall().times(pollConfig.getPolledServices().length);

		expect(m_backEnd.getPollerConfiguration(1)).andReturn(pollConfig);
	}

	private void anticipatePolledServicesInitialized() {
		m_pollService.initialize(isA(PolledService.class));
		expectLastCall().times(pollConfig().getPolledServices().length);
	}

	private void anticipatePollerCheckingIn() {
		expect(m_backEnd.pollerCheckingIn(1, oldConfig().getConfigurationTimestamp())).andReturn(MonitorStatus.CONFIG_CHANGED);
	}

	private void anticipatePollerStopping() {
		m_backEnd.pollerStopping(getRegisteredId());
	}

	private void anticipatePollService() {
		anticipateAssertRegistered();
		
		anticipateDoPoll();
		
		anticipateUpdateServicePollState();
		
		anticipateGetMonitorId();
		
		anticipateReportResult();
		
	}

	private void anticipatePollServiceSetMonitorLocators() {
		ServiceMonitorLocator locator = new DefaultServiceMonitorLocator(
				"HTTP", HttpMonitor.class);
		Set<ServiceMonitorLocator> locators = Collections.singleton(locator);
		expect(
				m_backEnd
						.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR))
				.andReturn(locators);
		m_pollService.setServiceMonitorLocators(locators);
	}

	private void anticipateRegisterLocationMonitor() {
		setRegistered();
		expect(m_backEnd.registerLocationMonitor("OAK")).andReturn(getRegisteredId());
		m_settings.setMonitorId(getRegisteredId());
	}

	private void anticipateReportResult() {
		m_backEnd.reportResult(getRegisteredId(), pollConfig().getFirstId(), m_serviceStatus);
	}


	private void anticipateSetInitialPollTime() {
		anticipateAssertRegistered();
		anticipateGetServicePollState();
		anticipateFireServicePollStateChanged();
	}

	private void anticipateStart() {
		if (m_started) return;
		
		anticipateIsRegistered();

		if (getRegisteredId() == null) {
			return;
		}

		anticipateGetMonitorId();

		expect(m_backEnd.pollerStarting(getRegisteredId(), getPollerDetails()))
				.andReturn(true);
		
		m_started = true;

	}

	private void anticipateStop() {
		anticipateGetMonitorId();
		anticipatePollerStopping();
	}

	private void anticipateUpdateServicePollState() {
		anticipateAssertRegistered();
		anticipateGetServicePollState();
		anticipateFireServicePollStateChanged();
	}

	private void assertPropertyEquals(String propertyName,
			Map<String, String> details) {
		assertNotNull("has " + propertyName, details.get(propertyName));
		assertEquals(propertyName, System.getProperty(propertyName), details
				.get(propertyName));
	}

	@SuppressWarnings("unchecked")
	private <T> T createMock(Class<T> name) {
		T mock = EasyMock.createMock(name);
		m_mocks.add(mock);
		return mock;
	}

	private PropertyChangeEvent eq(PropertyChangeEvent e) {
		EasyMock.reportMatcher(new PropertyChangeEventEquals(e));
		return null;

	}

	private ServicePollStateChangedEvent eq(ServicePollStateChangedEvent e) {
		EasyMock.reportMatcher(new PolledServiceChangeEventEquals(e));
		return null;

	}

	private Map<String, String> getPollerDetails() {
		/*
		 * Map<String, String> pollerDetails = new HashMap<String, String>();
		 * pollerDetails.put("os.name", System.getProperty("os.name")); return
		 * pollerDetails;
		 */
		return m_frontEnd.getDetails();
	}

	private Integer getRegisteredId() {
		return m_registeredId;
	}

	private DemoPollerConfiguration oldConfig() {
		return m_oldPollerConfiguration;
	}

	private DemoPollerConfiguration pollConfig() {
		return m_pollerConfiguration;
	}
	
	private void replayMocks() {
		EasyMock.replay(m_mocks.toArray());
	}
	
	private void setPollConfig(DemoPollerConfiguration pollerConfiguration) {
		m_oldPollerConfiguration = pollConfig();
		m_pollerConfiguration = pollerConfiguration;
	}

	private void setRegistered() {
		setRegisteredId(1);
	}

	private void setRegisteredId(Integer registeredId) {
		m_registeredId = registeredId;
	}

	private void testAfterPropertiesSetWithRegisteredId(Integer registeredId) throws Exception {
		setRegisteredId(registeredId);

		anticipateAfterPropertiesSet();

		replayMocks();

		m_frontEnd.afterPropertiesSet();

		verifyMocks();
	}

	private void testIsRegistered(Integer monitorId, boolean expectedIsRegistered) {
		setRegisteredId(monitorId);

		anticipateIsRegistered();

		replayMocks();

		assertEquals("Unexpected value for isRegistered", 
				expectedIsRegistered,
				m_frontEnd.isRegistered());

		verifyMocks();
	}

	private void verifyMocks() {
		EasyMock.verify(m_mocks.toArray());
		EasyMock.reset(m_mocks.toArray());
	}

}

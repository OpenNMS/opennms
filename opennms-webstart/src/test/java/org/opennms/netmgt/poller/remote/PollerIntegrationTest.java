package org.opennms.netmgt.poller.remote;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class PollerIntegrationTest extends
		AbstractDependencyInjectionSpringContextTests {
	
	private Poller m_poller;

	protected String[] getConfigLocations() {
		return new String[] { "classpath:/META-INF/opennms/applicationContext-ws-svclayer.xml" };
	}
	
	public void setPoller(Poller poller) {
		m_poller = poller;
	}
	
	public void testPoller() throws Exception {
		
		BeanInfo info = Introspector.getBeanInfo(DefaultPolledServicesModel.class);
		EventSetDescriptor[] eventSetDescriptors = info.getEventSetDescriptors();
		System.err.println("Num esd's = "+eventSetDescriptors.length);
		
		for (int i = 0; i < eventSetDescriptors.length; i++) {
			EventSetDescriptor esd = eventSetDescriptors[i];
			System.err.println("esd = "+esd);
		}
		Thread.sleep(10000);
	}

}

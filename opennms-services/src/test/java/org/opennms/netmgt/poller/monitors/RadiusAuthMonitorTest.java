package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	OpenNMSConfigurationExecutionListener.class,
	TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
public class RadiusAuthMonitorTest {

	@Before
	public void setup() throws Exception {
	    MockLogAppender.setupLogging();
	}

	@Test
	@Ignore("have to have a radius server set up")
	public void testResponse() throws Exception {
		final Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

		final ServiceMonitor monitor = new RadiusAuthMonitor();
		final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "192.168.211.11", "RADIUS", false);

        m.put("user", "testing");
        m.put("password", "password");
        m.put("retry", "1");
        m.put("secret", "testing123");
        m.put("authtype", "chap");

        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
	}

}

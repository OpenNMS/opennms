package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DiskUsageMonitorTest {

    private static final String TEST_HOST_ADDRESS = "10.10.10.11";

    private final DiskUsageMonitor diskUsageMonitor = new DiskUsageMonitor();

    @Test
    @JUnitSnmpAgent(host=TEST_HOST_ADDRESS, resource="/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void canPollSuccesfully() throws UnknownHostException {
        final MonitoredService svc = new MockMonitoredService(1, "node1", InetAddress.getByName(TEST_HOST_ADDRESS), "dsk");
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("disk", "/");
        assertTrue(diskUsageMonitor.poll(svc, parameters).isAvailable());
    }

    @Test
    @JUnitSnmpAgent(host=TEST_HOST_ADDRESS, resource="/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void failsIfNoDisksAreMatched() throws UnknownHostException {
        final MonitoredService svc = new MockMonitoredService(1, "node1", InetAddress.getByName(TEST_HOST_ADDRESS), "dsk");
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("disk", "/should/not/match");
        assertTrue(diskUsageMonitor.poll(svc, parameters).isUnavailable());
    }
}

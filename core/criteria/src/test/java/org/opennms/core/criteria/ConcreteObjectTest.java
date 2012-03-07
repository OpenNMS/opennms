package org.opennms.core.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria.FetchType;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;

public class ConcreteObjectTest {

	@Before
	public void setUp() {
		LogUtils.logToConsole();
		LogUtils.enableDebugging();
	}
	
	@Test
	public void testTypes() throws Exception {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
		builder.distinct();
        final Criteria crit = builder.toCriteria();

		assertEquals(OnmsNode.class, crit.getType("node"));
		assertEquals(Integer.class, crit.getType("node.id"));
		assertEquals(String.class, crit.getType("node.label"));
		assertNull(crit.getType("monkey"));
		assertNull(crit.getType("node.label.foo"));
		assertEquals(OnmsIpInterface.class, crit.getType("node.ipInterfaces"));
		assertEquals(Map.class, crit.getType("details"));
	}
	
	@Test
	public void testAliases() throws Exception {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
		builder.distinct();

		builder.fetch("firstEvent", FetchType.EAGER);
        builder.fetch("lastEvent", FetchType.EAGER);
        
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.monitoredServices", "monitoredService", JoinType.LEFT_JOIN);
        builder.alias("monitoredService.currentOutages", "currentOutage", JoinType.LEFT_JOIN);
        builder.alias("currentOutage.monitoredService", "service", JoinType.LEFT_JOIN);

        final Criteria crit = builder.toCriteria();

		assertEquals(OnmsIpInterface.class, crit.getType("ipInterface"));
		assertEquals(OnmsMonitoredService.class, crit.getType("monitoredService"));
		assertEquals(OnmsOutage.class, crit.getType("currentOutage"));
		assertEquals(OnmsMonitoredService.class, crit.getType("service"));
	}
	
	@Test
	public void testNode() throws Exception {
	    final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
	    builder.distinct();
	    
        builder.alias("snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
	    builder.alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
	    
	    final Criteria crit = builder.toCriteria();
	    
	    assertEquals(InetAddress.class, crit.getType("ipInterface.ipAddress"));
	}
}

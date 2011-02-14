package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class IPhoneRestServiceTest extends AbstractSpringJerseyRestTestCase {
	protected void afterServletStart() {
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		DatabasePopulator dbp = context.getBean("databasePopulator", DatabasePopulator.class);
		dbp.populateDatabase();
	}

	@Test
	public void testAlarms() throws Exception {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("orderBy", "lastEventTime");
		parameters.put("order", "desc");
		parameters.put("alarmAckUser", "null");
		parameters.put("limit", "1");
		String xml = sendRequest(GET, "/alarms", parameters, 200);
		assertTrue(xml.contains("This is a test alarm"));

		xml = sendRequest(GET, "/alarms/1", parameters, 200);
		assertTrue(xml.contains("This is a test alarm"));
	}
	
	@Test
	public void testEvents() throws Exception {
		Map<String, String> parameters = new HashMap<String, String>();
		String xml = sendRequest(GET, "/events", parameters, 200);
		assertTrue(xml.contains("uei.opennms.org/test"));

		parameters.put("orderBy", "lastEventTime");
		parameters.put("order", "desc");
		parameters.put("limit", "1");
		xml = sendRequest(GET, "/events/1", parameters, 200);
		assertTrue(xml.contains("uei.opennms.org/test"));
	}
	
	@Test
	public void testNodes() throws Exception {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("comparator", "ilike");
		parameters.put("match", "any");
		parameters.put("label", "1%");
		parameters.put("ipInterface.ipAddress", "1%");
		parameters.put("ipInterface.ipHostName", "1%");
		String xml = sendRequest(GET, "/nodes", parameters, 200);
		assertTrue(xml.contains("<node id=\"1\""));
		assertTrue(xml.contains("label=\"node1\""));

		parameters.clear();
		parameters.put("comparator", "ilike");
		parameters.put("match", "any");
		parameters.put("label", "8%");
		parameters.put("ipInterface.ipAddress", "8%");
		parameters.put("ipInterface.ipHostName", "8%");
		xml = sendRequest(GET, "/nodes", parameters, 200);
		assertTrue(xml.contains("totalCount=\"0\""));
		
		parameters.clear();
		parameters.put("limit", "50");
		parameters.put("orderBy", "ifLostService");
		parameters.put("order", "desc");
		xml = sendRequest(GET, "/outages/forNode/1", parameters, 200);
		assertTrue(xml.contains("SNMP"));

		parameters.clear();
		parameters.put("orderBy", new String[] {"ipHostName", "ipAddress"});
		xml = sendRequest(GET, "/nodes/1/ipinterfaces", parameters, 200);
		assertTrue(xml.contains("192.168.1.1"));
		
		parameters.clear();
		parameters.put("orderBy", new String[] {"ifName", "ipAddress", "ifDesc"});
		xml = sendRequest(GET, "/nodes/1/snmpinterfaces", parameters, 200);
		assertTrue(xml.contains("192.168.1.1"));
		
		parameters.clear();
		parameters.put("limit", "50");
		parameters.put("orderBy", "lastEventTime");
		parameters.put("node.id", "1");
		xml = sendRequest(GET, "/events", parameters, 200);
		assertTrue(xml.contains("totalCount=\"0\""));
	}

	@Test
	public void testEventsForNodes() throws Exception {
		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put("limit", "50");
		parameters.put("node.id", "1");
		String xml = sendRequest(GET, "/events", parameters, 200);
		assertTrue(xml.contains("totalCount=\"0\""));
	}
	
	@Test
	public void testOutages() throws Exception {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("orderBy", "ifLostService");
		parameters.put("order", "desc");
		parameters.put("ifRegainedService", "null");
		String xml = sendRequest(GET, "/outages", parameters, 200);
		assertTrue(xml.contains("count=\"1\""));
		assertTrue(xml.contains("id=\"2\""));
		assertTrue(xml.contains("192.168.1.1"));
	}
}

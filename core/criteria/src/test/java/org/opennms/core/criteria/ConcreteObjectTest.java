package org.opennms.core.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class ConcreteObjectTest {

	public static class PropertyPathTypeMap {

	}

	@Test
	public void testPropertyPathTypeMap() throws Exception {
		Criteria crit = new Criteria(OnmsAlarm.class);

		assertEquals(OnmsNode.class, crit.getType("node"));
		assertEquals(Integer.class, crit.getType("node.id"));
		assertEquals(String.class, crit.getType("node.label"));
		assertNull(crit.getType("monkey"));
		assertNull(crit.getType("node.label.foo"));
		assertEquals(OnmsIpInterface.class, crit.getType("node.ipInterfaces"));
	}
}

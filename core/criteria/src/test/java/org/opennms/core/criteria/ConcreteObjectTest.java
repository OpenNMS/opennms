/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;

public class ConcreteObjectTest {

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

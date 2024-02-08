/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

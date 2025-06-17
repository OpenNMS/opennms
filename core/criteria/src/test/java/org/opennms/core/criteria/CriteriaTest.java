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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

public class CriteriaTest {

	private static final Order[] EMPTY_ORDER_ARRAY = new Order[0];

	@Test
	public void testBuilder() {
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		assertNotNull(cb);
		assertNotNull(cb.toCriteria());
	}
	
	@Test
	public void testOrder() {
		final List<Order> orders = new ArrayList<>();

		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

		// ascending
		cb.orderBy("firstEventTime");
		orders.add(Order.asc("firstEventTime"));
		assertArrayEquals(orders.toArray(EMPTY_ORDER_ARRAY), cb.toCriteria().getOrders().toArray(EMPTY_ORDER_ARRAY));

		// descending
		cb.clearOrder();
		orders.clear();
		cb.orderBy("firstEventTime").desc();
		orders.add(Order.desc("firstEventTime"));
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());

		// multiple unrelated attributes, using .desc()
		cb.clearOrder();
		orders.clear();
		cb.orderBy("firstEventTime").desc();
		cb.orderBy("id").desc();
		orders.add(Order.desc("firstEventTime"));
		orders.add(Order.desc("id"));
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());

		assertEquals(Order.asc("firstEventTime"), Order.desc("firstEventTime"));
		cb.clearOrder();
		orders.clear();
		cb.orderBy("firstEventTime").desc();
		cb.orderBy("id").desc();
		orders.add(Order.desc("firstEventTime"));
		orders.add(Order.desc("id"));
		// this should be ignored, we already have an orderBy=firstEventTime
		cb.orderBy("firstEventTime").asc();
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());

		// should be ignored still
		cb.orderBy("id").asc();
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());
		
		cb.clearOrder().orderBy("id").asc();
		orders.clear();
		orders.add(Order.asc("id"));
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());
	}

	@Test
	public void testFetch() {
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

		cb.fetch("firstEvent").fetch("lastEvent").fetch("distPoller", FetchType.LAZY);
		final Iterator<Fetch> i = cb.toCriteria().getFetchTypes().iterator();
		assertEquals(FetchType.DEFAULT, i.next().getFetchType());
		i.next();
		assertEquals(FetchType.LAZY, i.next().getFetchType());
	}
	
	@Test
	public void testAlias() {
		CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		
		cb.alias("node", "node").join("node.snmpInterfaces", "snmpInterface").join("node.ipInterfaces", "ipInterface");
		assertEquals(JoinType.LEFT_JOIN, cb.toCriteria().getAliases().iterator().next().getType());
		assertEquals(3, cb.toCriteria().getAliases().size());

		cb = new CriteriaBuilder(OnmsAlarm.class).join("monkey", "ook", JoinType.FULL_JOIN);
		assertEquals("monkey", cb.toCriteria().getAliases().iterator().next().getAssociationPath());
		assertEquals("ook", cb.toCriteria().getAliases().iterator().next().getAlias());
		assertEquals(JoinType.FULL_JOIN, cb.toCriteria().getAliases().iterator().next().getType());
	}
	
	@Test
	public void testDistinct() {
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		
		cb.alias("node", "node");
		assertFalse(cb.toCriteria().isDistinct());
		
		cb.distinct();
		assertTrue(cb.toCriteria().isDistinct());
		
		cb.distinct(false);
		assertFalse(cb.toCriteria().isDistinct());
	}
	
	@Test
	public void testLimits() {
		CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.limit(10).offset(20);
		assertEquals(Integer.valueOf(10), cb.toCriteria().getLimit());
		assertEquals(Integer.valueOf(20), cb.toCriteria().getOffset());
	}

	@Test
	public void testRestrictions() {
		CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		
		final List<Restriction> expected = new ArrayList<>();
		expected.add(Restrictions.isNull("tticketId"));
		expected.add(Restrictions.isNotNull("severity"));
		cb.isNull("tticketId").isNotNull("severity");
		assertEquals(expected, cb.toCriteria().getRestrictions());

		final Date d = new Date();
		cb = new CriteriaBuilder(OnmsAlarm.class);
		cb.id(1).and(Restrictions.gt("firstEventTime", d), Restrictions.lt("severity", OnmsSeverity.CRITICAL));
		expected.clear();
		expected.add(Restrictions.eq("id", 1));
		expected.add(Restrictions.and(Restrictions.gt("firstEventTime", d), Restrictions.lt("severity", OnmsSeverity.CRITICAL)));
		assertEquals(expected, cb.toCriteria().getRestrictions());
		
		cb.like("description", "*foo*").ilike("uei", "*bar*");
		expected.add(Restrictions.like("description", "*foo*"));
		expected.add(Restrictions.ilike("uei", "*bar*"));
		assertEquals(expected, cb.toCriteria().getRestrictions());
		
		final List<String> inValues = new ArrayList<>();
		inValues.add("a");
		inValues.add("b");
		cb.in("nodeLabel", inValues);
		expected.add(Restrictions.in("nodeLabel", inValues));
		final List<String> notInValues = new ArrayList<>();
		notInValues.add("c");
		notInValues.add("d");
		cb.not().in("nodeLabel", notInValues);
		expected.add(Restrictions.not(Restrictions.in("nodeLabel", notInValues)));
		assertEquals(expected, cb.toCriteria().getRestrictions());
		
		cb = new CriteriaBuilder(OnmsAlarm.class);
		expected.clear();
		cb.between("id", 1, 10);
		expected.add(Restrictions.between("id", 1, 10));
		cb.ne("id", 8);
		expected.add(Restrictions.not(Restrictions.eq("id", 8)));
		assertEquals(expected, cb.toCriteria().getRestrictions());
		
		cb = new CriteriaBuilder(OnmsAlarm.class);
                cb.id(1).and(Restrictions.gt("firstEventTime", d), Restrictions.lt("severity", OnmsSeverity.CRITICAL));
                expected.clear();
                expected.add(Restrictions.eq("id", 1));
                expected.add(Restrictions.and(Restrictions.gt("firstEventTime", d), Restrictions.lt("severity", OnmsSeverity.CRITICAL)));
                cb.like("description", "*foo*").ilike("uei", "*bar*");
                expected.add(Restrictions.like("description", "*foo*"));
                expected.add(Restrictions.ilike("uei", "*bar*"));
		inValues.clear();
                cb.in("nodeLabel", inValues);
                expected.add(Restrictions.in("nodeLabel", inValues));
                assertEquals(expected, cb.toCriteria().getRestrictions());
	}
}

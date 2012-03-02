package org.opennms.core.criteria;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.core.criteria.Criteria.FetchType;
import org.opennms.core.criteria.Alias.JoinType;
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
		final List<Order> orders = new ArrayList<Order>();

		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

		cb.orderBy("firstEventTime");
		orders.add(Order.asc("firstEventTime"));
		assertArrayEquals(orders.toArray(EMPTY_ORDER_ARRAY), cb.toCriteria().getOrders().toArray(EMPTY_ORDER_ARRAY));

		cb.desc();
		orders.clear();
		orders.add(Order.desc("firstEventTime"));
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());

		cb.orderBy("id").desc();
		orders.add(Order.desc("id"));
		assertArrayEquals(orders.toArray(), cb.toCriteria().getOrders().toArray());

		// should be ignored, adding "id" should have already frozen firstEventTime
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
		assertEquals(FetchType.DEFAULT, cb.toCriteria().getFetchTypes().get("firstEvent"));
		assertEquals(FetchType.LAZY, cb.toCriteria().getFetchTypes().get("distPoller"));
	}
	
	@Test
	public void testAlias() {
		CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
		
		cb.alias("node", "node").join("node.snmpInterfaces", "snmpInterface").join("node.ipInterfaces", "ipInterface");
		assertEquals(JoinType.LEFT_JOIN, cb.toCriteria().getAliases().get(0).getType());
		assertEquals(3, cb.toCriteria().getAliases().size());

		cb = new CriteriaBuilder(OnmsAlarm.class).join("monkey", "ook", JoinType.FULL_JOIN);
		assertEquals("monkey", cb.toCriteria().getAliases().get(0).getAssociationPath());
		assertEquals("ook", cb.toCriteria().getAliases().get(0).getAlias());
		assertEquals(JoinType.FULL_JOIN, cb.toCriteria().getAliases().get(0).getType());
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
		
		final Set<Restriction> expected = new LinkedHashSet<Restriction>();
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
		
		final List<String> inValues = new ArrayList<String>();
		inValues.add("a");
		inValues.add("b");
		cb.in("nodeLabel", inValues);
		expected.add(Restrictions.in("nodeLabel", inValues));
		final List<String> notInValues = new ArrayList<String>();
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
	}
}

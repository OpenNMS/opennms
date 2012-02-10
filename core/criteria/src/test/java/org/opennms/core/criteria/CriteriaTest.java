package org.opennms.core.criteria;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.core.criteria.Criteria.FetchType;
import org.opennms.netmgt.model.OnmsAlarm;

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
	public void testJoins() {
		final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

		cb.fetch("firstEvent").fetch("lastEvent").fetch("distPoller", FetchType.LAZY);
		assertEquals(FetchType.DEFAULT, cb.toCriteria().getFetchTypes().get("firstEvent"));
		assertEquals(FetchType.LAZY, cb.toCriteria().getFetchTypes().get("distPoller"));
	}
}

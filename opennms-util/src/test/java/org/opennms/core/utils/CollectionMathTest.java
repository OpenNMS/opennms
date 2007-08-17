package org.opennms.core.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

public class CollectionMathTest extends TestCase {
	
	private Collection<BigDecimal> getTestCollection() {
		Collection<BigDecimal> c = new ArrayList<BigDecimal>();
		c.add(null);
		c.add(new BigDecimal(1));
		c.add(new BigDecimal(100));
		c.add(new BigDecimal(16));
		c.add(null);
		return c;
	}

	public void testEmpty() {
		Collection<BigDecimal> c = new ArrayList<BigDecimal>();
		assertNull(CollectionMath.percentNotNull(c));
		assertNull(CollectionMath.percentNull(c));
	}
	
	public void testPercents() {
		Collection<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(60).doubleValue(), CollectionMath.percentNotNull(c).doubleValue());
		assertEquals(new BigDecimal(40).doubleValue(), CollectionMath.percentNull(c).doubleValue());
	}

	public void testCounts() {
		Collection<BigDecimal> c = getTestCollection();
		assertEquals(3, CollectionMath.countNotNull(c));
		assertEquals(2, CollectionMath.countNull(c));
	}
	
	public void testAverage() {
		Collection<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(39), CollectionMath.average(c));
	}
	
	public void testMedian() {
		Collection<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(16), CollectionMath.median(c));
		c.add(new BigDecimal(22));
		assertEquals(new BigDecimal(19), CollectionMath.median(c));
	}
}

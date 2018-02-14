/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class CollectionMathTest extends TestCase {
	
	private List<BigDecimal> getTestCollection() {
		List<BigDecimal> c = new ArrayList<>();
		c.add(null);
		c.add(new BigDecimal(1));
		c.add(new BigDecimal(100));
		c.add(new BigDecimal(16));
		c.add(null);
		return c;
	}

	public void testEmpty() {
		List<BigDecimal> c = new ArrayList<>();
		assertNull(CollectionMath.percentNotNull(c));
		assertNull(CollectionMath.percentNull(c));
	}
	
	public void testPercentNotNull() {
		List<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(60).doubleValue(), CollectionMath.percentNotNull(c).doubleValue());
	}
	public void testPercentNull() {
        List<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(40).doubleValue(), CollectionMath.percentNull(c).doubleValue());
	}

	public void testCountNotNull() {
		List<BigDecimal> c = getTestCollection();
		assertEquals(3, CollectionMath.countNotNull(c));
	}
	
	public void testCountNull() {
        List<BigDecimal> c = getTestCollection();
		assertEquals(2, CollectionMath.countNull(c));
	}
	
	public void testAverage() {
		List<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(39).doubleValue(), CollectionMath.average(c).doubleValue());
	}
	
	public void testMedian() {
		List<BigDecimal> c = getTestCollection();
		assertEquals(new BigDecimal(16).doubleValue(), CollectionMath.median(c).doubleValue());
		c.add(new BigDecimal(22));
		assertEquals(new BigDecimal(19).doubleValue(), CollectionMath.median(c).doubleValue());
	}
}

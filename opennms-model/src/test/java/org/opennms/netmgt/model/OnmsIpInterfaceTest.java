/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import junit.framework.TestCase;


public class OnmsIpInterfaceTest extends TestCase {

	public void testCollectionTypeGetNull () {
		PrimaryType collectionType = PrimaryType.get(null);
		
		assertSame("The expected value is N for a null", PrimaryType.NOT_ELIGIBLE, collectionType);
		
	}

	public void testCollectionTypeGetSpaces () {
		PrimaryType collectionType = PrimaryType.get("   ");
		
		assertSame("The expected valus is N for all spaces", PrimaryType.NOT_ELIGIBLE, collectionType);
	}
	
	public void testCollectionTypeGetTwoChars () {
		
		try {
			@SuppressWarnings("unused")
			PrimaryType collectionType = PrimaryType.get(" MN  ");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testCollectionTypeGetZ () {
		
		try {
			@SuppressWarnings("unused")
			PrimaryType collectionType = PrimaryType.get("Z");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testCollectionTypeComparison () {
		PrimaryType left = PrimaryType.NOT_ELIGIBLE;
		PrimaryType right = null;
		try {
			left.isLessThan(right);
			fail("Expected to catch an exception here");
		} catch (NullPointerException e) {
		}
	}
}

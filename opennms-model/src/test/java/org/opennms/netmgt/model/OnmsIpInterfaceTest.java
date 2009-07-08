/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.model;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;

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
		PrimaryType left = new PrimaryType('N');
		PrimaryType right = null;
		try {
			left.isLessThan(right);
			fail("Expected to catch an exception here");
		} catch (NullPointerException e) {
		}
	}
}

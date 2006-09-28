package org.opennms.netmgt.model;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

public class OnmsIpInterfaceTest extends TestCase {

	public void testCollectionTypeGetNull () {
		CollectionType collectionType = CollectionType.get(null);
		
		assertSame("The expected value is N for a null", CollectionType.NO_COLLECT, collectionType);
		
	}

	public void testCollectionTypeGetSpaces () {
		CollectionType collectionType = CollectionType.get("   ");
		
		assertSame("The expected valus is N for all spaces", CollectionType.NO_COLLECT, collectionType);
	}
	
	public void testCollectionTypeGetTwoChars () {
		
		try {
			@SuppressWarnings("unused")
			CollectionType collectionType = CollectionType.get(" MN  ");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testCollectionTypeGetZ () {
		
		try {
			@SuppressWarnings("unused")
			CollectionType collectionType = CollectionType.get("Z");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testCollectionTypeComparison () {
		CollectionType left = new CollectionType('N');
		CollectionType right = null;
		try {
			left.isLessThan(right);
			fail("Expected to catch an exception here");
		} catch (NullPointerException e) {
		}
	}
}

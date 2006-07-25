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
			CollectionType collectionType = CollectionType.get(" MN  ");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}

}

package org.opennms.features.topology.netutils.internal;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PingWindowTest {

	private PingWindow pingWindow;
	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		pingWindow = new PingWindow(testNode1, "");
	}
	
	@Test
	public void testBuildURL_upperBounds() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("10000");
		try {
			pingWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_lowerBounds() {
		pingWindow.packetSizeDropdown.setValue("16");
		pingWindow.requestsField.setValue("0");
		pingWindow.timeoutField.setValue("0");
		try {
			pingWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_nonIntegerInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("abcd");
		pingWindow.timeoutField.setValue("abcd");
		try {
			pingWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_negativeIntegers() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("-99");
		pingWindow.timeoutField.setValue("-1024");
		try {
			pingWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}

}

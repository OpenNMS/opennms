package org.opennms.features.topology.netutils.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TracerouteWindowTest {

	private TracerouteWindow traceWindow;
	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		traceWindow = new TracerouteWindow(testNode1, "");
	}

	@Test
	public void testBuildURL_upperBounds() {
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("256.256.256.256");
		try {
			traceWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_lowerBounds() {
		traceWindow.forcedHopField.setValue("-1.-1.-1.-1");
		try {
			traceWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_nonIntegerInput() {
		traceWindow.forcedHopField.setValue("abc.def.ghi.jkl");
		try {
			traceWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_notIPAddress() {
		traceWindow.forcedHopField.setValue("not an IP");
		try {
			traceWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
	
	@Test
	public void testBuildURL_negativeIntegers() {
		traceWindow.forcedHopField.setValue("-255.-255.-255.-255");
		try {
			traceWindow.buildURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(true); //Should throw an exception if it validated the input correctly
		}
	}
}

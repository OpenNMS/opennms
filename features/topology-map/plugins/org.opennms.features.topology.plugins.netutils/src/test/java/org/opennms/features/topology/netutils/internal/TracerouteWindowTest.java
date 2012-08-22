package org.opennms.features.topology.netutils.internal;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class TracerouteWindowTest {

	TracerouteWindow traceWindow;
	TracerouteWindow traceWindow2;
	TracerouteWindow traceWindow3;
	Window mainWindow;
	Application app;
	
	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		traceWindow = new TracerouteWindow(testNode1, "http://localhost:8080/");
		traceWindow2 = new TracerouteWindow(null, "http://localhost:8080/");
		traceWindow3 = new TracerouteWindow(testNode1, "");
		mainWindow = new Window();
		app = new Application() { //Empty Application
			@Override
			public void init() {}
		};
		app.setMainWindow(mainWindow);
		app.getMainWindow().addWindow(traceWindow);
		app.getMainWindow().addWindow(traceWindow2);
		app.getMainWindow().addWindow(traceWindow3);
	}
	
	@Test
	public void testBuildURL_correctInput() {
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("127.0.0.1");
		assertNotNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_malformedURL() {
		traceWindow3.numericalDataCheckBox.setValue(true);
		traceWindow3.forcedHopField.setValue("127.0.0.1");
		assertNull(traceWindow3.buildURL());
	}

	@Test
	public void testBuildURL_upperBounds() {
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("256.256.256.256");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_lowerBounds() {
		traceWindow.forcedHopField.setValue("-1.-1.-1.-1");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_nonIntegerInput() {
		traceWindow.forcedHopField.setValue("abc.def.ghi.jkl");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_notIPAddress() {
		traceWindow.forcedHopField.setValue("not an IP");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_negativeIntegers() {
		traceWindow.forcedHopField.setValue("-255.-255.-255.-255");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testButtonClick() {
		traceWindow.tracerouteButton.click();
		
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("127.0.0.1");
		traceWindow.tracerouteButton.click();
	}
	
	@Test
	public void testAttach() {
		assertTrue(app.getMainWindow().getChildWindows().contains(traceWindow));
		app.getMainWindow().removeWindow(traceWindow);
		assertFalse(app.getMainWindow().getChildWindows().contains(traceWindow));
	}
}

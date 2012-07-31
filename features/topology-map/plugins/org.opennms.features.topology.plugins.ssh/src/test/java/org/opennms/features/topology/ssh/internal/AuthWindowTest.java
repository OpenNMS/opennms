package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class AuthWindowTest {



	String testHost = "debian.opennms.org";
	String emptyHost = "";
	int testPort = 22;
	int emptyPort = 0;
	String invalidPort = "-1";  // passed in to test for invalid port
	String validPort = "22";
	String invalidPortString = "abcd"; // passed in to test for error checking
	String invalidHost = "philip";
	String testPassword = "password";
	String testUser = "usr";

	AuthWindow normalWindow;
	AuthWindow noPortWindow; 
	AuthWindow noHostWindow; 
	AuthWindow emptyWindow;
	AuthWindow invalidHostWindow;
	Window mainWindow;
	Application app;

	@Before
	public void setup (){     
		normalWindow = new AuthWindow(testHost, testPort);
		noPortWindow = new AuthWindow(testHost, emptyPort);
		noHostWindow = new AuthWindow(emptyHost, testPort);
		emptyWindow = new AuthWindow(emptyHost, emptyPort);
		invalidHostWindow = new AuthWindow(invalidHost, testPort);

		mainWindow = new Window();
		app = new Application() { //Empty Application
			@Override
			public void init() {}
		};
		app.setMainWindow(mainWindow);
		app.getMainWindow().addWindow(normalWindow);
		app.getMainWindow().addWindow(noHostWindow);
		app.getMainWindow().addWindow(noPortWindow);
		app.getMainWindow().addWindow(emptyWindow);
		app.getMainWindow().addWindow(invalidHostWindow);

	}

	@Test
	public void testButtonClick() {        
		normalWindow.buttonClick(null);
		assertEquals("Failed to log in", normalWindow.testString);

		noPortWindow.portField.setValue(invalidPort);
		noPortWindow.buttonClick(null);
		assertEquals("Port must be between 1 and 65535", noPortWindow.testString);
		
		invalidHostWindow.buttonClick(null);
		assertEquals("Failed to connect to host", invalidHostWindow.testString);
		
		emptyWindow.portField.setValue(invalidPortString);
		emptyWindow.buttonClick(null);
		assertEquals("Port must be an integer", emptyWindow.testString);
		
		emptyWindow.portField.setValue(validPort);
		emptyWindow.hostField.setValue(invalidHost);
		emptyWindow.buttonClick(null);
		assertEquals("Failed to connect to host", emptyWindow.testString);
	}

	@Test
	public void testAttach(){
		assertTrue(app.getMainWindow().getChildWindows().contains(normalWindow));
		app.getMainWindow().removeWindow(normalWindow);
		assertFalse(app.getMainWindow().getChildWindows().contains(normalWindow));
	}
	
	@Test
	public void testShowSSHWindow() {
		normalWindow.showSSHWindow();
		assertFalse(app.getMainWindow().getChildWindows().contains(normalWindow));
	}

}

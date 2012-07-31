package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.ssh.internal.testframework.SudoPaintTarget;

import com.vaadin.Application;
import com.vaadin.terminal.PaintException;
import com.vaadin.ui.Window;


public class SSHTerminalTest {

	String testHost = "debian.opennms.org";
	int testPort = 22;
	SSHTerminal sshTerm;
	SSHTerminal.SessionTerminal sessionTerm;
	Application app;
	Window mainWindow;

	@Before
	public void setUp() throws Exception {

		app = new Application() {
			@Override
			public void init() {}
		};
		mainWindow = new Window();
		app.setMainWindow(mainWindow);

		SSHWindow sshWindow = new SSHWindow(null, 200, 200);
		app.getMainWindow().addWindow(sshWindow);

		SshClient client = SshClient.setUpDefaultClient();
		client.start();
		ClientSession session = null;
		try {
			session = client.connect(testHost, testPort).await().getSession();
		} catch (Exception e) {
			fail("Could not connect to host");
		}
		sshTerm = new SSHTerminal(sshWindow, session, 200, 200);
		sshWindow.addComponent(sshTerm);
	}

	@Test
	public void testPaintContent() {
		try {
			sshTerm.paintContent(new SudoPaintTarget());
		} catch (PaintException e) {
			fail("PaintContent exception was thrown");
		} 
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testChangeVariables() {
		Map map = new LinkedHashMap();

		map.put("isClosed", false);
		map.put("toSSH", "data to the ssh server");
		sshTerm.changeVariables(new Object(), map);

		map.put("isClosed", true);
		map.put("toSSH", "data to the ssh server");
		sshTerm.changeVariables(new Object(), map);
	}
	
	@Test
	public void testClose() {
		assertTrue(sshTerm.close());
	}

}

package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class SSHWindowTest {
    
	Application app;
    Window mainWindow;
    SSHWindow sshWindow;
    SSHWindow sshWindow2;
    SshClient client;
    ClientSession session;
    String testHost = "debian.opennms.org";
    int testPort = 22;
    
    @SuppressWarnings("serial")
	@Before
    public void setup () {
        app = new Application() {
            @Override
            public void init() {}
        };
        sshWindow = new SSHWindow(null, 200, 200);
        client = SshClient.setUpDefaultClient();
        client.start();
        try {
			session = client.connect(testHost, testPort).await().getSession();
		} catch (Exception e) {
			fail("Could not connect to host");
		}
        sshWindow2 = new SSHWindow(session, 200, 200);
        mainWindow = new Window();
        app.setMainWindow(mainWindow);
        app.getMainWindow().addWindow(sshWindow);
        app.getMainWindow().addWindow(sshWindow2);
        
    }
    
    @Test
    public void testAttach() {
    	assertTrue(app.getMainWindow().getChildWindows().contains(sshWindow));
    	app.getMainWindow().removeWindow(sshWindow);
    	assertFalse(app.getMainWindow().getChildWindows().contains(sshWindow));
    }
    
    @Test
    public void testClose() {
    	sshWindow2.close();
    	assertTrue(true); //Should execute above line without failure
    }
    
}

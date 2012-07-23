package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.apache.sshd.ClientSession;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class TestSSHWindow {
    Application a;
    Window w;
    SSHWindow window;
    
    @Before
    public void setup () {
        a = new Application(){

            @Override
            public void init() {
                // DO NOTHING            
            }
            
        };
        window = new SSHWindow(null, 200, 200);
        w = new Window();
    }
    
    @Test
    public void testAttach() {
        a.setMainWindow(w);
        w.addWindow(window);
        
        assertEquals(window, a.getMainWindow().getChildWindows().toArray()[0]);
    }
    
    @Test
    public void testClose() {
        window.close();
        assertEquals(0, a.getWindows().toArray().length);
    }
}

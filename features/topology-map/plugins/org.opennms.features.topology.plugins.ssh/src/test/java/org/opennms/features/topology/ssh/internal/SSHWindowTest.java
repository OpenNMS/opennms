package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class SSHWindowTest {
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
        w = new Window("Main Window");
    }
    
    @Test
    public void testAttach() {
    	//Causes compilation error
        a.setMainWindow(w);
        a.getMainWindow().addWindow(window);
        
        assertEquals(window, a.getMainWindow().getChildWindows().toArray()[0]);
    }
    
}

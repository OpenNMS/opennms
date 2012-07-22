package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import com.vaadin.ui.TextField;

public class TestAuthWindow {
    

    
    String testHost = "debian.opennms.org";
    String emptyHost = "";
    int testPort = 22;
    int emptyPort = 0;
    String invalidPort = "-1";  // passed in to test for invalid port
    String invalidPortString = "abcd"; // passed in to test for error checking
    String testPassword = "password";
    String testUser = "usr";
    
    AuthWindow normalWindow;
    AuthWindow noPortWindow; 
    AuthWindow noHostWindow; 
    AuthWindow emptyWindow;
    
    @Before
    public void setup (){     
       normalWindow = new AuthWindow(testHost, testPort);
       noPortWindow = new AuthWindow(testHost, emptyPort);
       noHostWindow = new AuthWindow(emptyHost, testPort);
       emptyWindow = new AuthWindow(emptyHost, emptyPort);
       
    }
    
    @Test
    public void testWindowCreation(){
        
    }
    
    @Test
    public void testButtonClick() {        
        try {
            normalWindow.buttonClick(null);
        } catch (Exception e){
            assertEquals("Failed to connect to host", normalWindow.testString);
        } 

        try {
            noPortWindow.portField.setValue(invalidPort);
            noPortWindow.buttonClick(null);
        } catch (Exception e) {
            assertEquals("Port must be between 1 and 65535", noPortWindow.testString);
        }
        
        try {
            emptyWindow.portField.setValue(invalidPortString);
            emptyWindow.buttonClick(null);
        } catch (Exception e) {
            assertEquals("Port must be an integer", emptyWindow.testString);
        }
    }
    
    @Test
    public void testAttach(){
        
    }
    @Test
    public void tearDown() {
        
    }

}

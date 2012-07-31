package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KeyBufferTest {

    String testString = "test"; // The string value used to create Keys in the KeyBuffer
    String doubleTestString = testString + testString; // Double the string used in testing drain
    KeyBuffer k; // The KeyBuffer used for testing
    
    @Before
    public void setup () {
       k = new KeyBuffer();
    }
    
    @Test
    public void testAdd(){
        assertEquals(0, k.size()); // Empty KeyBuffer will have size zero
        k.add(testString);
        assertEquals(1,k.size()); // Size should be 1 since we just added an item
    }
    
    @Test
    public void testToString(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        assertEquals("", k.toString());
        k.add(testString);
        assertEquals(testString, k.toString());
        }
    
    @Test 
    public void testFree(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        k.add(testString); // Add the test string so we have data to free
        k.free(); // Clear the buffer
        assertEquals(0, k.size()); // Make sure the list is empty again after the drain
        
        assertEquals(0, k.size());
        k.add(testString);
        k.add(testString);
        k.free();
        assertEquals(0, k.size());
    }
    @Test
    public void testDrain(){
        assertEquals(0, k.size()); // Make sure the list is empty before we start
        k.add(doubleTestString); // Add the test string twice so we have some data to drain
        assertEquals(doubleTestString, k.drain()); // drain the list into a string
        assertEquals(0, k.size()); // Make sure the list is empty again after the drain
    }
    

}


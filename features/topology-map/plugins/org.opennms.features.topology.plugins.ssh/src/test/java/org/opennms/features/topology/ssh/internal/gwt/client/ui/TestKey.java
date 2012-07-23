package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestKey {
    String testString = "test";
    String valueString = "value";
    String prevString = "prev";
    String nextString = "next";
    
    Key testKey; 
    Key prevKey; 
    Key nextKey; 
    
    @Before 
    public void setup(){
      testKey  = new Key(testString);
      prevKey  = new Key(prevString);
      nextKey  = new Key(nextString);
    }
    
    
    @Test
    public void getAndSetValue(){
        assertEquals(testString, testKey.getValue());
        testKey.setValue(valueString);
        assertEquals(valueString, testKey.getValue());
    }
    
    @Test 
    public void getAndSetPrev(){
        assertNull(testKey.getPrev());
        testKey.setPrev(prevKey);
        assertNotNull(testKey.getPrev());
        assertEquals(prevString, testKey.getPrev().getValue()); // Tests setPrev
    }
    
    @Test
    public void getAndSetNext() {
        assertNull(testKey.getNext());
        testKey.setNext(nextKey);
        assertNotNull(testKey.getNext());
        assertEquals(nextString, testKey.getNext().getValue());
        
    }
}

package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TestNoClosePipedOuputStream {

    NoClosePipedOutputStream nullSource;
    NoClosePipedOutputStream out;
    NoClosePipedInputStream in; 

    
    @Before
    public void setup() {
        out = new NoClosePipedOutputStream();
        nullSource = null;
        in = new NoClosePipedInputStream();
    }
    
    @Test
    public void testNullConnect() {
        
        try {
            out.connect(null);
            fail();
        } catch (NullPointerException e) {
            // If it gets to this point, then the exception must have been
            // thrown, and it is doing the right thing.
            return;
        } catch (IOException e) {
            fail(); // This should not happen
        }
        fail(); // If the exception was not caught, the code is not error checking properly
    }
    
    @Test
    public void testAlreadyConnected () {
        try {
            out.connect(in);
            out.connect(in);
        } catch (IOException e) {
            // If it gets to this point, then the exception must have been
            // thrown, and it is doing the right thing.
            return;
        } catch (NullPointerException e) {
            fail (); // This should not happen
        }
        fail(); // If the exception was not caught, the code is not error checking properly
    }
    
    @Test
    public void testNormalConnect() {
        try {
            out.connect(in);
        } catch (IOException e) {
            fail(); // This will only happen if the source is already connected
        } catch(NullPointerException e) {
            fail(); // This will only happen if the source is null
        }
        // If no exception is caught, the test passes
    }
    @Test
    public void testNotConnectedIntWrite () {
        int b  = 1;
        try {
            out.write(b);
            fail(); // error checking is not working properly
        } catch (IOException e) {
            // Should be thrown if the source is null
            return;
        }
        fail(); // Error checking is not working properly
    }
    
    @Test
    public void testIntWrite() throws IOException {
        int b = 1;
        out.connect(in);
        out.write(b);
       
    }
    @Test
    public void testBufferWrite () {
        
    }
    @Test
    public void testFlush () {
        
    }
}

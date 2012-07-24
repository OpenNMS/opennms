package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TestNoClosePipedOuputStream {

    NoClosePipedOutputStream nullSource;
    NoClosePipedOutputStream out;
    NoClosePipedInputStream in; 
    int testValue = 25;
    byte[] testByte = {1,2,3,4};
    byte[] emptyByte = new byte[0];


    @Before
    public void setup() {
        out = new NoClosePipedOutputStream();
        nullSource = null;
        in = new NoClosePipedInputStream(); 
    }

    @Test
    public void testConnectToNullSource() {

        try {
            out.connect(null);
            fail("This test should have already thrown a NullPointerException");
        } catch (NullPointerException e) {
            // If it gets to this point, then the exception must have been
            // thrown, and it is doing the right thing.
            return;
        } catch (IOException e) {
            fail("The pipe is already connected"); // This should not happen
        }
        fail("This test should have caught a NullPointerException"); // If the exception was not caught, the code is not error checking properly
    }

    @Test
    public void testConnectWhenAlreadyConnected () {
        try {
            out.connect(in);
            out.connect(in);
            fail("This test should have thrown an IOException already");
        } catch (IOException e) {
            // If it gets to this point, then the exception must have been
            // thrown, and it is doing the right thing.
            return;
        } catch (NullPointerException e) {
            fail ("The source being connected to is null"); // This should not happen
        }
        fail("This test should have caught an IOException"); // If the exception was not caught, the code is not error checking properly
    }

    @Test
    public void testNormalConnect() {
        try {
            out.connect(in);
        } catch (IOException e) {
            fail("The pipe is already connected"); // This will only happen if the source is already connected
        } catch(NullPointerException e) {
            fail("The source being connected to is null"); // This will only happen if the source is null
        }
        // If no exception is caught, the test passes

    }
    @Test
    public void testNotConnectedIntWrite () {
        try {
            out.write(testValue);
            fail("This test should have thrown an IOException already"); // error checking is not working properly
        } catch (IOException e) {
            // Should be thrown if the source is null
            return;
        }
        fail("This test should have caught an IOException"); // Error checking is not working properly
    }

    @Test
    public void testIntWrite() throws IOException {
        out.connect(in);
        out.write(testValue);
        assertEquals("25", java.lang.Byte.valueOf(in.buffer[0]).toString());

    }
    @Test
    public void testNotConnectedByteWrite () {
        try {
            out.write(testByte, 0, 1);
            fail("This test should have thrown an IOException already");
        } catch (IOException IOE) {
           return; //Should be thrown if the pipe is not connected
        } catch (NullPointerException NPE) {
            fail("The byte is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException IOB) {
            fail ("The offset or write length is invalid"); // Should not be thrown in this test case
        }
        fail("This test should have caught an IOException"); // It should not be possible for this test to reach the end 
    }
    
    @Test
    public void testNullByteWrite () {
        try {
            out.connect(in);
            out.write(null, 0, 1);
            fail("This test should hae thrown a NullPointerException already");
        } catch (IOException IOE) {
          fail ("The popes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException NPE) {
            return;  // This should be thrown due to the fact that the byte is null
        } catch (IndexOutOfBoundsException IOB) {
            fail ("The offset or write length is invalid"); // Should not be thrown in this test case
        }
        fail("This test should have caught a NullPointerException"); // It should not be possible for this test to reach the end 
    }
    
    @Test
    public void testIndexOutOfBoundsByteWrite () {
        try {
            out.connect(in);
            out.write(testByte, -1, 1);
            fail("This test should have thrown an IndexOutOfBoundsException already");
        } catch (IOException IOE) {
          fail ("The pipes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException NPE) {
            fail("The bye array is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException IOB) {
            return; // This should be thrown due to the fact that the Index referenced is larger than the array
        }
        fail("This test should have caught an IndexOutOfBoundsException"); // It should not be possible for this test to reach the end 
    }
    
    public void testNormalByteWrite () {
        try {
            out.connect(in);
            out.write(testByte, 1, 1);
        } catch (IOException IOE) {
            fail("The pipes are not connected"); // This should not be thrown in this test
        } catch (NullPointerException NPE) {
            fail("The byte array is most likely null"); // This should not be thrown in this test
        } catch (IndexOutOfBoundsException IOB) {
            fail("The offset or write length is invalid"); // This should not be thrown in this test
        }
        assertEquals("2", java.lang.Byte.valueOf(in.buffer[0]).toString());
    }
    @Test
    public void testFlush () {

    }
    
    @Test 
    public void testNoClose() {
        try{ 
            out.connect(in);
            out.close();
         } catch (IOException IOE) {
             fail("The pipes are already connected or the pipe is already closed");
         } catch (NullPointerException NPE) {
             fail("The source being connected to is null");
         }
         try {
             out.connect(in);
         } catch (IOException IOE) {
             // Due to the fact that the pipe cannot be closed, it should throw this
             // from the multiple pipe connect attempts
         } catch (NullPointerException NPE) {
             fail("The source being connected to is null");
         }
    }
    
    @Test
    public void testZeroLengthByteWrite() {
        try {
            out.connect(in);
            out.write(testByte, 1, 0); // write from offset, and continue writing for zero length
        } catch (IOException IOE) {
            fail("The pipes are not connected"); // This should not be thrown in this test
        } catch (NullPointerException NPE) {
            fail("The byte array is most likely null"); // This should not be thrown in this test
        } catch (IndexOutOfBoundsException IOB) {
            fail("The offset or write length is invalid"); // This should not be thrown in this test
        }
              assertEquals("0", java.lang.Byte.valueOf(in.buffer[0]).toString());
    }
}

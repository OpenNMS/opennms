package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class NoClosePipedInputStreamTest {

    NoClosePipedInputStream in;
    NoClosePipedOutputStream out;
    NoClosePipedInputStream definedPipeSize;
    NoClosePipedInputStream definedPipeAndSource;
    NoClosePipedInputStream definedSource;
    
    byte[] testByte = {1,2,3,4};
    
    
    @Before 
    public void setup() {
        in = new NoClosePipedInputStream();
        out = new NoClosePipedOutputStream();
        byte b = 10;
        for(int i = 0; i < 10; i++) {            
            in.buffer[i] = b;
            b++;

        }
    }
    
    @Test
    public void testCreatePipeWithDefinedPipeSize() {
        try {
             definedPipeSize = new NoClosePipedInputStream(64);
        } catch(Exception e) {
            fail();
        }
        assertEquals(false, definedPipeSize.connected);
        assertEquals(64, definedPipeSize.buffer.length);
    }
    
    @Test
    public void testCreatePipeWithNegativePipeSize() {
        try {
            definedPipeSize = new NoClosePipedInputStream(-1);
        } catch (IllegalArgumentException e) {
            assertEquals("Pipe Size <= 0", e.getMessage());
            return; // This test should throw this exception
        }
        fail("This test should throw an IllegalArgument Exception. Error checking is not working correctly");
    }
    
    @Test
    public void testCreatePipeWithDefinedSource() throws IOException {
        definedSource = new NoClosePipedInputStream(out);
        assertEquals(true, definedSource.connected);
        assertEquals(definedSource.PIPE_SIZE, definedSource.buffer.length);
    }
    
    @Test 
    public void testCreatePipeWithDefinedPipeSizeAndSource() throws IOException {
        definedPipeAndSource = new NoClosePipedInputStream(out, 64);
        assertEquals(64, definedPipeAndSource.buffer.length);
        assertEquals(true, definedPipeAndSource.connected);
    }
    @Test
    public void testNormalConnect() {
        try {
            in.connect(out);
        } catch (IOException e) {
            fail("IOException caught. This should not happen");
        }
        // If no exception is caught, the test passes
    }
    
    @Test
    public void testNotConnectedPipesIntReceive() {
        try{
            in.receive(1);
            fail("This test should have thrown an IOException already");
        } catch (IOException e) {
            assertEquals("Pipe not connected", e.getMessage());
            return; // The pipes are not connected, and therefore this exception should be thrown;
        } 
        fail("This test should have thrown a IOException due to not connected pipes");
    }
    
    @Test 
    public void testAwaitSpaceIntReceive() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should pass when the read position increments
        
        in.connect(out);
        in.in=1;
        in.out=1;
        boolean waited = false;
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.receive(1);
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 5000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
            }
            try {
                Thread.sleep(500);
                in.in++;
            }
            catch (InterruptedException t) {
                fail("Test thread was interrupted");
            }
        }
        // This will fail if the system takes longer than 5 seconds to get out of the awaitSpace() method
        assertEquals(false, waited);
        
    }
    
    @Test 
    public void testInterruptedAwaitSpaceIntReceive() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should time out due to the fact that neither the 
        // read position nor the write position change. The timeout will cause
        // this test to pass.
        
        in.connect(out);
        in.in=1;
        in.out=1;
        boolean waited = false;
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.receive(1);
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 3000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
                thread.interrupt();
                break;
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException t) {
                // The thread should be interrupted, but this should not cause problems.
            }
        }
        assertEquals(true, waited);
        
    }
    
    @Test
    public void testInEqualToBufferLengthIntReceive() throws IOException{
        in.connect(out);
        in.in = 1023;
        in.out = 1;
        in.receive(1);
        
        assertEquals(0, in.in);
    }
    
    @Test
    public void testNormalIntReceive () {
        try{
            in.connect(out);
            in.receive(1);
        } catch (IOException e) {
            fail("This test should have thrown an IOException already"); // The pipe cannot close
        } 
        assertEquals("1", java.lang.Byte.valueOf(in.buffer[0]).toString());
    }
    
    @Test
    public void testNormalByteReceive() throws IOException {
        in.in = 1;
        in.out  = 1;
        in.connect(out);
        in.receive(testByte, 0, 4);
        
        for(int i = 1; i < testByte.length; i++) {
            assertEquals(testByte[i], in.buffer[i]);
        }
    }

    @Test
    public void testZeroLengthByteReceive() throws IOException {
        in.connect(out);
        in.in=1;
        in.out=1;
        in.receive(testByte, 0, 0);
        
        assertEquals(10, in.buffer[0]);
    }

    @Test
    public void testOutLessThanInByteReceive() throws IOException {
        in.connect(out);
        in.in = 5;
        in.out = 1;
        in.receive(testByte, 0, 4);
        
        for(int i = 5; i < testByte.length; i++) {
            assertEquals(testByte[i], in.buffer[i]);
        }
        assertEquals(14,in.buffer[4]);
    }
    
    @Test
    public void testInLessThanOutByteReceive() throws IOException {
        in.connect(out);
        in.in = 1;
        in.out = 5;
        in.receive(testByte, 0, 4);
        
        for(int i = 0; i < testByte.length; i++) {
            assertEquals(testByte[i], in.buffer[i+1]);
        }
        assertEquals(15,in.buffer[5]);
    }
    
    @Test 
    public void testAwaitSpaceByteReceive() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should time out due to the fact that neither the 
        // read position nor the write position change. The timeout will cause
        // this test to pass.
        
        in.connect(out);
        in.in=1;
        in.out=1;
        boolean waited = false;
      
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.receive(testByte, 0, 1);
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 3000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
                break;
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException t) {
                // The thread should be interrupted, but this should not cause problems.
            }
        }
        assertEquals(true, waited);
    }
    
    @Test 
    public void testInterruptedAwaitSpaceByteReceive() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should time out due to the fact that neither the 
        // read position nor the write position change. The timeout will cause
        // this test to pass.
        
        in.connect(out);
        in.in=1;
        in.out=1;
        boolean waited = false;
      
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.receive(testByte, 0, 1);
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 3000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
                thread.interrupt();
                break;
            }
            try {
                Thread.sleep(500);
                in.in++;
            }
            catch (InterruptedException t) {
                // The thread should be interrupted, but this should not cause problems.
            }
        }
        assertEquals(false, waited);
    }
    
    @Test
    public void testInGreaterThanBufferLengthByteReceive() throws IOException {
        
        in.connect(out);
        in.in = 1023;
        in.out = 5;
        
        in.receive(testByte, 0, 1);
        
        assertEquals(0, in.in);
    }
    
    @Test
    public void testNotConnectedIntRead() {
        try{
            in.read();
            fail("This test should have already thrown an IOException");
        } catch (IOException e) {
            // The pipe was not connected before the read, and therefore this exception should be thrown;
            assertEquals("Pipe not connected", e.getMessage());
            return;
        }
        fail("This test should have thrown an IOException");
    }
    
    @Test
    public void testNormalIntRead() throws IOException {
            in.connect(out);
            in.out = 3;
            in.in = 1;
           
            
            assertEquals(13, in.read()); // because in reads buffer[out++]
            assertEquals(4, in.out);  // because buffer[out++] increments in.out
            assertEquals(1, in.in); // because in.in was equal to in.out
            
            in.in = 100;
            in.out = 100;
            
            assertEquals(0, in.read());
    }
    
    @Test
    public void testWaitIntRead() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should time out due to the fact that neither the 
        // read position nor the write position change. The timeout will cause
        // this test to pass.
        
        in.connect(out);
        in.in = -1;
        in.out=1;
        boolean waited = false;
      
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.read();
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 3000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
                break;
            }
            try {
                Thread.sleep(500);
                in.in++;
            }
            catch (InterruptedException t) {
            }
        }
        assertEquals(false, waited);
    }
    
    @Test
    public void testInterruptedWaitIntRead() throws IOException {
        // This test creates a thread to test the waiting that the buffer
        // performs when it is either full or empty. This test gets into the
        // awaitSpace() method because the read position and write position are
        // the same, and should time out due to the fact that neither the 
        // read position nor the write position change. The timeout will cause
        // this test to pass.
        
        in.connect(out);
        in.in = -1;
        in.out = 1;
        boolean waited = false;
      
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    in.read();
                } catch (IOException e) {
                    fail("Pipe not Connected");
                } 
            }
        });
        
        thread.start();
        long endTimeMillis = System.currentTimeMillis() + 3000;
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                waited = true;
                thread.interrupt();
                break;
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException t) {
                // The thread should be interrupted, but this should not cause problems.
            }
        }
        assertEquals(true, waited);
    }
    
    @Test
    public void testOutEqualToBufferLengthIntRead() throws IOException {
            in.connect(out);
            in.out = 1023;
            in.in = 1;
            
            assertEquals(0, in.read()); // because in reads buffer[out++]
            assertEquals(0, in.out);  // because buffer[out++] increments in.out
            assertEquals(1, in.in); // because in.in was equal to in.out
            
    }
    
    @Test
    public void testInEqualOutIntRead() throws IOException {
            in.connect(out);
            in.out = 1;
            in.in = 2;
           
            
            assertEquals(11, in.read()); // because in reads buffer[out++]
            assertEquals(2, in.out);  // because buffer[out++] increments in.out
            assertEquals(-1, in.in); // because in.in was equal to in.out
            
    }
    
    @Test
    public void testNullSourceByteRead() {
        try{
            in.connect(out);
            in.read(null, 0, 1);
            fail("This test should have already thrown a NullPointerException");
        } catch(NullPointerException e) {
            //Reading should not be possible from a null source
            return;
        } catch (IOException e) {
            //IOException is only possible with out of bounds index or not connected pipe
            fail("The index is out of bounds");
        }
        fail("This test should have thrown a NullPointerException");
    }
    
    @Test
    public void testNormalByteRead() throws IOException {
            in.connect(out);
            in.out=1;
            in.in=1;
            assertEquals(1, in.read(testByte, 0, 1));
            assertEquals(1, in.read(testByte, 1, 1));
            assertEquals(1, in.read(testByte, 2, 1));
    }
    
    @Test
    public void testNegativeOffsetByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in=1;
        
       try{
           in.read(testByte, -1, 1);
           fail("This test should have thrown an IndexOutOfBoundsException already"); // This test should not continue past the first line
       } catch(IndexOutOfBoundsException e) {
            //A negative offset should throw this exception
           return;
       }
       //This test should not complete successfully 
       fail("This test should have thrown an IndexOutOfBoundsException");       
    }
    
    @Test
    public void testNegativeReadLengthByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in=1;
        
       try{
           in.read(testByte, 0, -1);
           fail("This test should have thrown an IndexOutOfBoundsException already"); // This test should not continue past the first line
       } catch(IndexOutOfBoundsException e) {
            //A negative read length should throw this exception
           return;
       }
       //This test should not complete successfully 
       fail("This test should have thrown an IndexOutOfBoundsException");       
    }
    
    @Test
    public void testTooLongByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in=1;
        
       try{
           in.read(testByte, 0, 5);
           fail("This test should have thrown an IndexOutOfBoundsException already"); // This test should not continue past the first line
       } catch(IndexOutOfBoundsException e) {
            //A read length longer than the byte array should throw this error
           return;
       }
       //This test should not complete successfully 
       fail("This test should have thrown an IndexOutOfBoundsException");       
    }
    
    @Test
    public void testZeroLengthByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in=0;
        // should return zero due to the fact that nothing was read
        assertEquals(0, in.read(testByte,0,0));
    }
    
    @Test
    public void testMultipleCharacterByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in= 1;    
        //returns 2 because 2 bytes have been read
        assertEquals(2,in.read(testByte, 0, 2)); 
    }
    
    @Test
    public void testInGreaterThanOutMultipleCharacterByteRead() throws IOException {
        in.connect(out);
        in.out=1;
        in.in= 3;
              
        // Should read 2 bytes since it is a normal byte write
        assertEquals(2,in.read(testByte, 0, 2)); 
                                                    
    }
    
    @Test
    public void testOutEqualToBufferLengthByteRead() throws IOException { 
        in.connect(out);
        in.out=1022;
        in.in= 1;
              
        // Should read 2 bytes since it is a normal byte write
        assertEquals(2,in.read(testByte, 0, 2)); 
        
        // The out value should have been reset to zero since it was larger than the buffer size
        assertEquals(0, in.out);
        
    }
    
    @Test
    public void testNegativeInAvailable() throws IOException {
        in.in = -1;
        
        // This should return zero since there is zero available buffer space
        assertEquals(0, in.available());
    }
    
    @Test
    public void testInEqualsOutAvailable() throws IOException {
        in.in=1;
        in.out=1;
        // This should return the full buffer length since the read and write positions are the same
        assertEquals(in.buffer.length, in.available());
    }
    
    @Test
    public void testInGreaterThanOutAvailable() throws IOException {
        in.in = 3;
        in.out = 1;
        
        //This should return the write position minus the read position
        assertEquals(in.in - in.out, in.available());
    }
    
    @Test
    public void testNormalAvailable() throws IOException {
        in.in = 1;
        in.out = 3;
        
        //This should return the read position plus the buffer length minus the write position
        //to show available buffer space
        assertEquals(in.in + in.buffer.length - in.out, in.available());
        
    }
}

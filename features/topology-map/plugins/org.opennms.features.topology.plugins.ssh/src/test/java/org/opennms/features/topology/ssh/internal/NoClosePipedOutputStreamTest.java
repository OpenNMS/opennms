/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class NoClosePipedOutputStreamTest {

    NoClosePipedOutputStream definedSource;
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
    public void testCreateWithDefinedSource() throws IOException {
            definedSource = new NoClosePipedOutputStream(in);
            assertEquals(true, in.connected);
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
            assertEquals("Already connected", e.getMessage());
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
            // Should be thrown if the pipes are not connected
            assertEquals("Pipe not connected", e.getMessage());
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
        } catch (IOException e) {
            assertEquals("Pipe not connected", e.getMessage());
            return; //Should be thrown if the pipe is not connected
        } catch (NullPointerException e) {
            fail("The byte is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException e) {
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
        } catch (IOException e) {
            fail ("The popes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException e) {
            return;  // This should be thrown due to the fact that the byte is null
        } catch (IndexOutOfBoundsException e) {
            fail ("The offset or write length is invalid"); // Should not be thrown in this test case
        }
        fail("This test should have caught a NullPointerException"); // It should not be possible for this test to reach the end 
    }

    @Test
    public void testNegativeOffsetByteWrite () {
        try {
            out.connect(in);
            out.write(testByte, -1, 1);
            fail("This test should have thrown an IndexOutOfBoundsException already");
        } catch (IOException e) {
            fail ("The pipes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException e) {
            fail("The bye array is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException e) {
            return; // This should be thrown due to the fact that the Index referenced is larger than the array
        }
        fail("This test should have caught an IndexOutOfBoundsException"); // It should not be possible for this test to reach the end 
    }

    @Test
    public void testNormalByteWrite () throws IOException {
        out.connect(in);
        
        out.write(testByte, 1, 1);
        assertEquals("2", java.lang.Byte.valueOf(in.buffer[0]).toString());     
    }

    @Test
    public void testNegativeLengthByteWrite() {
        try {
            out.connect(in);
            out.write(testByte, 1, -1);
            fail("This test should have thrown an IndexOutOfBoundsException already");
        } catch (IOException e) {
            fail ("The pipes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException e) {
            fail("The bye array is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException e) {
            return; // This should be thrown due to the fact that the Index referenced is larger than the array
        }
        fail("This test should have caught an IndexOutOfBoundsException"); // It should not be possible for this test to reach the end 
    }

    @Test
    public void testSumOfOffsetAndLengthLargerThanArrayByteWrite() {  
        try {
            out.connect(in);
            out.write(testByte, 3, 3);
            fail("This test should have thrown an IndexOutOfBoundsException already");
        } catch (IOException e) {
            fail ("The pipes are not connected"); // Should not be thrown in this test case
        } catch (NullPointerException e) {
            fail("The bye array is most likely null");  // Should not be thrown in this test case
        } catch (IndexOutOfBoundsException e) {
            return; // This should be thrown due to the fact that the Index referenced is larger than the array
        }
        fail("This test should have caught an IndexOutOfBoundsException"); // It should not be possible for this test to reach the end 
    }

    @Test
    public void testOffsetLargerThanArrayByteWrite() {
        try {
            out.connect(in);
            out.write(testByte, 10, 1);
            assertEquals("2", java.lang.Byte.valueOf(in.buffer[0]).toString());
        } catch (IOException e) {
            fail("The pipes are not connected"); // This should not be thrown in this test
        } catch (NullPointerException e) {
            fail("The byte array is most likely null"); // This should not be thrown in this test
        } catch (IndexOutOfBoundsException e) {
            return; // The offset is larger than the size of the byte array, and therefore this is expected
        }
        fail("This test should have thrown an IndexOutOfBoundsException"); // This should not be thrown in this test
    }

    @Test
    public void testZeroLengthByteWrite() {
        try {
            out.connect(in);
            out.write(testByte, 1, 0); // write from offset, and continue writing for zero length
        } catch (IOException e) {
            fail("The pipes are not connected"); // This should not be thrown in this test
        } catch (NullPointerException e) {
            fail("The byte array is most likely null"); // This should not be thrown in this test
        } catch (IndexOutOfBoundsException e) {
            fail("The offset or write length is invalid"); // This should not be thrown in this test
        }
        assertEquals("0", java.lang.Byte.valueOf(in.buffer[0]).toString());
    }
}

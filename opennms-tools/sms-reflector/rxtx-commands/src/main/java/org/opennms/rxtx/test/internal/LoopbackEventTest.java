/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.rxtx.test.internal;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * <p>LoopbackEventTest class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LoopbackEventTest {

    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;

    /**
     * <p>connect</p>
     *
     * @param portName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void connect(String portName) throws IOException {
        try {
            // Obtain a CommPortIdentifier object for the port you want to open
            CommPortIdentifier portId =
                    CommPortIdentifier.getPortIdentifier(portName);

            // Get the port's ownership
            serialPort =
                    (SerialPort) portId.open("Demo application", 5000);

            // Set the parameters of the connection.
            setSerialPortParameters();

            // Open the input and output streams for the connection.
            // If they won't open, close the port before throwing an
            // exception.
            outStream = serialPort.getOutputStream();
            inStream = serialPort.getInputStream();
        } catch (NoSuchPortException e) {
            throw new IOException(e.getMessage());
        } catch (PortInUseException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            serialPort.close();
            throw e;
        }
    }

    /**
     * Get the serial port input stream
     *
     * @return The serial port input stream
     */
    public InputStream getSerialInputStream() {
        return inStream;
    }

    /**
     * Get the serial port output stream
     *
     * @return The serial port output stream
     */
    public OutputStream getSerialOutputStream() {
        return outStream;
    }

    /**
     * Register event handler for data available event
     *
     * @param eventHandler Event handler
     */
    public void addDataAvailableEventHandler(
            SerialPortEventListener eventHandler) {
        try {
            // Add the serial port event listener
            serialPort.addEventListener(eventHandler);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Disconnect the serial port
     */
    public void disconnect() {
        if (serialPort != null) {
            try {
                // close the i/o streams.
                outStream.close();
                inStream.close();
            } catch (IOException ex) {
                // don't care
                }
            // Close the port.
            serialPort.close();
        }
    }

    /**
     * Sets the serial port parameters to 57600bps-8N1
     */
    private void setSerialPortParameters() throws IOException {
        int baudRate = 57600; // 57600bps

        try {
            // Set serial port to 57600bps-8N1..my favourite
            serialPort.setSerialPortParams(
                    baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Unsupported serial port parameter");
        }
    }

    public static class SerialEventHandler implements SerialPortEventListener {

        private InputStream inStream;
        private int readBufferLen;
        private int readBufferOffset;
        private byte[] readBuffer;

        public SerialEventHandler(InputStream inStream, int readBufferLen) {
            this.inStream = inStream;
            this.readBufferLen = readBufferLen;
            readBuffer = new byte[readBufferLen];
        }

        public boolean isBufferFull() {
            return (readBufferOffset == readBufferLen);
        }

        public String getReadBuffer() {
            return new String(readBuffer);
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE:
		    System.out.println("DATA_AVAILABLE");
                    readSerial();
                    break;
            }
        }

        private void readSerial() {
            try {
                int availableBytes = inStream.available();
                System.out.println("inStream " + availableBytes + " available");
                if (availableBytes > 0) {
                    // Read the serial port
                	int n = inStream.read(readBuffer, readBufferOffset, 1);
                	if (n > 0) {
                		byte b = readBuffer[readBufferOffset];
                		System.out.println(String.format("READ: %d bytes: %c (0x%02x)", n, (char)b, b));
                		readBufferOffset += n;
                	} else {
                		System.out.println("No bytes read.");
                	}

                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
            	
            } catch (IOException e) {
            	System.out.println(e);
            }
            
        }

        public static void main(String[] args) {
            // Timeout = 1s
            final int TIMEOUT_VALUE = 10000;

            LoopbackEventTest loopbackTest = new LoopbackEventTest();
            try {
                // Open serial port
                loopbackTest.connect(args[0]);

                // Register the serial event handler
                String testString = args.length < 2 
                	? "The quick brown fox jumps over the lazy dog"
                    : args[1].replace("\\r", "\r").replace("\\n", "\n").replace("\\\\", "\\");
                InputStream inStream =
                        loopbackTest.getSerialInputStream();
                
                String result = args.length < 3 ? testString : args[2].replace("\\r", "\r").replace("\\n", "\n").replace("\\\\", "\\"); 

                SerialEventHandler serialEventHandler =
                        new SerialEventHandler(inStream, 2048);
                loopbackTest.addDataAvailableEventHandler(serialEventHandler);

                // Send the testing string
                OutputStream outStream =
                        loopbackTest.getSerialOutputStream();

                byte[] bytes = testString.getBytes();

                for(int i = 0; i < bytes.length; i++) {
                	outStream.write(bytes[i]);
                }
		    
                // Wait until all the data is received
                long startTime = System.currentTimeMillis();
                long elapsedTime;
                boolean matches = false;
                String actual;
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    elapsedTime = System.currentTimeMillis() - startTime;
                    
                    actual = serialEventHandler.getReadBuffer();
                    matches = actual.equals(result);
                    System.out.println("Expected: " + result + " Actual: " + actual + " Equals? " + matches);
                } while ((elapsedTime < TIMEOUT_VALUE) && !matches);

                // Check the data if not TIMEOUT
                if (elapsedTime < TIMEOUT_VALUE) {
                    if (serialEventHandler.getReadBuffer().matches(result)) {
                        System.out.println("All data is received successfully");
                    } else {
                        System.out.println("Test failed");
                        System.out.println("Sent:" + testString);
                        System.out.println("Received:" +
                                serialEventHandler.getReadBuffer());
                    }
                } else {
                    System.err.println("Timeout");
                }

                System.out.println("Test done");
                loopbackTest.disconnect();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * This file is a derivative work, containing both original code, included code,
 * and modified code that was published under the GNU General Public License.
 * 
 * Original code for this file Copyright (C) 2002 Scott McCrory.
 * All rights reserved.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * Captures the output of an InputStream.
 * </P>
 * 
 * With acknowledgements to Michael C. Daconta, author of "Java Pitfalls, Time
 * Saving Solutions, and Workarounds to Improve Programs." and his article in
 * JavaWorld "When Runtime.exec() Won't".
 * 
 * See the ExecRunner class for a reference implementation.
 * 
 * @author <a href="mailto:smccrory@users.sourceforge.net">Scott McCrory </a>.
 */
public class StreamGobbler extends Thread {
	
	private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);

    /** The {@link InputStream} we're gobbling */
    private InputStream in = null;

    /** The {@link PrintWriter} we'll send the gobbled characters to if asked */
    private PrintWriter pwOut = null;

    /** Our flag to allow us to safely terminate the monitoring thread */
    private boolean quit = false;

    /**
     * Basic constructor for StreamGobbler.
     */
    public StreamGobbler() {
        super();
    }

    /**
     * A simpler constructor for StreamGobbler - defaults to {@link System#out}
     *
     * @param in
     *            InputStream
     */
    public StreamGobbler(final InputStream in) {
        this();
        this.in = in;
        this.pwOut = new PrintWriter(System.out, true);
    }

    /**
     * A more explicit constructor for StreamGobbler where you can tell it
     * exactly where to relay the output to. Creation date: (9/23/2001 8:48:01
     * PM)
     *
     * @param in
     *            InputStream
     * @param out
     *            OutputStream
     */
    public StreamGobbler(final InputStream in, final OutputStream out) {
        this();
        this.in = in;
        this.pwOut = new PrintWriter(out, true);
    }

    /**
     * A more explicit constructor for StreamGobbler where you can tell it
     * exactly where to relay the output to. Creation date: (9/23/2001 8:48:01
     * PM)
     *
     * @param in
     *            InputStream
     * @param pwOut
     *            PrintWriter
     */
    public StreamGobbler(final InputStream in, final PrintWriter pwOut) {
        this();
        this.in = in;
        this.pwOut = pwOut;
    }

    /**
     * We override the <code>clone</code> method here to prevent cloning of
     * our class.
     *
     * @throws java.lang.CloneNotSupportedException
     *             To indicate cloning is not allowed
     * @return Nothing ever really returned since we throw a
     *         CloneNotSupportedException
     */
    @Override
    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Tells the StreamGobbler to quit it's operation. This is safer than using
     * stop() since it uses a semaphore checked in the main wait loop instead of
     * possibly forcing semaphores to untimely unlock.
     */
    public void quit() {
        quit = true;
    }

    /**
     * We override the <code>readObject</code> method here to prevent
     * deserialization of our class for security reasons.
     * 
     * @param in
     *            java.io.ObjectInputStream
     * @throws IOException
     *             thrown if a problem occurs
     */
    private final void readObject(final ObjectInputStream in) throws IOException {
        throw new IOException("Object cannot be deserialized");
    }

    /**
     * Gobbles up all the stuff coming from the InputStream and sends it to the
     * OutputStream specified during object construction.
     */
    @Override
    public void run() {

        InputStreamReader isr = null;
        BufferedReader br = null;
        try {

            // Set up the input stream
            isr = new InputStreamReader(in);
            br = new BufferedReader(isr);

            // Initialize the temporary results containers
            String line = null;

            // Main processing loop which captures the output
            while ((line = br.readLine()) != null) {
                if (quit) {
                    break;
                } else {
                    pwOut.println(line);
                }
            }

        } catch (final Throwable e) {
        	LOG.debug("Unable to read lines.", e);
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
        }

    }

    /**
     * We override the <code>writeObject</code> method here to prevent
     * serialization of our class for security reasons.
     * 
     * @param out
     *            java.io.ObjectOutputStream
     * @throws IOException
     *             thrown if a problem occurs
     */
    private final void writeObject(final ObjectOutputStream out) throws IOException {
        throw new IOException("Object cannot be serialized");
    }

}

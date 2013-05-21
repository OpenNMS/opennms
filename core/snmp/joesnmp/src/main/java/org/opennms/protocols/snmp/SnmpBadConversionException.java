/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Constructed when the library is unable to covert a value to another.
 * 
 * @see SnmpIPAddress#convertToIpAddress()
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class SnmpBadConversionException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 5888208447180460071L;
    /**
     * The original exception that caused this exception to be generated.
     */
    private final Exception m_chained;

    /**
     * The exception constructor.
     * 
     * @param why
     *            The message for the exception.
     */
    public SnmpBadConversionException(String why) {
        super(why);
        m_chained = null;
    }

    /**
     * The exception constructor
     * 
     * @param why
     *            The message for the exception.
     * @param reason
     *            The original exception that caused the problem
     * 
     */
    public SnmpBadConversionException(String why, Exception reason) {
        super(why);
        m_chained = reason;
    }

    /**
     * The exception constructor
     * 
     */
    public SnmpBadConversionException() {
        super();
        m_chained = null;
    }

    /**
     * Constructs a new exception which is based upon a previous exception. The
     * two exceptions are chained together internally.
     * 
     * @param reason
     *            The original exception
     */
    public SnmpBadConversionException(Exception reason) {
        super(reason.getLocalizedMessage());
        m_chained = reason;
    }

    /**
     * Prints the stack trace of the exception. If the exception has been
     * chained then the original exception is also printed to the stream.
     */
    @Override
    public void printStackTrace() {
        this.printStackTrace(System.err);
    }

    /**
     * Prints the stack trace of the exception. If the exception has been
     * chained then the original exception is also printed to the stream.
     * 
     * @param writer
     *            The stream to writer the stack trace onto.
     */
    @Override
    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (m_chained != null) {
            writer.println("");
            writer.println("Original Reason");
            writer.println("");
            m_chained.printStackTrace(writer);
        }
    }

    /**
     * Prints the stack trace of the exception. If the exception has been
     * chained then the original exception is also printed to the stream.
     * 
     * @param stream
     *            The stream to writer the stack trace onto.
     */
    @Override
    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        if (m_chained != null) {
            stream.println("");
            stream.println("Original Reason");
            stream.println("");
            m_chained.printStackTrace(stream);
        }
    }
}

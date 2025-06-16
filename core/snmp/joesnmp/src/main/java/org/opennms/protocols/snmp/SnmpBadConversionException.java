/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

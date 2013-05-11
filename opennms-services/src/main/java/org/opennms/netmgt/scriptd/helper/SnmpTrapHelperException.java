/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.helper;

/**
 * This class describes an exception associated with the SnmpTrapHelper class.
 * If the described exception was caused by some other exception (e.g. an
 * exception associated with the JoeSNMP library), that exception will be
 * included as the "cause". The getMessage and printStackTrace methods are
 * overridden, in order to describe the "cause" exception, where present.
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
public class SnmpTrapHelperException extends Exception {

    private static final long serialVersionUID = -7866108381105769823L;

    /**
     * Construct an SnmpTrapHelperException with the specified message.
     *
     * @param message
     *            The message to be associated with the exception.
     */
    public SnmpTrapHelperException(String message) {
        super(message);
    }

    /**
     * Construct an SnmpTrapHelperException with the specified message and
     * cause.
     *
     * @param message
     *            The message to be associated with the exception.
     * @param cause
     *            The cause exception to be associated with the exception.
     */
    public SnmpTrapHelperException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the message associated with this exception. If this exception has an
     * associated cause exception, the message associated with the cause
     * exception is appended.
     *
     * @return The message associated with this exception.
     */
    @Override
    public String getMessage() {
        Throwable cause = getCause();

        if (cause == null) {
            return super.getMessage();
        } else {
            return super.getMessage() + " the problem resulted from: \n\t" + cause.toString();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Print the stack trace associated with this exception to the specified
     * print stream. If this exception has an associated cause exception, the
     * stack trace associated with the cause exception is printed.
     */
    @Override
    public void printStackTrace(java.io.PrintStream ps) {
        Throwable cause = getCause();

        if (cause == null) {
            super.printStackTrace(ps);
        } else {
            synchronized (ps) {
                ps.println(this);
                cause.printStackTrace(ps);
            }
        }
    }

    /**
     * Print the stack trace associated with this exception to System.err. If
     * this exception has an associated cause exception, the stack trace
     * associated with the cause exception is printed.
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print the stack trace associated with this exception to the specified
     * print writer. If this exception has an associated cause exception, the
     * stack trace associated with the cause exception is printed.
     *
     * @param pw
     *            The print writer to which the stack trace should be printed.
     */
    @Override
    public void printStackTrace(java.io.PrintWriter pw) {
        Throwable cause = getCause();

        if (cause == null) {
            super.printStackTrace(pw);
        } else {
            synchronized (pw) {
                pw.println(this);
                cause.printStackTrace(pw);
            }
        }
    }
}

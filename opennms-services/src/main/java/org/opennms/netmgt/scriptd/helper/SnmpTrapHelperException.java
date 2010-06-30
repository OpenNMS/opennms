//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2003 Tavve Software Company.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

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
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @version $Id: $
 */
public class SnmpTrapHelperException extends Exception {
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

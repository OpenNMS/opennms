/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.filter;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This class is used to indicate a failure of the Filter engine to correctly
 * parse a filter expression. If the error was caused by an undeclared exception
 * during processing, that exception is also encapsulated by the exception.
 *
 * @author <a href="jason@opennms.org">Jason </a>
 * @author <a href="weave@oculan.com">Weave </a>
 */
public class FilterParseException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = -1504582717903933407L;
    /**
     * The encapsulated throwable if any
     */
    private Throwable m_delegate;

    /**
     * Constructs a new, empty filter parser exception.
     */
    public FilterParseException() {
        super();
        m_delegate = null;
    }

    /**
     * Constructs a new parse exception with the passed message as its error
     * text.
     *
     * @param msg
     *            The exception text.
     */
    public FilterParseException(String msg) {
        super(msg);
        m_delegate = null;
    }

    /**
     * Constructs a new parse exception with the passed message as the error
     * text and the throwable as the encapsulated error causing the failure.
     *
     * @param msg
     *            The exception text.
     * @param t
     *            The cause of the failure.
     */
    public FilterParseException(String msg, Throwable t) {
        super(msg);
        m_delegate = t;
    }

    /**
     * Prints the stack trace of the exception, and the encapsulated exception
     * if any.
     */
    public void printStackTrace() {
        if (m_delegate != null)
            m_delegate.printStackTrace();
        super.printStackTrace();
    }

    /**
     * {@inheritDoc}
     *
     * Prints the stack trace of the exception, and the encapsulated exception
     * if any.
     */
    public void printStackTrace(PrintStream ps) {
        if (m_delegate != null)
            m_delegate.printStackTrace(ps);
        super.printStackTrace(ps);
    }

    /**
     * Prints the stack trace of the exception, and the encapsulated exception
     * if any.
     *
     * @param pw
     *            The location to write the exception.
     */
    public void printStackTrace(PrintWriter pw) {
        if (m_delegate != null)
            m_delegate.printStackTrace(pw);
        super.printStackTrace(pw);
    }
}

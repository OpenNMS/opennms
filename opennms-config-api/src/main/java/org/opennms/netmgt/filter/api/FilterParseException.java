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
package org.opennms.netmgt.filter.api;

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
    @Override
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
    @Override
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
    @Override
    public void printStackTrace(PrintWriter pw) {
        if (m_delegate != null)
            m_delegate.printStackTrace(pw);
        super.printStackTrace(pw);
    }
}

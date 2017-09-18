/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import junit.framework.AssertionFailedError;

/**
 * <p>ThrowableAnticipator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThrowableAnticipator extends Assert {
    /** Constant <code>IGNORE_MESSAGE="*** ThrowableAnticipator ignore Throwab"{trunked}</code> */
    public final static String IGNORE_MESSAGE =
        "*** ThrowableAnticipator ignore Throwable.getMessage() ***";
    
    private List<Throwable> m_anticipated;
    private List<Throwable> m_unanticipated;
    private boolean m_failFast;

    /**
     * Create a new anticipator with default failFast setting of true.
     */
    public ThrowableAnticipator() {
        this(true);
    }
    
    /**
     * Create a new anticipator with the specified failFast setting.
     *
     * @param failFast whether unanticipated exceptions will cause an immediate
     *        junit.framework.Assert#fail(String).  See #setFailFast(boolean)
     *        for details.
     */
    public ThrowableAnticipator(boolean failFast) {
        init();
        m_failFast = failFast;
    }
    
    private void init() {
        m_anticipated = new ArrayList<>();
        m_unanticipated = new ArrayList<>();
        m_failFast = true;
    }
   
    /**
     * Anticipate a specific java.lang.Throwable.
     *
     * @param t The java.lang.Throwable to anticipate.  Both the class and the
     *          message will be matched.
     */
    public void anticipate(Throwable t) {
        m_anticipated.add(t);
    }
    
    /**
     * Process a received throwable.  See #setFailFast(boolean) for the effects
     * of the failFast setting on when errors are signaled.
     *
     * @param t The received throwable.
     * @throws AssertionFailedError if failFast is set to true and an
     *         unanticipated java.lang.Throwable is received.
     */
    public void throwableReceived(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException("Throwable must not be null");
        }
        
        boolean foundMatch = false;

        for (Throwable our : m_anticipated) {
            if (t.getClass().isAssignableFrom(our.getClass())) {
                if (IGNORE_MESSAGE.equals(our.getMessage())
                        || (t.getMessage() == null && our.getMessage() == null)
                        || (t.getMessage() != null
                                && t.getMessage().equals(our.getMessage()))) {
                    m_anticipated.remove(our);
                    foundMatch = true;
                    break;
                }
            }
        }
        
        if (!foundMatch) {
            throwableFailedMatch(t);
        }
    }
    
    private void throwableFailedMatch(Throwable t) {
        if (m_failFast) {
            fail("Received an unexpected Exception: " + t.toString(), t);
        } else {
            m_unanticipated.add(t);
        }
    }

    /**
     * Set the failFast value for this anticipator.  This controls what happens
     * when an unanticipation throwable is received #receiveThrowable(Throwable).
     *
     * @param failFast when set to true (the default), an unanticipated throwable
     *        will cause #receiveThrowable(Throwable) to call
     *        junit.framework.Assert#fail().  When set to false, the error will
     *        be delayed until #verifyAnticipated() is called.
     * @throws junit.framework.AssertionFailedError if failFast is being changed from false to
     *         true and one or more unanticipated exceptions have been received.
     */
    public void setFailFast(boolean failFast) throws AssertionFailedError {
        if (m_failFast == false && failFast == true
                && m_unanticipated.size() > 0) {
            fail("failFast is being changed from false to true and unanticipated "
                 + "exceptions have been received:\n" + listUnanticipated());
        }
        m_failFast = failFast;
    }

    /**
     * Returns the failFast value for this anticipator.
     *
     * @return failFast value.  See #setFailFast(boolean) for details.
     */
    public boolean isFailFast() {
        return m_failFast;
    }
    
    /**
     * Reset the class back to its initial state as if it had just been
     * instantiated.
     */
    public void reset() {
        init();
    }
    
    /**
     * Perform after-test verification that all anticipated java.lang.Throwable's
     * have been seen and that no unanticipated java.lang.Throwable's have been
     * seen.
     *
     * @throws junit.framework.AssertionFailedError if one or more anticipated Throwables were
     *         not received or one or more unanticipated Throwables were received.
     */
    public void verifyAnticipated() throws AssertionFailedError {
        StringBuffer error = new StringBuffer();
        
        if (m_anticipated.size() != 0) {
            error.append("Anticipated list is non-zero (has "
                         + m_anticipated.size() + " entries):\n");
            error.append(listAnticipated());
        }
        
        if (m_unanticipated.size() != 0) {
            error.append("Unanticipated list is non-zero (has "
                         + m_unanticipated.size() + " entries):\n");
            error.append(listUnanticipated());
        }
        
        if (error.length() > 0) {
            fail(error.toString());
        }
    }
    
    private StringBuffer listAnticipated() {
        return makeList(m_anticipated,
                        "Anticipated but unreceived Throwable: ", "\n");
    }
    
    private StringBuffer listUnanticipated() {
        return makeList(m_unanticipated, "Unanticipated Throwable: ", "\n");
    }
    
    private StringBuffer makeList(List<Throwable> list, String before, String after) {
        StringBuffer output = new StringBuffer();
        
        for (Throwable t : list) {
            output.append(before + t.toString() + after);
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            t.printStackTrace(pw);
            output.append(w.getBuffer());
            output.append("\n");
        }
        
        return output;
    }
    
    /**
     * Generate an AssertionFailedError with the specified cause.
     *
     * @param message error message
     * @param t java.lang.Throwable that caused this error to be thrown
     * @throws junit.framework.AssertionFailedError always thrown.  Generated based on the
     * parameters passed.
     */
    public void fail(String message, Throwable t) throws AssertionFailedError {
        AssertionFailedError error = new AssertionFailedError(message);
        error.initCause(t);
        throw error;
    }

}

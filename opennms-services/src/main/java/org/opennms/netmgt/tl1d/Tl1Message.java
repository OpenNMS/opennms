/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tl1d;

import java.util.Date;

/**
 * Abstraction for generic TL1 Messages.  Must generic methods are used to populate
 * OpenNMS Event fields.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public abstract class Tl1Message {
    
    /** Constant <code>INPUT=1</code> */
    public static final int INPUT = 1;
    /** Constant <code>OUTPUT=2</code> */
    public static final int OUTPUT = 2;
    /** Constant <code>ACKNOWLEDGEMENT=3</code> */
    public static final int ACKNOWLEDGEMENT = 3;
    /** Constant <code>AUTONOMOUS=4</code> */
    public static final int AUTONOMOUS = 4;

    private Date m_timestamp;
    private String m_rawMessage;
    private String m_host;
    
    /**
     * <p>getTimestamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTimestamp() {
        return m_timestamp;
    }

    /**
     * <p>setTimestamp</p>
     *
     * @param timestamp a {@link java.util.Date} object.
     */
    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    /**
     * <p>getRawMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRawMessage() {
        return m_rawMessage;
    }

    /**
     * <p>setRawMessage</p>
     *
     * @param rawMessage a {@link java.lang.String} object.
     */
    public void setRawMessage(String rawMessage) {
        this.m_rawMessage = rawMessage;
    }

    /**
     * <p>getHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return m_host;
    }

    /**
     * <p>setHost</p>
     *
     * @param host a {@link java.lang.String} object.
     */
    public void setHost(String host) {
        m_host = host;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "Message from: "+m_host+"\n"+m_rawMessage;
    }
        
}

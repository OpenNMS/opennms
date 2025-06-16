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

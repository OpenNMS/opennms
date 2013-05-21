/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.opennms.netmgt.model.PollStatus;

/**
 * <p>PassiveStatusValue class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PassiveStatusValue {
    
    private PassiveStatusKey m_key;
    private PollStatus m_status;
    
    /**
     * <p>Constructor for PassiveStatusValue.</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param status a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PassiveStatusValue(String nodeLabel, String ipAddr, String serviceName, PollStatus status) {
        this(new PassiveStatusKey(nodeLabel, ipAddr, serviceName), status);
    }
    
    /**
     * <p>Constructor for PassiveStatusValue.</p>
     *
     * @param key a {@link org.opennms.netmgt.config.PassiveStatusKey} object.
     * @param status a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PassiveStatusValue(PassiveStatusKey key, PollStatus status) {
        m_key = key;
        m_status = status;
    }
    
    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus getStatus() {
        return m_status;
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public void setStatus(PollStatus status) {
        m_status = status;
    }
    
    /**
     * <p>getKey</p>
     *
     * @return a {@link org.opennms.netmgt.config.PassiveStatusKey} object.
     */
    public PassiveStatusKey getKey() {
        return m_key;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getKey().toString()+" -> "+m_status;
    }

    
}

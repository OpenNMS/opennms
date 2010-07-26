//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
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
    public String toString() {
        return getKey().toString()+" -> "+m_status;
    }

    
}

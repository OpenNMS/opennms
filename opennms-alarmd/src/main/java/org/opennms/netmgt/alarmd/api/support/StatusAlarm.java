/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api.support;


import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.alarmd.api.Alarm;

/**
 * An event used to represent the Status of Alarm Sync
 * FIXME: Probably not implemented completely or very well, fix with tests and real use cases.
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
public class StatusAlarm implements Alarm {

    public static StatusAlarm createStartMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdStart");
        try {
                a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {
    
        }
        return a;
    }

    public static StatusAlarm createStopMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdStop");
        try {
                a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {
    
        }
        return a;
    }

    public static StatusAlarm createSyncLostMessage() {
        StatusAlarm a = new StatusAlarm("uei.opennms.org/external/nnm/opennmsdSyncLost");
        a.setPreserved(true);
        
        try {
            a.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception excp) {

        }
        
        return a;
    }

  
    
    private String m_uei;
    private Date m_timeStamp;
    private boolean m_preserved;
    private String m_agentAddress;

    // This fields caches the resolved agentAddress for using in forwarding
    private String m_nodeLabel;

    
    public String getAgentAddress() {
                return m_agentAddress;
        }

        public void setAgentAddress(String address) {
                m_agentAddress = address;
        }

        public String getNodeLabel() {
                return m_nodeLabel;
        }

        public void setNodeLabel(String label) {
                m_nodeLabel = label;
        }

        public StatusAlarm(String uei) {
        this(uei, new Date());
    }

    public StatusAlarm(String uei, Date timeStamp) {
        m_uei = uei;
        m_timeStamp = timeStamp;
    }
    
    public String getUei() {
        return m_uei;
    }
    
    public Date getTimeStamp() {
        return m_timeStamp;
    }

    public void setTimeStamp(Date timestamp) {
        m_timeStamp = timestamp;
    }

    public boolean isPreserved() {
        return m_preserved;
    }
    
    public void setPreserved(boolean preserved) {
        m_preserved = preserved;
    }

    @Override
    public Integer getId() {
        // TODO Auto-generated method stub
        return null;
    }

}
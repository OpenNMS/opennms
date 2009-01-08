/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

public class NodeScanSchedule {
    private int m_nodeId;
    private String m_foreignSource;
    private long m_initialDelay;
    private long m_scanInterval;
    /**
     * @return the nodeId
     */
    public int getNodeId() {
        return m_nodeId;
    }
    /**
     * @param nodeId the nodeId to set
     */
    public void setNodeId(int nodeId) {
        this.m_nodeId = nodeId;
    }
    /**
     * @return the foreignSource
     */
    public String getForeignSource() {
        return m_foreignSource;
    }
    /**
     * @param foreignSource the foreignSource to set
     */
    public void setForeignSource(String foreignSource) {
        this.m_foreignSource = foreignSource;
    }
    /**
     * @return the initialDelay
     */
    public long getInitialDelay() {
        return m_initialDelay;
    }
    /**
     * @param initialDelay the initialDelay to set
     */
    public void setInitialDelay(long initialDelay) {
        this.m_initialDelay = initialDelay;
    }
    /**
     * @return the scanInterval
     */
    public long getScanInterval() {
        return m_scanInterval;
    }
    /**
     * @param scanInterval the scanInterval to set
     */
    public void setScanInterval(long scanInterval) {
        this.m_scanInterval = scanInterval;
    }
    
}
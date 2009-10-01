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
package org.opennms.netmgt.provision;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * SimplerQueuedProvisioningAdapter
 *
 * @author brozow
 */
public abstract class SimplerQueuedProvisioningAdapter extends SimpleQueuedProvisioningAdapter {
    
    private String m_name;
    private long m_delay = 1;
    private TimeUnit m_timeUnit = TimeUnit.SECONDS;
    
    public SimplerQueuedProvisioningAdapter(String name) {
        m_name = name;
    }
    
    public void setTimeUnit(TimeUnit timeUnit) {
        m_timeUnit = timeUnit;
    }
    
    public void setDelay(long delay) {
        m_delay = delay;
    }
    
    @Override
    public String getName() {
        return m_name;
    }
    
    @Override
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
        return new AdapterOperationSchedule(m_delay, 0, 1, m_timeUnit);
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    @Override
    public void processPendingOperationForNode(final AdapterOperation op) throws ProvisioningAdapterException {
        log().info("processPendingOperationForNode: Handling Operation: "+op);
        
        switch (op.getType()) {
        case ADD:
            doAddNode(op.getNodeId());
            break;
        case UPDATE:
            doUpdateNode(op.getNodeId());
            break;
        case DELETE:
            doDeleteNode(op.getNodeId());
            break;
        case CONFIG_CHANGE:
            doNotifyConfigChange(op.getNodeId());
            break;
        }
    }
    
    @Override
    public void init() {
        assertNotNull(m_timeUnit, "timeUnit must be set");
    }
    
    private void assertNotNull(Object o, String msg) {
        assertTrue(o != null, msg);
    }
    
    private void assertTrue(boolean b, String m) {
        if (!b) throw new IllegalStateException(m);
    }

    public void doAddNode(int nodeid) {}
    
    public void doUpdateNode(int nodeid) {}
    
    public void doDeleteNode(int nodeid) {}
    
    public void doNotifyConfigChange(int nodeid) {}
    
    
    private static Category log() {
        return ThreadCategory.getInstance(LinkProvisioningAdapter.class);
    }


}

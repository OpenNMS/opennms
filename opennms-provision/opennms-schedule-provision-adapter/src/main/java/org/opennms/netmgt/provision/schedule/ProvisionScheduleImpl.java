/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
package org.opennms.netmgt.provision.schedule;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.ProvisioningAdapterException;
import org.springframework.core.io.Resource;


/**
 * @author Donald Desloge
 *
 */
public class ProvisionScheduleImpl implements ProvisionSchedule {
    
    public static class ScheduleHolder{
        
        private Scheduler m_scheduler;
        private String m_nodeId;
        
        public ScheduleHolder(String schedule, String nodeId) {
            setScheduler(new SchedulerImpl(schedule));
            setNodeId(nodeId);
        }
        
        public Long getNextScheduledInterval() {
            return m_scheduler.getScheduleInterval();
        }
        
        public void setScheduler(Scheduler scheduler) {
            m_scheduler = scheduler;
        }

        public Scheduler getScheduler() {
            return m_scheduler;
        }

        public void setNodeId(String nodeId) {
            this.m_nodeId = nodeId;
        }

        public String getNodeId() {
            return m_nodeId;
        }
        
    }
    
    public static class NodeInfo{
        
    }
    
    private List<ScheduleHolder> m_scheduleQueue = new ArrayList<ScheduleHolder>();
    
    public void init() {
        // TODO Auto-generated method stub
        
    }

    public void scheduleChanged(String foreignSourceId) {
        // TODO Auto-generated method stub
        
    }

    public void setImportResource(Resource resource) {
        // TODO Auto-generated method stub
        
    }

    public void setImportSchedule(String schedule) {
        // TODO Auto-generated method stub
        
    }

    public void setNotifier(Notifier notif) {
        // TODO Auto-generated method stub
        
    }

    public void start() {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    public void addNode(int nodeId) throws ProvisioningAdapterException {
        // TODO Auto-generated method stub
        //addNode to scheduler and get the schedule from the provisioned import  
    }

    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        // TODO Auto-generated method stub
        
    }

    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        // TODO Auto-generated method stub
        
    }

}

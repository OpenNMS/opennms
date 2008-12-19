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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.provision.ProvisioningAdapter;
import org.opennms.netmgt.provision.ProvisioningAdapterException;

/**
 * @author Donald Desloge
 *
 */
public class ScheduleProvisiondAdapter implements ProvisioningAdapter {
    
    public static class Scheduler implements Runnable{
        
        private int m_id;
        
        public void run() {
            System.out.printf("Notifying Provisiond that node: %s needs to be scanned", getId());
            
        }
        
        public void setId(int id) {
            m_id = id;
        }
        
        public int getId() {
            return m_id;
        }
        
        
    }
    
    /**
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    private NodeDao m_nodeDao;
    private ScheduledExecutorService m_scheduler;
    private int m_scheduleInterval;
    private Map<String, ScheduledFuture<?>> m_scheduleQueue = new HashMap<String, ScheduledFuture<?>>();
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(int)
     */
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        //OnmsNode node = m_nodeDao.get(nodeId);
        //Date timestamp = node.getCreateTime();
        Scheduler scheduledTask = new Scheduler();
        scheduledTask.setId(nodeId);
        ScheduledFuture<?> future = m_scheduler.schedule(scheduledTask, 10, SECONDS);
        m_scheduleQueue.put(Integer.toString(nodeId), future);
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(int)
     */
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        ScheduledFuture<?> future = m_scheduleQueue.remove(Integer.toString(nodeId));
        if(future != null) {
            future.cancel(true);
        }

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(int)
     */
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        deleteNode(nodeId);
        addNode(nodeId);
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setScheduler(ScheduledExecutorService schedulder) {
        m_scheduler = schedulder;
    }

    public ScheduledExecutorService getSchedulder() {
        return m_scheduler;
    }

    public void setScheduleInterval(int scheduleInterval) {
        m_scheduleInterval = scheduleInterval;
    }

    public int getScheduleInterval() {
        return m_scheduleInterval;
    }

}

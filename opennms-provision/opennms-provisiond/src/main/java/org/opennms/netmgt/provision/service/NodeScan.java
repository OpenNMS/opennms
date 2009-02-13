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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance;
import org.opennms.netmgt.provision.service.lifecycle.LifeCycleRepository;

public class NodeScan implements Runnable {
    private String m_foreignSource;
    private String m_foreignId;
    private LifeCycleRepository m_lifeCycleRepository;
    private List<Object> m_providers;

    public NodeScan(String foreignSource, String foreignId, LifeCycleRepository lifeCycleRepository, List<Object> providers) {
        m_foreignSource = foreignSource;
        m_foreignId = foreignId;
        m_lifeCycleRepository = lifeCycleRepository;
        m_providers = providers;
    }
    
    private void doNodeScan() throws InterruptedException, ExecutionException {
        LifeCycleInstance doNodeScan = m_lifeCycleRepository.createLifeCycleInstance("nodeScan", m_providers.toArray());
        doNodeScan.setAttribute("nodeScan", this);
        doNodeScan.setAttribute("foreignSource", m_foreignSource);
        doNodeScan.setAttribute("foreignId", m_foreignId);
        
        doNodeScan.trigger();
        
        doNodeScan.waitFor();
    }
    
    public void run() {
        try {
            doNodeScan();
            System.err.println(String.format("Finished Scanning Node %s / %s", m_foreignSource, m_foreignId));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

    ScheduledFuture<?> schedule(ScheduledExecutorService executor, NodeScanSchedule schedule) {
        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(this, schedule.getInitialDelay().getMillis(), schedule.getScanInterval().getMillis(), TimeUnit.MILLISECONDS);
        System.err.println(String.format("SCHEDULE: Created schedule for node %d : %s", schedule.getNodeId(), future));
        return future;
    }

    
}
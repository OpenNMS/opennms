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
 * Modifications:
 * 
 * Created: March 2, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;

/**
 * This class takes the work out of scheduling and queuing calls from the provisioner.  Each provisioning
 * adapter can extend this class for this functionality.  The interface API methods are final so that
 * the child class can not implement them and override the queuing for what would be the point.  (Unless
 * of course we decide that it is worth while to override a subset of the API methods.
 * 
 * To use the class, have your provisioning adapter extend this abstract class.  You see that you must
 * implement abstract methods to compile.
 * 
 * To change the schedule, override the createScheduleForNode method and return a schedule suitable for
 * the node.  In this base class, the same schedule is used for all nodes.
 * 
 * TODO: Add logging
 * TODO: Verify correct Exception handling
 * TODO: Write tests (especially the equals method of the NodeOperation for proper queue handling)
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public abstract class SimpleQueuedProvisioningAdapter implements ProvisioningAdapter {
    
    private final BlockingQueue<AdapterOperation> m_operQueue = new LinkedBlockingQueue<AdapterOperation>();
    private volatile ScheduledExecutorService m_executorService;
    
    protected SimpleQueuedProvisioningAdapter(ScheduledExecutorService executorService) {
        m_executorService = executorService;
    }
    
    protected SimpleQueuedProvisioningAdapter() {
        this(createDefaultSchedulerService());
    }

    private static PausibleScheduledThreadPoolExecutor createDefaultSchedulerService() {
        PausibleScheduledThreadPoolExecutor executorService = new PausibleScheduledThreadPoolExecutor(1);
        
        return executorService;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#getName()
     */
    public abstract String getName();
    
    /**
     * This method is called when the sc
     * Adapters extending this class must implement this method
     */
    public abstract boolean isNodeReady(int nodeId);
    public abstract void processPendingOperationsForNode(List<AdapterOperation> ops) throws ProvisioningAdapterException;
    

    /**
     * Override this method to change the default schedule
     * @param adapterOperationType 
     * @return
     */
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
        return new AdapterOperationSchedule();
    }
    
    final List<AdapterOperation> removeOperationsForNode(Integer nodeId) {

        List<AdapterOperation> ops = new ArrayList<AdapterOperation>();
        Iterator<AdapterOperation> opIter = m_operQueue.iterator();
        while (opIter.hasNext()) {
            AdapterOperation op = (AdapterOperation) opIter.next();
            if (op.getNodeId() == nodeId) {
                ops.add(op);
                m_operQueue.remove(op);
            }
        }
        return ops;
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(int)
     */
    public final void addNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.ADD, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.ADD));
        if (!m_operQueue.contains(op)) {
            m_operQueue.offer(op);
            op.schedule(m_executorService);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(int)
     */
    public final void updateNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.UPDATE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.UPDATE));
        if (!m_operQueue.contains(op)) {
            m_operQueue.offer(op);
            op.schedule(m_executorService);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(int)
     */
    public final void deleteNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.DELETE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.DELETE));
        if (!m_operQueue.contains(op)) {
            m_operQueue.offer(op);
            op.schedule(m_executorService);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#nodeConfigChanged(int)
     */
    public final void nodeConfigChanged(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.CONFIG_CHANGE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.CONFIG_CHANGE));
        if (!m_operQueue.contains(op)) {
            m_operQueue.offer(op);
            op.schedule(m_executorService);
        }
    }

    /**
     * Represents a node operation to be queued and scheduled.
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     *
     */
    class AdapterOperation implements Runnable {
        
        private final Integer m_nodeId;
        private final AdapterOperationType m_type;
        private AdapterOperationSchedule m_schedule;
        private final Date m_createTime;
        
        public AdapterOperation(Integer nodeId, AdapterOperationType type, AdapterOperationSchedule schedule) {
            m_nodeId = nodeId;
            m_type = type;
            m_schedule = schedule;
            m_createTime = new Date();
        }
        
        public Integer getNodeId() {
            return m_nodeId;
        }
        
        public Date getCreateTime() {
            return m_createTime;
        }
        
        public AdapterOperationType getType() {
            return m_type;
        }
        
        public AdapterOperationSchedule getSchedule() {
            return m_schedule;
        }
        
        ScheduledFuture<?> schedule(ScheduledExecutorService executor) {
            ScheduledFuture<?> future = executor.scheduleWithFixedDelay(this, m_schedule.m_initalDelay, m_schedule.m_interval, m_schedule.m_unit);
            return future;
        }
        
        //TODO: Test this behavior with Unit Tests, for sure!
        @Override
        public boolean equals(Object operation) {
            boolean equals = false;
            
            if (this == operation) {
                equals = true;
            }
            
            if (operation == null || (operation.getClass() != this.getClass())) {
                throw new IllegalArgumentException("the Operation Object passed is either null or of the wrong class");
            }
            
            if (this.m_nodeId == ((AdapterOperation)operation).getNodeId() &&
                this.m_type == ((AdapterOperation)operation).getType()) {
                equals = true;
            }
            return equals;
        }
        
        @Override
        public String toString() {
            return "Operation: "+m_type+" on Node: "+m_nodeId;
        }
        
        public void run() {
            AdapterOperation op = m_operQueue.poll();
            
            if (isNodeReady(op.getNodeId())) {
                List<AdapterOperation> ops = removeOperationsForNode(op.getNodeId());
                processPendingOperationsForNode(ops);
            }
        }
    }

    /**
     * Simple class for handling the scheduling bits for an AdapterOperation
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     */
    class AdapterOperationSchedule {
        long m_initalDelay;
        long m_interval;
        TimeUnit m_unit;
        
        public AdapterOperationSchedule(long initalDelay, long interval, TimeUnit unit) {
            m_initalDelay = initalDelay;
            m_interval = interval;
            m_unit = unit;
        }
        
        public AdapterOperationSchedule() {
            this(60, 60, TimeUnit.SECONDS);
        }
        
        public long getInitalDelay() {
            return m_initalDelay;
        }
        
        public long getInterval() {
            return m_interval;
        }
        
        public TimeUnit getUnit() {
            return m_unit;
        }
    }

    /**
     * Since the operations are queued, we need a way of identifying type of provisioning action
     * happened to create the operation.  The adapters will need to know what is the appropriate
     * action to take.
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     */
    static enum AdapterOperationType {
        ADD(1, "Add"),
        UPDATE(2, "Update"),
        DELETE(3, "Delete"),
        CONFIG_CHANGE(4, "Configuration Change");
        
        private static final Map<Integer, AdapterOperationType> m_idMap; 
        private static final List<Integer> m_ids;

        
        private int m_id;
        private String m_label;
        
        static {
            m_ids = new ArrayList<Integer>(values().length);
            m_idMap = new HashMap<Integer, AdapterOperationType>(values().length);
            for (AdapterOperationType operation : values()) {
                m_ids.add(operation.getId());
                m_idMap.put(operation.getId(), operation);
            }
        }

        private AdapterOperationType(int id, String label) {
            m_id = id;
            m_label = label;
        }

        private Integer getId() {
            return m_id;
        }

        @Override
        public String toString() {
            return m_label;
        }
        
        public static AdapterOperationType get(int id) {
            if (m_idMap.containsKey(id)) {
                return m_idMap.get(id);
            } else {
                throw new IllegalArgumentException("Cannot create AdapterOperation from unknown ID " + id);
            }
        }
    }

    
    public BlockingQueue<AdapterOperation> getOperQueue() {
        return m_operQueue;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        m_executorService = executorService;
    }

    public ScheduledExecutorService getExecutorService() {
        return m_executorService;
    }
}

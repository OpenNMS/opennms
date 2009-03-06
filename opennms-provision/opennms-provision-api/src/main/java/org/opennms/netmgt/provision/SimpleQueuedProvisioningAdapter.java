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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;

/**
 * This class takes the work out of scheduling and queuing calls from the provisioner.  Each provisioning
 * adapter can extend this class for this functionality.  The ProvisioningAdapter Interface API methods are final so that
 * the child class can not implement them and override the queuing... for what would be the point.
 * 
 * To use this class, have your provisioning adapter extend this abstract class.  You see that you must
 * implement abstract methods.
 * 
 * To change the schedule, override the createScheduleForNode method and return a schedule suitable for
 * the node.  In this base class, the same schedule is used for all nodes.
 * 
 * This class throws away duplicate node/operation tuples.  This way you are guaranteed to only receive one node/operation
 * from the queue, until it is removed from the queue that is.  This is the purpose of the initial delay.  It is suspected
 * that the add/update/delete operations will not have any delay.  Since there is only one thread per adapter, you will get
 * these in the order they're scheduled.  Be sure that adds/updates/deletes use the same schedule to insure the proper
 * order. 
 * 
 * TODO: Add logging
 * TODO: Verify correct Exception handling
 * TODO: Write tests (especially the equals method of the NodeOperation for proper queue handling)
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public abstract class SimpleQueuedProvisioningAdapter implements ProvisioningAdapter {
    
    private final AdapterOperationQueue m_operationQueue = new AdapterOperationQueue();
    
    private volatile PausibleScheduledThreadPoolExecutor m_executorService;
    
    protected SimpleQueuedProvisioningAdapter(PausibleScheduledThreadPoolExecutor executorService) {
        m_executorService = executorService;
    }
    
    protected SimpleQueuedProvisioningAdapter() {
        this(createDefaultSchedulerService());
    }

    //final for now
    private final static PausibleScheduledThreadPoolExecutor createDefaultSchedulerService() {
        PausibleScheduledThreadPoolExecutor executorService = new PausibleScheduledThreadPoolExecutor(1);
        
        return executorService;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#getName()
     */
    public abstract String getName();
    
    /**
     * Adapters extending this class must implement this method
     */
    public abstract boolean isNodeReady(AdapterOperation op);
    public abstract void processPendingOperationForNode(AdapterOperation op) throws ProvisioningAdapterException;
    

    /**
     * Override this method to change the default schedule
     * @param adapterOperationType 
     * @return
     */
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
        return new AdapterOperationSchedule(adapterOperationType);
    }
    
    final List<AdapterOperation> removeOperationsForNode(Integer nodeId) {
        return m_operationQueue.dequeueOperationsForNode(nodeId);
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(int)
     */
    public final void addNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.ADD, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.ADD));
        
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(int)
     */
    public final void updateNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.UPDATE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.UPDATE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(int)
     */
    public final void deleteNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.DELETE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.DELETE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#nodeConfigChanged(int)
     */
    public final void nodeConfigChanged(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.CONFIG_CHANGE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.CONFIG_CHANGE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
    }
    
    class AdapterOperationQueue {
        
        private final ConcurrentHashMap<Integer,LinkedBlockingQueue<AdapterOperation>> m_mappedQueue;
        
        public AdapterOperationQueue() {
            m_mappedQueue = new ConcurrentHashMap<Integer, LinkedBlockingQueue<AdapterOperation>>();
        }
        
        public synchronized boolean enqueOperation(Integer nodeId, AdapterOperation op) {
            
            //TODO: should implement some logic here about what is currently pending in the queue and whether
            //or not to enqueue.  For example, if an update arrives and an add is still in the queue,
            //perhaps we should just drop the update.  Same consideration for delete, but should remove all
            //operations for the node like nothing ever happened.
            
            if (m_mappedQueue.containsKey(nodeId) && m_mappedQueue.get(nodeId).contains(op)) {
                return false;
            } else if (m_mappedQueue.containsKey(nodeId) && !m_mappedQueue.get(nodeId).contains(op)) {
                m_mappedQueue.get(nodeId).offer(op);
                return true;
            } else {
                LinkedBlockingQueue<AdapterOperation> queue = new LinkedBlockingQueue<AdapterOperation>();
                queue.offer(op);
                m_mappedQueue.put(nodeId, queue);
                return true;
            }
        }
        
        public synchronized void enqueOperations(Integer nodeId, Collection<AdapterOperation> ops) {
            for (AdapterOperation op : ops) {
                enqueOperation(nodeId, op);
            }
        }
        
        public synchronized List<AdapterOperation> dequeueOperationsForNode(Integer nodeId) {
            List<AdapterOperation> ops = new ArrayList<AdapterOperation>();
            m_mappedQueue.get(nodeId).drainTo(ops);
            return ops;
        }
        
        public synchronized boolean dequeueOperationForNode(Integer nodeId, AdapterOperation op) {
            return m_mappedQueue.get(nodeId).remove(op);
        }
        
        public synchronized LinkedBlockingQueue<AdapterOperation> getOperationQueueForNode(Integer nodeId) {
            return m_mappedQueue.get(nodeId);
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
        
        /**
         * Schedules this operation
         * 
         * @param executor
         * @param reduceAttempts
         * @return
         *   Returns a future if scheduled.  Returns null if remaining attempts to schedule is < 1
         */
        ScheduledFuture<?> schedule(ScheduledExecutorService executor, boolean reduceAttempts) {
            ScheduledFuture<?> future = null;
            if (reduceAttempts) {
                if (getSchedule().getAttemptsRemainingAndDecrement() > 0) {
                    future = executor.schedule(this, m_schedule.getInitialDelay(), m_schedule.getUnit());
                }
            } else {
                future = executor.schedule(this, m_schedule.getInitialDelay(), m_schedule.getUnit());
            }
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
            
            if (isNodeReady(this)) {
                m_operationQueue.dequeueOperationForNode(m_nodeId, this);
                try {
                    processPendingOperationForNode(this);
                } catch (ProvisioningAdapterException pae) {
                    //reschedule if the adapter throws a provisioning adapter exception
                    schedule(getExecutorService(), true);
                }
            } else {
                schedule(getExecutorService(), false);
            }
        }
    }

    /**
     * Simple class for handling the scheduling bits for an AdapterOperation
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     */
    class AdapterOperationSchedule {
        long m_initialDelay;
        long m_interval;
        int m_attemptsRemaining;  //never set this to 0, it will never schedule
        TimeUnit m_unit;
        
        public AdapterOperationSchedule(long initialDelay, long interval, int attempts, TimeUnit unit) {

            if (attempts < 1) {
                attempts = 1;
            }
            
            m_initialDelay = initialDelay;
            m_interval = interval;
            m_attemptsRemaining = attempts;
            m_unit = unit;
        }
        
        public AdapterOperationSchedule() {
            this(60, 60, 1, TimeUnit.SECONDS);
        }
        
        /**
         * This constructor changes the initial delay for config change events to 1 hour
         * @param type
         */
        public AdapterOperationSchedule(AdapterOperationType type) {
            this(60, 60, 1, TimeUnit.SECONDS);
            if (type == AdapterOperationType.CONFIG_CHANGE) {
                m_initialDelay = 3600;
            }
        }
        
        public long getInitialDelay() {
            return m_initialDelay;
        }
        
        public long getInterval() {
            return m_interval;
        }
        
        public TimeUnit getUnit() {
            return m_unit;
        }
        
        public int getAttemptsRemaining() {
            return m_attemptsRemaining;
        }
        
        public int getAttemptsRemainingAndDecrement() {
            int currentAttemptsRemaining = m_attemptsRemaining--;
            return currentAttemptsRemaining;
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

    
    PausibleScheduledThreadPoolExecutor getExecutorService() {
        return m_executorService;
    }

    AdapterOperationQueue getOperationQueue() {
        return m_operationQueue;
    }
}

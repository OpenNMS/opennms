/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes the work out of scheduling and queuing calls from the provisioner.  Each provisioning
 * adapter can extend this class for this functionality.  The ProvisioningAdapter Interface API methods are final so that
 * the child class cannot implement them and override the queuing... for what would be the point.
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
 * @version $Id: $
 */
public abstract class SimpleQueuedProvisioningAdapter implements ProvisioningAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleQueuedProvisioningAdapter.class);
    
    private final AdapterOperationQueue m_operationQueue = new AdapterOperationQueue();
    
    private volatile PausibleScheduledThreadPoolExecutor m_executorService;
    
    /**
     * <p>Constructor for SimpleQueuedProvisioningAdapter.</p>
     *
     * @param executorService a {@link org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor} object.
     */
    protected SimpleQueuedProvisioningAdapter(PausibleScheduledThreadPoolExecutor executorService) {
        m_executorService = executorService;
    }
    
    /**
     * <p>Constructor for SimpleQueuedProvisioningAdapter.</p>
     */
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
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getName();
    
    /**
     * Adapters extending this class must implement this method.
     *
     * This method is called in the run method of an operation to insure that the adapter is ready
     * for the operation to run for the associated node.  The adapter is responsible for setting the schedule, however,
     * something could have altered the state of readiness for the provisioning system in the meantime.  If this method
     * returns false, the operation is rescheduled with the and the attempts remaining on the operation are not
     * decremented.
     *
     * @param op a {@link org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation} object.
     * @return a boolean representing the state of readiness from the underlying system integrated by the
     *         implementing adapter.
     */
    public abstract boolean isNodeReady(AdapterOperation op);
    
    /**
     * The class implements the API and therefore the concrete class implements this method to handle
     * dequeued operations. The concrete implementation should check the operation type to derive the
     * its behavior.
     *
     * @param op a {@link org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation} object.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    protected abstract void processPendingOperationForNode(AdapterOperation op) throws ProvisioningAdapterException;
    

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
    /** {@inheritDoc} 
     * @return */
    @Override
    public final ScheduledFuture<?> addNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.ADD, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.ADD));
        
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            return op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(int)
     */
    /** {@inheritDoc} */
    @Override
    public final ScheduledFuture<?> updateNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.UPDATE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.UPDATE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            return op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(int)
     */
    /** {@inheritDoc} */
    @Override
    public final ScheduledFuture<?> deleteNode(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.DELETE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.DELETE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#nodeConfigChanged(int)
     */
    /** {@inheritDoc} */
    @Override
    public final ScheduledFuture<?> nodeConfigChanged(int nodeId) {
        AdapterOperation op = new AdapterOperation(Integer.valueOf(nodeId), AdapterOperationType.CONFIG_CHANGE, 
                                                   createScheduleForNode(nodeId, AdapterOperationType.CONFIG_CHANGE));
        if (m_operationQueue.enqueOperation(nodeId, op)) {
            op.schedule(m_executorService, true);
        } else {
            //TODO: log something
        }
        return null;
    }
    
    /**
     * (non-Javadoc)
     *
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#init()
     *
     * Override this implementation if needed.
     */
    @Override
    public void init() {
        
    }
    
    
    public static class AdapterOperationQueue {
        
        private final List<AdapterOperationQueueListener> m_listeners = new ArrayList<AdapterOperationQueueListener>();
        
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
            } else {
                if (m_mappedQueue.containsKey(nodeId) && !m_mappedQueue.get(nodeId).contains(op)) {
                    m_mappedQueue.get(nodeId).offer(op);
                } else {
                    LinkedBlockingQueue<AdapterOperation> queue = new LinkedBlockingQueue<AdapterOperation>();
                    queue.offer(op);
                    m_mappedQueue.put(nodeId, queue);
                }
                synchronized(m_listeners) {
                    for (AdapterOperationQueueListener listener : m_listeners) {
                        listener.onEnqueueOperation(op);
                    }
                }
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
            synchronized(m_listeners) {
                for (AdapterOperation op : ops) {
                    for (AdapterOperationQueueListener listener : m_listeners) {
                        listener.onDequeueOperation(op);
                    }
                }
            }
            return ops;
        }
        
        public synchronized boolean dequeueOperationForNode(Integer nodeId, AdapterOperation op) {
            boolean retval = m_mappedQueue.get(nodeId).remove(op); 
            synchronized(m_listeners) {
                for (AdapterOperationQueueListener listener : m_listeners) {
                    listener.onDequeueOperation(op);
                }
            }
            return retval;
        }
        
        public synchronized LinkedBlockingQueue<AdapterOperation> getOperationQueueForNode(Integer nodeId) {
            return m_mappedQueue.get(nodeId);
        }
        
        public void addListener(AdapterOperationQueueListener listener) {
            synchronized(m_listeners) {
                m_listeners.add(listener);
            }
        }
        
        public void removeListener(AdapterOperationQueueListener listener) {
            synchronized(m_listeners) {
                m_listeners.add(listener);
            }
        }
        
        public List<AdapterOperationQueueListener> getListeners() {
            synchronized(m_listeners) {
                List<AdapterOperationQueueListener> retval = new ArrayList<AdapterOperationQueueListener>();
                for (AdapterOperationQueueListener listener : m_listeners) {
                    retval.add(listener);
                }
                return retval;
            }
        }
    }

    public interface AdapterOperationQueueListener {
        void onEnqueueOperation(AdapterOperation op);
        void onDequeueOperation(AdapterOperation op);
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
            
            if (m_nodeId == ((AdapterOperation)operation).getNodeId() &&
                m_type == ((AdapterOperation)operation).getType()) {
                equals = true;
            }
            return equals;
        }
        
        @Override
        public String toString() {
            return "Operation: "+m_type+" on Node: "+m_nodeId;
        }
        
        @Override
        public void run() {
            try {
                if (isNodeReady(this)) {
                    m_operationQueue.dequeueOperationForNode(m_nodeId, this);
                    // Synchronize here so that we can signal any interested classes that this
                    // operation is being executed
                    synchronized(this) {
                        try {
                            processPendingOperationForNode(this);
                        } catch (ProvisioningAdapterException e) {
                            LOG.warn("Exception thrown during adapter queuing, rescheduling: {}", e.getMessage(), e);
                            //reschedule if the adapter throws a provisioning adapter exception
                            schedule(getExecutorService(), true);
                        } finally {
                            this.notifyAll();
                        }
                    }
                } else {
                    schedule(getExecutorService(), false);
                }
            } catch (Throwable e) {
                LOG.error("Unexpected exception during node operation: {}", e.getMessage(), e);
            }
        }
    }

    
    /**
     * Simple class for handling the scheduling bits for an AdapterOperation
     * 
     * @author <a href="mailto:david@opennms.org">David Hustace</a>
     */
    static class AdapterOperationSchedule {
        private static final int DEFAULT_ATTEMPTS = 1;
		private static final int DEFAULT_INTERVAL = 60;
		private static final int DEFAULT_INITIAL_DELAY = 300;
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
            this(DEFAULT_INITIAL_DELAY, DEFAULT_INTERVAL, DEFAULT_ATTEMPTS, TimeUnit.SECONDS);
        }
        
        /**
         * This constructor changes the initial delay for configuration change events to 1 hour
         * @param type
         */
        public AdapterOperationSchedule(AdapterOperationType type) {
            this(DEFAULT_INITIAL_DELAY, DEFAULT_INTERVAL, DEFAULT_ATTEMPTS, TimeUnit.SECONDS);
            if (type == AdapterOperationType.CONFIG_CHANGE) {
                m_initialDelay = 3600;
                m_interval = 600;
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
        
        @Override
        public String toString() {
        	StringBuilder sb = new StringBuilder();
        	sb.append("AdapterOperationSchedule; Initial delay: ") ;sb.append(m_initialDelay);
        	sb.append(", Interval: "); sb.append(m_interval);
        	sb.append(", Attempts: "); sb.append(m_attemptsRemaining);
        	sb.append(", Units: "); sb.append(m_unit);
        	return sb.toString();
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

    public AdapterOperationQueue getOperationQueue() {
        return m_operationQueue;
    }
}

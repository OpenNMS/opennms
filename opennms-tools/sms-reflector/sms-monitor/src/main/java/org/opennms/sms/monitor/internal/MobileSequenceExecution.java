/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.SequenceTask;
import org.opennms.core.tasks.Task;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.MobileSequenceTransaction;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

/**
 * MobileSequenceExecution
 *
 * @author brozow
 * @version $Id: $
 */
public class MobileSequenceExecution {
    
    // Use a LinkedHashMap to ensure transaction latencies are ordered.
    private Map<String, Number> m_responseTimes = new LinkedHashMap<String,Number>();
    private Long m_startTime;
    private SequenceTask m_task;
    private MobileSequenceConfig m_sequenceConfig;
    private List<MobileTransactionExecution> m_transactionExecutions = new ArrayList<MobileTransactionExecution>();

    /**
     * <p>Constructor for MobileSequenceExecution.</p>
     *
     * @param sequenceConfig a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
     */
    public MobileSequenceExecution(MobileSequenceConfig sequenceConfig) {
        m_sequenceConfig = sequenceConfig;
        
        for(MobileSequenceTransaction transaction : sequenceConfig.getTransactions()) {
            m_transactionExecutions.add(new MobileTransactionExecution(transaction));
        }
        
    }
    
    /**
     * <p>getSequenceConfig</p>
     *
     * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
     */
    public MobileSequenceConfig getSequenceConfig() {
        return m_sequenceConfig;
    }
    
    /**
     * <p>getTransactionExecutions</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<MobileTransactionExecution> getTransactionExecutions() {
        return m_transactionExecutions;
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getStartTime() {
        return m_startTime;
    }
    
    /**
     * <p>setStartTime</p>
     *
     * @param startTime a {@link java.lang.Long} object.
     */
    public void setStartTime(Long startTime) {
        m_startTime = startTime;
    }
    
    /**
     * <p>getResponseTimes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Number> getResponseTimes() {
        return m_responseTimes;
    }

    /**
     * <p>end</p>
     */
    public void end() {
        long end = System.currentTimeMillis();
    	getResponseTimes().put("response-time", Long.valueOf(end - (long) getStartTime()));
    }

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    public void waitFor() throws InterruptedException, ExecutionException {

        Task task = getTask();
        if (task == null) {
            throw new IllegalStateException("attempting to wait for the sequence to complete but the sequence has never been started!");
        }
        task.waitFor();
        
        end();
    }

    /**
     * <p>getTask</p>
     *
     * @return a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    public SequenceTask getTask() {
        return m_task;
    }

    /**
     * <p>setTask</p>
     *
     * @param task a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    public void setTask(SequenceTask task) {
        m_task = task;
    }

    /**
     * <p>updateResults</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @throws java.lang.Throwable if any.
     */
    public void updateResults(MobileSequenceSession session) throws Throwable {
        for(MobileTransactionExecution execution : getTransactionExecutions()) {
            MobileSequenceTransaction transaction = execution.getTransaction();
            if (execution.getError() != null) {
                throw execution.getError();
            }
            getResponseTimes().put(transaction.getLabel(session), execution.getLatency());
        }
    }

    /**
     * <p>start</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    public void start(MobileSequenceSession session, DefaultTaskCoordinator coordinator) {
        
        setTask(coordinator.createSequence().get());

        for (MobileTransactionExecution execution : getTransactionExecutions()) {
            getTask().add(createAsync(session, execution), null);
        }
    
        setStartTime(System.currentTimeMillis());
        
        getTask().schedule();
    }

    private Async<MobileMsgResponse> createAsync(final MobileSequenceSession session, final MobileTransactionExecution execution) {
        return new Async<MobileMsgResponse>() {
            @Override
            public void submit(Callback<MobileMsgResponse> cb) {
                if (hasFailed()) {
                	cb.complete(null);
                } else {
                    execution.sendRequest(session, cb);
                }
            }
        };
    }

    private boolean hasFailed() {
        for (MobileTransactionExecution execution : getTransactionExecutions()) {
            if (execution.getError() != null) {
                return true;
            }
        }
        return false;
    }

}

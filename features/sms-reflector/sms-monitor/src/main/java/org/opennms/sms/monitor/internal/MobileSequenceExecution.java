/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
 */
public class MobileSequenceExecution {
    
    // Use a LinkedHashMap to ensure transaction latencies are ordered.
    private Map<String, Number> m_responseTimes = new LinkedHashMap<String,Number>();
    private Long m_startTime;
    private SequenceTask m_task;
    private MobileSequenceConfig m_sequenceConfig;
    private List<MobileTransactionExecution> m_transactionExecutions = new ArrayList<MobileTransactionExecution>();

    public MobileSequenceExecution(MobileSequenceConfig sequenceConfig) {
        m_sequenceConfig = sequenceConfig;
        
        for(MobileSequenceTransaction transaction : sequenceConfig.getTransactions()) {
            m_transactionExecutions.add(new MobileTransactionExecution(transaction));
        }
        
    }
    
    public MobileSequenceConfig getSequenceConfig() {
        return m_sequenceConfig;
    }
    
    public List<MobileTransactionExecution> getTransactionExecutions() {
        return m_transactionExecutions;
    }

    public Long getStartTime() {
        return m_startTime;
    }
    
    public void setStartTime(Long startTime) {
        m_startTime = startTime;
    }
    
    public Map<String, Number> getResponseTimes() {
        return m_responseTimes;
    }

    public void end() {
        long end = System.currentTimeMillis();
    	getResponseTimes().put("response-time", Long.valueOf(end - (long) getStartTime()));
    }

    public void waitFor() throws InterruptedException, ExecutionException {

        Task task = getTask();
        if (task == null) {
            throw new IllegalStateException("attempting to wait for the sequence to complete but the sequence has never been started!");
        }
        task.waitFor();
        
        end();
    }

    public SequenceTask getTask() {
        return m_task;
    }

    public void setTask(SequenceTask task) {
        m_task = task;
    }

    public void updateResults(MobileSequenceSession session) throws Throwable {
        for(MobileTransactionExecution execution : getTransactionExecutions()) {
            MobileSequenceTransaction transaction = execution.getTransaction();
            if (execution.getError() != null) {
                throw execution.getError();
            }
            getResponseTimes().put(transaction.getLabel(session), execution.getLatency());
        }
    }

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

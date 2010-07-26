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

import org.opennms.core.utils.ThreadCategory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * SimplerQueuedProvisioningAdapter
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class SimplerQueuedProvisioningAdapter extends SimpleQueuedProvisioningAdapter {
    
    private String m_name;
    protected long m_delay = 1;
    protected TimeUnit m_timeUnit = TimeUnit.SECONDS;

    protected TransactionTemplate m_template;

    /**
     * <p>getTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTemplate() {
        return m_template;
    }

    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }

    /**
     * <p>Constructor for SimplerQueuedProvisioningAdapter.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public SimplerQueuedProvisioningAdapter(String name) {
        m_name = name;
    }
    
    /**
     * <p>setTimeUnit</p>
     *
     * @param timeUnit a {@link java.util.concurrent.TimeUnit} object.
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        m_timeUnit = timeUnit;
    }
    
    /**
     * <p>setDelay</p>
     *
     * @param delay a long.
     */
    public void setDelay(long delay) {
        m_delay = delay;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_name;
    }
    
    @Override
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
        return new AdapterOperationSchedule(m_delay, 0, 1, m_timeUnit);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void processPendingOperationForNode(final AdapterOperation op) throws ProvisioningAdapterException {
        if (log().isDebugEnabled()) {
            log().debug("processPendingOperationForNode: " + op.getType() + " for node: " + op.getNodeId() );
        }
        switch (op.getType()) {
        case ADD:
            m_template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {
                    log().debug("processPendingOperationForNode: calling doAddNode() for node: " + op.getNodeId() );
                    doAddNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case UPDATE:
            m_template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {
                    log().debug("processPendingOperationForNode: calling doUpdateNode() for node: " + op.getNodeId() );
                    doUpdateNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case DELETE:
            m_template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {
                    log().debug("processPendingOperationForNode: calling doDeleteNode() for node: " + op.getNodeId() );
                    doDeleteNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case CONFIG_CHANGE:
            m_template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {
                    log().debug("processPendingOperationForNode: calling doNotifyConfigChange() for node: " + op.getNodeId() );
                    doNotifyConfigChange(op.getNodeId());
                    return null;
                }
            });
            break;
        default:
            log().warn("unknown operation: " + op.getType());
        }
    }
    
    /** {@inheritDoc} */
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

    /**
     * <p>doAddNode</p>
     *
     * @param nodeid a int.
     */
    public void doAddNode(int nodeid) {}
    
    /**
     * <p>doUpdateNode</p>
     *
     * @param nodeid a int.
     */
    public void doUpdateNode(int nodeid) {}
    
    /**
     * <p>doDeleteNode</p>
     *
     * @param nodeid a int.
     */
    public void doDeleteNode(int nodeid) {}
    
    /**
     * <p>doNotifyConfigChange</p>
     *
     * @param nodeid a int.
     */
    public void doNotifyConfigChange(int nodeid) {}
    
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(SimplerQueuedProvisioningAdapter.class);
    }
}

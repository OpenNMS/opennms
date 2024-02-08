/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(SimplerQueuedProvisioningAdapter.class);
    
    private String m_name;
    protected long m_delay = 1;
    protected TimeUnit m_timeUnit = TimeUnit.SECONDS;

    protected TransactionTemplate m_template;

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
        LOG.debug("processPendingOperationForNode: {} for node ID: {}", op.getType(), op.getNodeId());
        switch (op.getType()) {
        case ADD:
            m_template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus arg0) {
                    LOG.debug("processPendingOperationForNode: calling doAddNode() for node ID: {}", op.getNodeId());
                    doAddNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case UPDATE:
            m_template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus arg0) {
                    LOG.debug("processPendingOperationForNode: calling doUpdateNode() for node ID: {}", op.getNodeId());
                    doUpdateNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case DELETE:
            m_template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus arg0) {
                    LOG.debug("processPendingOperationForNode: calling doDeleteNode() for node ID: {}", op.getNodeId() );
                    doDeleteNode(op.getNodeId());
                    return null;
                }
            });
            break;
        case CONFIG_CHANGE:
            m_template.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus arg0) {
                    LOG.debug("processPendingOperationForNode: calling doNotifyConfigChange() for node ID: {}", op.getNodeId() );
                    doNotifyConfigChange(op.getNodeId());
                    return null;
                }
            });
            break;
        default:
            LOG.warn("unknown operation: {}", op.getType());
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
}

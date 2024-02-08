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
package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.rrd.QueuingRrdStrategy;

/**
 * <p>Queued class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Queued extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.queued.Queued> implements QueuedMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.queued.Queued.getLoggingCateogy();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "queuedContext";
    }

    private QueuingRrdStrategy getRrdStrategy() {
        return (QueuingRrdStrategy) getDaemon().getRrdStrategy();
    }


    /**
     * <p>getStatsStatus</p>
     *
     * @return a boolean.
     */
    public boolean getStatsStatus() {
        if (getDaemon().getRrdStrategy() instanceof QueuingRrdStrategy) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>getElapsedTime</p>
     *
     * @return a long.
     */
    @Override
    public long getElapsedTime() {
        return System.currentTimeMillis() - getStartTime();
    }

    /**
     * <p>getCreatesCompleted</p>
     *
     * @return a long.
     */
    @Override
    public long getCreatesCompleted() {
        if (getStatsStatus()) {
            return getRrdStrategy().getCreatesCompleted();
        } else {
            return 0;
        }
    }

    /**
     * <p>getTotalOperationsPending</p>
     *
     * @return a long.
     */
    @Override
    public long getTotalOperationsPending() {
        if (getStatsStatus()) {
            return getRrdStrategy().getTotalOperationsPending();
        } else {
            return 0;
        }
    }

    /**
     * <p>getErrors</p>
     *
     * @return a long.
     */
    @Override
    public long getErrors() {
        if (getStatsStatus()) {
            return getRrdStrategy().getErrors();
        } else {
            return 0;
        }
    }

    /**
     * <p>getUpdatesCompleted</p>
     *
     * @return a long.
     */
    @Override
    public long getUpdatesCompleted() {
        if (getStatsStatus()) {
            return getRrdStrategy().getUpdatesCompleted();
        } else {
            return 0;
        }
    }

    /**
     * <p>getPromotionCount</p>
     *
     * @return a long.
     */
    @Override
    public long getPromotionCount() {
        if (getStatsStatus()) {
            return getRrdStrategy().getPromotionCount();
        } else {
            return 0;
        }
    }

    /**
     * <p>getDequeuedItems</p>
     *
     * @return a long.
     */
    @Override
    public long getDequeuedItems() {
        if (getStatsStatus()) {
            return getRrdStrategy().getDequeuedItems();
        } else {
            return 0;
        }
    }

    /**
     * <p>getDequeuedOperations</p>
     *
     * @return a long.
     */
    @Override
    public long getDequeuedOperations() {
        if (getStatsStatus()) {
            return getRrdStrategy().getDequeuedOperations();
        } else {
            return 0;
        }
    }

    /**
     * <p>getEnqueuedOperations</p>
     *
     * @return a long.
     */
    @Override
    public long getEnqueuedOperations() {
        if (getStatsStatus()) {
            return getRrdStrategy().getEnqueuedOperations();
        } else {
            return 0;
        }
    }

    /**
     * <p>getSignificantOpsDequeued</p>
     *
     * @return a long.
     */
    @Override
    public long getSignificantOpsDequeued() {
        if (getStatsStatus()) {
            return getRrdStrategy().getSignificantOpsDequeued();
        } else {
            return 0;
        }
    }

    /**
     * <p>getSignificantOpsEnqueued</p>
     *
     * @return a long.
     */
    @Override
    public long getSignificantOpsEnqueued() {
        if (getStatsStatus()) {
            return getRrdStrategy().getSignificantOpsEnqueued();
        } else {
            return 0;
        }
    }

    /**
     * <p>getSignificantOpsCompleted</p>
     *
     * @return a long.
     */
    @Override
    public long getSignificantOpsCompleted() {
        if (getStatsStatus()) {
            return getRrdStrategy().getSignificantOpsCompleted();
        } else {
            return 0;
        }
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    @Override
    public long getStartTime() {
        if (getStatsStatus()) {
            return getRrdStrategy().getStartTime();
        } else {
            return 0;
        }
    }


}

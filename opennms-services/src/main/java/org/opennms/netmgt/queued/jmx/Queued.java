/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
        return "OpenNMS.Queued";
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

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.rrd.QueuingRrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class Queued extends AbstractSpringContextJmxServiceDaemon implements
QueuedMBean {

	@Override
	protected String getLoggingPrefix() {
		return "OpenNMS.Queued";
	}

	@Override
	protected String getSpringContext() {
		return "queuedContext";
	}

	private QueuingRrdStrategy getRrdStrategy() {
		return (QueuingRrdStrategy) RrdUtils.getStrategy();
	}


	public boolean getStatsStatus() {
		if (RrdUtils.getStrategy() instanceof QueuingRrdStrategy) {
			return true;
		} else {
			return false;
		}
	}
	
	public long getElapsedTime() {
		return System.currentTimeMillis() - getStartTime();
	}


	public long getCreatesCompleted() {
		if (getStatsStatus()) {
			return getRrdStrategy().getCreatesCompleted();
		} else {
			return 0;
		}
	}

	public long getTotalOperationsPending() {
		if (getStatsStatus()) {
			return getRrdStrategy().getTotalOperationsPending();
		} else {
			return 0;
		}
	}

	public long getErrors() {
		if (getStatsStatus()) {
			return getRrdStrategy().getErrors();
		} else {
			return 0;
		}
	}

	public long getUpdatesCompleted() {
		if (getStatsStatus()) {
			return getRrdStrategy().getUpdatesCompleted();
		} else {
			return 0;
		}
	}

	public long getPromotionCount() {
		if (getStatsStatus()) {
			return getRrdStrategy().getPromotionCount();
		} else {
			return 0;
		}
	}
	
	public long getDequeuedItems() {
		if (getStatsStatus()) {
			return getRrdStrategy().getDequeuedItems();
		} else {
			return 0;
		}
	}

	public long getDequeuedOperations() {
		if (getStatsStatus()) {
			return getRrdStrategy().getDequeuedOperations();
		} else {
			return 0;
		}
	}

	public long getEnqueuedOperations() {
		if (getStatsStatus()) {
			return getRrdStrategy().getEnqueuedOperations();
		} else {
			return 0;
		}
	}

	public long getSignificantOpsDequeued() {
		if (getStatsStatus()) {
			return getRrdStrategy().getSignificantOpsDequeued();
		} else {
			return 0;
		}
	}

	public long getSignificantOpsEnqueued() {
		if (getStatsStatus()) {
			return getRrdStrategy().getSignificantOpsEnqueued();
		} else {
			return 0;
		}
	}

	public long getSignificantOpsCompleted() {
		if (getStatsStatus()) {
			return getRrdStrategy().getSignificantOpsCompleted();
		} else {
			return 0;
		}
	}

	public long getStartTime() {
		if (getStatsStatus()) {
			return getRrdStrategy().getStartTime();
		} else {
			return 0;
		}
	}
	
		
}

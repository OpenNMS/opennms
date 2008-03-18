package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.rrd.QueuingRrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;

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

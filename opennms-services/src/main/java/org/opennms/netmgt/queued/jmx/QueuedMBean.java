package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

public interface QueuedMBean extends BaseOnmsMBean {
	public long getElapsedTime();
	public long getTotalOperationsPending();
	public long getSignificantOpsCompleted();
	public long getCreatesCompleted();
	public long getUpdatesCompleted();
	public long getErrors();
	public long getPromotionCount();
	public long getSignificantOpsEnqueued();
	public long getSignificantOpsDequeued();
	public long getEnqueuedOperations();
	public long getDequeuedOperations();
	public long getDequeuedItems();
	public long getStartTime();

}

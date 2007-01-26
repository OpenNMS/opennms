package org.opennms.netmgt.vulnscand;

public interface ScheduleTrigger {

	public abstract boolean isScheduled();

	public abstract void setScheduled(boolean newScheduled);

	public abstract boolean isTimeForRescan();

	public abstract Object getJob();

}
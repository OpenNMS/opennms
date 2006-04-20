package org.opennms.netmgt.collectd;

public interface ScheduledOutagesDao {

	public abstract OnmsOutageCalendar get(String outageName);

}

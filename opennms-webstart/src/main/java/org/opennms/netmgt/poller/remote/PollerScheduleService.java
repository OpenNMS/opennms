package org.opennms.netmgt.poller.remote;

import java.text.ParseException;

public interface PollerScheduleService {
	
	MonitorServicePollDetails[] getServicePollDetails() throws ParseException;

}

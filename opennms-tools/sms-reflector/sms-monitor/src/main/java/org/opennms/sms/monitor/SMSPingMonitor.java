package org.opennms.sms.monitor;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;

@Distributable(DistributionContext.DAEMON)
final public class SMSPingMonitor extends IPv4Monitor {

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		// TODO Auto-generated method stub
		return null;
	}
}
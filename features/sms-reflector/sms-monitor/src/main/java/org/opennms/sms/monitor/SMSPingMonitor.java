package org.opennms.sms.monitor;

import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.sms.ping.PingConstants;
import org.opennms.sms.ping.SmsPinger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Distributable(DistributionContext.DAEMON)
final public class SMSPingMonitor extends IPv4Monitor {
	Phonebook phonebook = new PropertyPhonebook();

	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);
		BeanUtils.getFactory("mobileMessagePollerContext", ClassPathXmlApplicationContext.class);
	}

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
        long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
        Long rtt = null;
        
		String phoneNumber = null;
		try {
			phoneNumber = phonebook.getTargetForAddress(svc.getIpAddr());
		} catch (PhonebookException e) {
			e.printStackTrace();
		}

		if (phoneNumber != null) {
			try {
				rtt = SmsPinger.ping(phoneNumber, timeout, retries);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (rtt != null) {
			return PollStatus.available(rtt.doubleValue());
		} else {
			return PollStatus.unavailable();
		}
	}
}

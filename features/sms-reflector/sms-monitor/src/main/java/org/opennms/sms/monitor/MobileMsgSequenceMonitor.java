package org.opennms.sms.monitor;


import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.SequenceConfigFactory;
import org.opennms.sms.monitor.internal.config.SequenceException;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.sms.ping.PingConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Distributable(DistributionContext.DAEMON)
final public class MobileMsgSequenceMonitor extends IPv4Monitor {
	Phonebook phonebook = new PropertyPhonebook();

	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);
		BeanUtils.getFactory("mobileMessagePollerContext", ClassPathXmlApplicationContext.class);
	}

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        @SuppressWarnings("unused")
		int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
        @SuppressWarnings("unused")
		long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
        String config = ParameterMap.getKeyedString(parameters, "sequence", "");
        if (config == null || "".equals(config)) {
        	return PollStatus.unavailable("Sequence configuration was empty.  You must specify a 'sequence' parameter in the SMSSequenceMonitor poller configuration!");
        }

        MobileSequenceConfig sequenceConfig = null;
        try {
            SequenceConfigFactory factory = SequenceConfigFactory.getInstance();
			sequenceConfig = factory.getSequenceForXml(config);
		} catch (SequenceException e) {
			log().warn("Unable to parse sequence configuration for host " + svc.getIpAddr(), e);
			return PollStatus.unavailable("unable to read sequence configuration");
		}

		// FIXME: Decide the validity of an empty sequence; is it a failure to configure?  Or passing because no transactions failed?
		if (sequenceConfig.getTransactions() == null || sequenceConfig.getTransactions().size() == 0) {
			log().warn("No transactions were configured for host " + svc.getIpAddr());
			return PollStatus.unavailable("No transactions were configured for host " + svc.getIpAddr());
		}

		try {
			Map<String, Number> responseTimes = MobileMsgSequencer.executeSequence(sequenceConfig);
			PollStatus response = PollStatus.available();
			response.setProperties(responseTimes);
			return response;
		} catch (Throwable e) {
			log().debug("Sequence failed", e);
			return PollStatus.unavailable("Sequence failed: " + e.getLocalizedMessage());
		}
	}
}

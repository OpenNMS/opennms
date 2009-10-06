package org.opennms.sms.monitor;


import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.sms.monitor.internal.SequenceException;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.SequenceConfigFactory;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.sms.ping.PingConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Distributable(DistributionContext.DAEMON)
public class MobileMsgSequenceMonitor extends IPv4Monitor {
    private static Logger log = Logger.getLogger(MobileMsgSequenceMonitor.class);
	private Phonebook phonebook = new PropertyPhonebook();

	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);
		BeanUtils.getFactory("mobileMessagePollerContext", ClassPathXmlApplicationContext.class);
	}

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		if (parameters.get("retry") == null) {
			parameters.put("retry", String.valueOf(PingConstants.DEFAULT_RETRIES));
		}
		if (parameters.get("timeout") == null) {
			parameters.put("timeout", String.valueOf(PingConstants.DEFAULT_TIMEOUT));
		}
        String config = ParameterMap.getKeyedString(parameters, "sequence", "");
        if (config == null || "".equals(config)) {
        	return PollStatus.unavailable("Sequence configuration was empty.  You must specify a 'sequence' parameter in the SMSSequenceMonitor poller configuration!");
        }

        Properties session = new Properties();
        try {
        	// first, transfer anything from the parameters to the session
			for (Map.Entry<String,Object> entry : parameters.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					session.put(entry.getKey(), entry.getValue());
				}
			}
			session.setProperty("recipient", phonebook.getTargetForAddress(svc.getIpAddr()));
		} catch (PhonebookException e) {
			log.warn("Unable to locate recpient phone number for IP address " + svc.getIpAddr(), e);
			return PollStatus.unavailable("Unable to find phone number for IP address " + svc.getIpAddr());
		}

        MobileSequenceConfig sequenceConfig = null;
        try {
            SequenceConfigFactory factory = SequenceConfigFactory.getInstance();
			sequenceConfig = factory.getSequenceForXml(config);
		} catch (SequenceException e) {
			log.warn("Unable to parse sequence configuration for host " + svc.getIpAddr(), e);
			return PollStatus.unavailable("unable to read sequence configuration");
		}

		// FIXME: Decide the validity of an empty sequence; is it a failure to configure?  Or passing because no transactions failed?
		if (sequenceConfig.getTransactions() == null || sequenceConfig.getTransactions().size() == 0) {
			log.warn("No transactions were configured for host " + svc.getIpAddr());
			return PollStatus.unavailable("No transactions were configured for host " + svc.getIpAddr());
		}

		try {
			Map<String, Number> responseTimes = MobileMsgSequencer.executeSequence(sequenceConfig, session);
			PollStatus response = PollStatus.available();
			response.setProperties(responseTimes);
			return response;
		} catch (Throwable e) {
			log.debug("Sequence failed", e);
			return PollStatus.unavailable("Sequence failed: " + e.getLocalizedMessage());
		}
	}
}

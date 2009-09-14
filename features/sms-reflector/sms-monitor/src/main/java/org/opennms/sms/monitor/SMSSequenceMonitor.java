package org.opennms.sms.monitor;

import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.and;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isSms;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isUssd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.MobileSequenceResponse;
import org.opennms.sms.monitor.internal.config.MobileSequenceTransaction;
import org.opennms.sms.monitor.internal.config.SequenceConfigFactory;
import org.opennms.sms.monitor.internal.config.SequenceException;
import org.opennms.sms.monitor.internal.config.SequenceResponseMatcher;
import org.opennms.sms.monitor.internal.config.SmsSequenceRequest;
import org.opennms.sms.monitor.internal.config.SmsSequenceResponse;
import org.opennms.sms.monitor.internal.config.UssdSequenceRequest;
import org.opennms.sms.monitor.internal.config.UssdSequenceResponse;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.sms.ping.PingConstants;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgSequence;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder.MobileMsgTransactionBuilder;

@Distributable(DistributionContext.DAEMON)
final public class SMSSequenceMonitor extends IPv4Monitor {
	Phonebook phonebook = new PropertyPhonebook();

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
			return PollStatus.available();
		}

		MobileMsgSequenceBuilder sequenceBuilder = new MobileMsgSequenceBuilder();
		
		for (MobileSequenceTransaction t : sequenceConfig.getTransactions()) {
			final MobileMsgTransactionBuilder transactionBuilder;
			if (t.getRequest() instanceof SmsSequenceRequest) {
				SmsSequenceRequest req = (SmsSequenceRequest)t.getRequest();
				// FIXME, what happens with the label?  is this the transaction label or the request one?
				transactionBuilder = sequenceBuilder.sendSms(req.getLabel(), req.getRecipient(), req.getText());
			} else if (t.getRequest() instanceof UssdSequenceRequest) {
				UssdSequenceRequest req = (UssdSequenceRequest)t.getRequest();
				transactionBuilder = sequenceBuilder.sendUssd(req.getLabel(), req.getText());
			} else {
				log().warn("Unknown request type: " + t.getRequest());
				return PollStatus.unavailable("Unknown request type: " + t.getRequest());
			}

			for (MobileSequenceResponse r : t.getResponses()) {
				List<MobileMsgResponseMatcher> matchers = new ArrayList<MobileMsgResponseMatcher>();
				for (SequenceResponseMatcher m : r.getMatchers()) {
					matchers.add(m.getMatcher());
				}
				if (r instanceof SmsSequenceResponse) {
					matchers.add(isSms());
				} else if (r instanceof UssdSequenceResponse) {
					matchers.add(isUssd());
				}
				transactionBuilder.expects(and(matchers.toArray(new MobileMsgResponseMatcher[0])));
			}
		}

		ExecutorService executor = Executors.newSingleThreadExecutor();
		DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator(executor);

		long start = System.currentTimeMillis();
		MobileMsgSequence seq = sequenceBuilder.getSequence();
		try {
			Map<String,Long> responseTimes = seq.execute(null, coordinator);
			long end = System.currentTimeMillis();
			PollStatus returnVal = PollStatus.available(Long.valueOf(end - start).doubleValue());
			for (String s : responseTimes.keySet()) {
				returnVal.setProperty(s, responseTimes.get(s));
			}
			return returnVal;
		} catch (Throwable e) {
			log().debug("unable to execute sequence " + seq, e);
			return PollStatus.unavailable(e.getMessage());
		}
	}
}

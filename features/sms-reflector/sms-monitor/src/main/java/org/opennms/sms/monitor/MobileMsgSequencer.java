/**
 * 
 */
package org.opennms.sms.monitor;

import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.and;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isSms;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isUssd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.MobileSequenceResponse;
import org.opennms.sms.monitor.internal.config.MobileSequenceTransaction;
import org.opennms.sms.monitor.internal.config.SequenceResponseMatcher;
import org.opennms.sms.monitor.internal.config.SequenceSessionVariable;
import org.opennms.sms.monitor.internal.config.SmsSequenceRequest;
import org.opennms.sms.monitor.internal.config.SmsSequenceResponse;
import org.opennms.sms.monitor.internal.config.UssdSequenceRequest;
import org.opennms.sms.monitor.internal.config.UssdSequenceResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgSequence;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder.MobileMsgTransactionBuilder;

public class MobileMsgSequencer {
	private static MobileMsgTracker s_tracker;
    private static Logger log = Logger.getLogger(MobileMsgSequencer.class);
	private static DefaultTaskCoordinator s_coordinator;

	public synchronized static void initialize() {
		if (s_coordinator == null) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			s_coordinator = new DefaultTaskCoordinator(executor);
		}
	    if (s_tracker == null) throw new IllegalStateException("MobileMsgSequencer not yet initialized!!"); 
	}

	public synchronized static void setMobileMsgTracker(MobileMsgTracker tracker) {
	    log.debug("Initializing MobileMsgSequencer with tracker " + tracker);
	    s_tracker = tracker;
	}

	public static String substitute(String text, Properties session) {
		if (text == null) {
			return null;
		}
		return PropertiesUtils.substitute(text, session);
	}

	public static Map<String,Number> executeSequence(MobileSequenceConfig sequenceConfig, Properties session) throws Throwable {
		initialize();
		MobileMsgSequenceBuilder sequenceBuilder = new MobileMsgSequenceBuilder();

		for (@SuppressWarnings("unused") SequenceSessionVariable var : sequenceConfig.getSessionVariables()) {
			// FIXME!  gotta implement dynamic session variables
		}

		for (final MobileSequenceTransaction t : sequenceConfig.getTransactions()) {
			final MobileMsgTransactionBuilder transactionBuilder;

			if (t.getGatewayId() != null) {
				sequenceBuilder.setDefaultGatewayId(t.getGatewayId());
			}

			String label = t.getRequest().getLabel();
			if (label == null) label = t.getLabel();
			
			if (t.getRequest() instanceof SmsSequenceRequest) {
				SmsSequenceRequest req = (SmsSequenceRequest)t.getRequest();
				transactionBuilder = sequenceBuilder.sendSms(
					substitute(label, session),
					substitute(req.getGatewayId(), session),
					substitute(req.getRecipient(), session),
					substitute(req.getText(), session)
				);
			} else if (t.getRequest() instanceof UssdSequenceRequest) {
				UssdSequenceRequest req = (UssdSequenceRequest)t.getRequest();
				transactionBuilder = sequenceBuilder.sendUssd(
					substitute(label, session),
					substitute(req.getGatewayId(), session),
					substitute(req.getText(), session)
				);
			} else {
				throw new SequencerException("Unknown request type: " + t.getRequest());
			}
			
			for (MobileSequenceResponse r : t.getResponses()) {
				List<MobileMsgResponseMatcher> matchers = new ArrayList<MobileMsgResponseMatcher>();
				for (SequenceResponseMatcher m : r.getMatchers()) {
					matchers.add(m.getMatcher(session));
				}
				if (r instanceof SmsSequenceResponse) {
					matchers.add(isSms());
				} else if (r instanceof UssdSequenceResponse) {
					matchers.add(isUssd());
				}
				transactionBuilder.expects(and(matchers.toArray(new MobileMsgResponseMatcher[0])));
			}
		}

		MobileMsgSequence seq = sequenceBuilder.getSequence();
		System.err.println("sequence = " + seq);
		long start = System.currentTimeMillis();
		Map<String,Number> responseTimes = seq.execute(s_tracker, s_coordinator);
		long end = System.currentTimeMillis();
		responseTimes.put("response-time", Long.valueOf(end - start));
		return responseTimes;
	}
}
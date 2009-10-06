/**
 * 
 */
package org.opennms.sms.monitor;

import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.and;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isSms;
import static org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers.isUssd;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.opennms.sms.monitor.session.SessionVariableGenerator;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgSequence;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder.MobileMsgTransactionBuilder;

public class MobileMsgSequencer {
	private static MobileMsgTracker s_tracker;
    private static Logger log = Logger.getLogger(MobileMsgSequencer.class);
	private static DefaultTaskCoordinator s_coordinator;
	private static boolean m_initialized = false;

	public synchronized static void initialize() {
		if (!m_initialized) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			s_coordinator = new DefaultTaskCoordinator(executor);
		    if (s_tracker == null) {
		    	throw new IllegalStateException("MobileMsgSequencer not yet initialized!!"); 
		    }
		    m_initialized = true;
		}
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
		sequenceBuilder.setDefaultRetries(Integer.parseInt(session.getProperty("retry", String.valueOf(sequenceBuilder.getDefaultRetries()))));
		sequenceBuilder.setDefaultTimeout(Long.parseLong(session.getProperty("timeout", String.valueOf(sequenceBuilder.getDefaultTimeout()))));

		Map<String,SessionVariableGenerator> sessionGenerators = new HashMap<String,SessionVariableGenerator>();

		// FIXME: use the service registry for this
		for (SequenceSessionVariable var : sequenceConfig.getSessionVariables()) {
			Class<?> c = Class.forName(var.getClassName());

			Class<?> superclass = c.getSuperclass();
			if (superclass != null && superclass.getName().equals("org.opennms.sms.monitor.session.BaseSessionVariableGenerator")) {
				SessionVariableGenerator generator = (SessionVariableGenerator)c.newInstance();
				generator.setParameters(var.getParametersAsMap());
				sessionGenerators.put(var.getName(), generator);
				String value = generator.checkOut();
				if (value == null) {
					value = "";
				}
				session.setProperty(var.getName(), value);
			} else {
				log.warn("unable to get instance of session class: " + c);
			}
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
		if (log.isTraceEnabled()) {
			log.trace("MobileMsgSequence = " + seq);
		}
		try {
			long start = System.currentTimeMillis();
			Map<String,Number> responseTimes = seq.execute(s_tracker, s_coordinator);
			long end = System.currentTimeMillis();
			responseTimes.put("response-time", Long.valueOf(end - start));
			return responseTimes;
		} finally {
			for (Map.Entry<String, SessionVariableGenerator> generator : sessionGenerators.entrySet()) {
				generator.getValue().checkIn(session.getProperty(generator.getKey()));
			}
		}
	}
}

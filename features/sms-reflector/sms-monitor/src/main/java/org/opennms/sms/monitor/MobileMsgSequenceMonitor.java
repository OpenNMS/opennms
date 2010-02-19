package org.opennms.sms.monitor;


import java.util.Map;

import org.apache.log4j.Logger;
import org.opennms.core.tasks.DefaultTaskCoordinator;
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
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.springframework.util.StringUtils;

@Distributable(DistributionContext.DAEMON)
public class MobileMsgSequenceMonitor extends IPv4Monitor {

    public static final String DEFAULT_CONTEXT_NAME = "mobileMessagePollerContext";
    public static final String CONTEXT_KEY = "mobileMessageContextName";

    private static Logger log = Logger.getLogger(MobileMsgSequenceMonitor.class);

    private Phonebook m_phonebook;
	private MobileMsgTracker m_tracker;
	private DefaultTaskCoordinator m_coordinator;

	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);

		String contextName = ParameterMap.getKeyedString(params, CONTEXT_KEY, DEFAULT_CONTEXT_NAME); 

		m_phonebook = BeanUtils.getBean(contextName, "phonebook", Phonebook.class);
		m_tracker = BeanUtils.getBean(contextName, "mobileMsgTracker", MobileMsgTracker.class);
		m_coordinator = BeanUtils.getBean(contextName, "sequenceTaskCoordinator", DefaultTaskCoordinator.class);
	}

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

	    try {

	        String config = ParameterMap.getKeyedString(parameters, "sequence", "");

	        if (!StringUtils.hasLength(config)) {
	            return PollStatus.unavailable("Sequence configuration was empty.  You must specify a 'sequence' parameter in the SMSSequenceMonitor poller configuration!");
	        }

	        SequenceConfigFactory factory = SequenceConfigFactory.getInstance();
	        MobileSequenceConfig sequenceConfig = factory.getSequenceForXml(config);

	        if (!sequenceConfig.hasTransactions()) {
	            log.warn("No transactions were configured for host " + svc.getIpAddr());
	            return PollStatus.unavailable("No transactions were configured for host " + svc.getIpAddr());
	        }

            MobileSequenceSession session = new MobileSequenceSession(parameters, sequenceConfig.getSessionVariables(), m_tracker);

            session.setRecipient(m_phonebook.getTargetForAddress(svc.getIpAddr()));

			session.checkoutVariables();

			Map<String, Number> results = null;
			try {
				results = sequenceConfig.executeSequence(session, m_coordinator);
			} finally {
				session.checkinVariables();
			}

	        Map<String, Number> responseTimes = results;
	        PollStatus response = PollStatus.available();
	        response.setProperties(responseTimes);
	        return response;


	    } catch (PhonebookException e) {
	        log.warn("Unable to locate recpient phone number for IP address " + svc.getIpAddr(), e);
	        return PollStatus.unavailable("Unable to find phone number for IP address " + svc.getIpAddr());
	    } catch (SequenceException e) {
	        log.warn("Unable to parse sequence configuration for host " + svc.getIpAddr(), e);
	        return PollStatus.unavailable("unable to read sequence configuration");
	    } catch (Throwable e) {
	        log.debug("Sequence failed", e);
	        return PollStatus.unavailable("Sequence failed: " + e.getLocalizedMessage());
	    } 
	}
}

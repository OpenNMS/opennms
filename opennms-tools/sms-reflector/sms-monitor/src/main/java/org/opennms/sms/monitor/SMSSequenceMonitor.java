package org.opennms.sms.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.sms.monitor.internal.config.Operation;
import org.opennms.sms.monitor.internal.config.SequenceConfigFactory;
import org.opennms.sms.monitor.internal.config.SequenceException;
import org.opennms.sms.monitor.internal.config.SmsSequence;
import org.opennms.sms.monitor.internal.config.TransactionOperation;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.sms.ping.PingConstants;
import org.opennms.sms.ping.SmsPinger;

@Distributable(DistributionContext.DAEMON)
final public class SMSSequenceMonitor extends IPv4Monitor {
	Phonebook phonebook = new PropertyPhonebook();

	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
        long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
        String config = ParameterMap.getKeyedString(parameters, "sequence", "");
        if (config.isEmpty()) {
        	return PollStatus.unavailable("Sequence configuration was empty.  You must specify a 'sequence' parameter in the SMSSequenceMonitor poller configuration!");
        }

        SmsSequence sequence = null;
        try {
            SequenceConfigFactory factory = SequenceConfigFactory.getInstance();
			sequence = factory.getSequenceForXml(config);
		} catch (SequenceException e) {
			log().warn("Unable to parse sequence configuration for host " + svc.getIpAddr(), e);
			return PollStatus.unavailable("unable to read sequence configuration");
		}

		// FIXME: Decide the validity of an empty sequence; is it a failure to configure?  Or passing because no transactions failed?
		if (sequence.getTransactions() == null) {
			log().warn("No transactions were configured for host " + svc.getIpAddr());
			return PollStatus.available();
		}

		DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator();
		
		/*
		List<Task> tasks = new ArrayList<Task>();
		for (Operation op : sequence.getTransactions()) {
			tasks.add(op.createTask(coordinator));
		}
		*/
		

		/*
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
		*/
		
		return PollStatus.unavailable();
	}
}
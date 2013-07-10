/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.monitor;


import java.util.Map;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.opennms.sms.monitor.internal.SequenceException;
import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.SequenceConfigFactory;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * <p>MobileMsgSequenceMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Distributable(DistributionContext.DAEMON)
public class MobileMsgSequenceMonitor extends AbstractServiceMonitor {

    /** Constant <code>DEFAULT_CONTEXT_NAME="mobileMessagePollerContext"</code> */
    public static final String DEFAULT_CONTEXT_NAME = "mobileMessagePollerContext";
    /** Constant <code>CONTEXT_KEY="mobileMessageContextName"</code> */
    public static final String CONTEXT_KEY = "mobileMessageContextName";

    private static Logger log = LoggerFactory.getLogger(MobileMsgSequenceMonitor.class);

    private Phonebook m_phonebook;
	private MobileMsgTracker m_tracker;
	private DefaultTaskCoordinator m_coordinator;

	/** {@inheritDoc} */
	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);

		String contextName = ParameterMap.getKeyedString(params, CONTEXT_KEY, DEFAULT_CONTEXT_NAME); 

		m_phonebook = BeanUtils.getBean(contextName, "phonebook", Phonebook.class);
		m_tracker = BeanUtils.getBean(contextName, "mobileMsgTracker", MobileMsgTracker.class);
		m_coordinator = BeanUtils.getBean(contextName, "sequenceTaskCoordinator", DefaultTaskCoordinator.class);
	}

	/** {@inheritDoc} */
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
	            log.warn("No transactions were configured for host {}", svc.getIpAddr());
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
	        log.warn("Unable to locate recpient phone number for IP address {}", svc.getIpAddr(), e);
	        return PollStatus.unavailable("Unable to find phone number for IP address " + svc.getIpAddr());
	    } catch (SequenceException e) {
	        log.warn("Unable to parse sequence configuration for host {}", svc.getIpAddr(), e);
	        return PollStatus.unavailable("unable to read sequence configuration");
	    } catch (Throwable e) {
	        log.debug("Sequence failed", e);
	        return PollStatus.unavailable("Sequence failed: " + e.getLocalizedMessage());
	    } 
	}
}

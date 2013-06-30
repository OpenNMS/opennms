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

import org.opennms.core.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.ParameterMap;
import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.phonebook.PropertyPhonebook;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.opennms.sms.ping.PingConstants;
import org.opennms.sms.ping.SmsPinger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>SMSPingMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Distributable(DistributionContext.DAEMON)
final public class SMSPingMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(SMSPingMonitor.class);
	Phonebook phonebook = new PropertyPhonebook();

	/** {@inheritDoc} */
	@Override
	public void initialize(Map<String,Object> params) {
		super.initialize(params);
		BeanUtils.getFactory("mobileMessagePollerContext", ClassPathXmlApplicationContext.class);
	}

	/** {@inheritDoc} */
	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
        long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
        Long rtt = null;
        
		String phoneNumber = null;
		try {
			phoneNumber = phonebook.getTargetForAddress(svc.getIpAddr());
		} catch (final PhonebookException e) {
		    LOG.warn("Unable to get phonebook target for {}", svc.getIpAddr(), e);
		}

		if (phoneNumber != null) {
			try {
				rtt = SmsPinger.ping(phoneNumber, timeout, retries);
			} catch (final Exception e) {
			    LOG.warn("Unable to ping phone number: {}", phoneNumber, e);
			}
		}

		if (rtt != null) {
			return PollStatus.available(rtt.doubleValue());
		} else {
			return PollStatus.unavailable();
		}
	}
}

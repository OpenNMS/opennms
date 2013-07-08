/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Win32ServiceMonitor class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class Win32ServiceMonitor extends SnmpMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(Win32ServiceMonitor.class);
	private static final String SV_SVC_OPERATING_STATE_OID = ".1.3.6.1.4.1.77.1.2.3.1.3";
	private static final String DEFAULT_SERVICE_NAME = "Server";
	
	/** {@inheritDoc} */
        @Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		String serviceName = ParameterMap.getKeyedString(parameters, "service-name", DEFAULT_SERVICE_NAME);
		int snLength = serviceName.length();
		
		StringBuffer serviceOidBuf = new StringBuffer(SV_SVC_OPERATING_STATE_OID);
		serviceOidBuf.append(".").append(Integer.toString(snLength));
		for (byte thisByte : serviceName.getBytes()) {
			serviceOidBuf.append(".").append(Byte.toString(thisByte));
		}
		
		LOG.debug("For Win32 service '{}', OID to check is {}", serviceName, serviceOidBuf);
		
		parameters.put("oid", serviceOidBuf.toString());
		parameters.put("operator", "=");
		parameters.put("operand", "1");
		
		return super.poll(svc, parameters);
	}
}

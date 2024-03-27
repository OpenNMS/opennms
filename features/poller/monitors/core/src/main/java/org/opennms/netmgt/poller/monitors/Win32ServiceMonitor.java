/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
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
		
		final StringBuilder serviceOidBuf = new StringBuilder(SV_SVC_OPERATING_STATE_OID);
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

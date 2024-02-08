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
package org.opennms.core.camel;

import java.util.Objects;
import java.util.Properties;

import org.apache.camel.component.properties.PropertiesComponent;
import org.opennms.core.utils.SystemInfoUtils;

/**
 * This class can be used to generate uniformly-formatted JMS queue names
 * for usage in Java, Spring, or Camel code. In the constructor, location is
 * an optional parameter. If it is null, then the names that are generated
 * will not include a field for the location.
 *
 * Generated queue names are of the form:
 *    $instanceid.$location.$component.$endpoint
 *    $instanceid.$component.$endpoint
 *
 * i.e.:
 *    OpenNMS.HQ.RPC.SNMP
 *    OpenNMS.Syslogd.BroadcastSyslog
 *
 * @author jwhite
 * @author Seth
 */
public class JmsQueueNameFactory {

	private static final String NAME_FORMAT_WITH_LOCATION = "%s.%s.%s.%s";
	private static final String NAME_FORMAT_WITHOUT_LOCATION = "%s.%s.%s";

	private final String m_component;
	private final String m_endpoint;
	private final String m_location;

	public JmsQueueNameFactory(String component, String endpoint, String location) {
		m_component = Objects.requireNonNull(component);
		m_endpoint = Objects.requireNonNull(endpoint);
		m_location = location;
	}

	public JmsQueueNameFactory(String component, String endpoint) {
		this(component, endpoint, null);
	}

	public String getLocation() {
		return m_location;
	}

	public String getComponent() {
		return m_component;
	}

	public String getName() {
		if (m_location == null) {
			return getNameWithoutLocation();
		} else {
			return getNameWithLocation(m_location);
		}
	}

	public String getNameWithoutLocation() {
		return String.format(NAME_FORMAT_WITHOUT_LOCATION, SystemInfoUtils.getInstanceId(), m_component, m_endpoint);
	}

	public String getNameWithLocation(String location) {
		return String.format(NAME_FORMAT_WITH_LOCATION, SystemInfoUtils.getInstanceId(), location, m_component, m_endpoint);
	}

	/**
	 * This method will return both queue name variants in a {@link Properties}
	 * list so that you can easily use it with a {@link PropertiesComponent} in
	 * a Spring context.
	 */
	public Properties getProperties() {
		Properties retval = new Properties();
		retval.setProperty("queueName", getName());
		retval.setProperty("queueNameWithoutLocation", getNameWithoutLocation());
		return retval;
	}
}

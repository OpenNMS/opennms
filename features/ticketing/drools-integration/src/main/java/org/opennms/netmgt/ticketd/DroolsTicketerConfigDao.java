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
package org.opennms.netmgt.ticketd;

import java.io.File;
import java.util.Objects;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DroolsTicketerConfigDao class.</p>
 *
 * @author jwhite
 */
public class DroolsTicketerConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsTicketerConfigDao.class);

	/**
	 * Retrieves the properties defined in the drools-ticketer.properties file.
	 * 
	 */
	private static Configuration getProperties() {
		String propsFile = System.getProperty("opennms.home") + "/etc/drools-ticketer.properties";
		LOG.debug("loading properties from: {}", propsFile);
		Configuration config = null;
		
		try {
			config = new PropertiesConfiguration(propsFile);
		} catch (final ConfigurationException e) {
		    LOG.debug("Unable to load properties from {}", propsFile, e);
		}
	
		return config;
	}
	
	public File getRulesFile() {
		final var properties = Objects.requireNonNull(getProperties());
        return new File(properties.getString("drools-ticketer.rules-file"));
	}
}

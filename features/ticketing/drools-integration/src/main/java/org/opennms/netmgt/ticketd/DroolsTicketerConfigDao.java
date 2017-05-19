/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketd;

import java.io.File;

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
	private Configuration getProperties() {
		String propsFile = new String(System.getProperty("opennms.home") + "/etc/drools-ticketer.properties");
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
		return new File(getProperties().getString("drools-ticketer.rules-file"));
	}
}

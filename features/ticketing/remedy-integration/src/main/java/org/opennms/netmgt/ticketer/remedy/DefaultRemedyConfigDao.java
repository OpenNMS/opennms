/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.remedy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DefaultremedyConfigDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultRemedyConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRemedyConfigDao.class);

	Configuration m_config = null;
	/**
	 * Retrieves the properties defined in the remedy.properties file.
	 * 
	 * @param remedyTicketerPlugin
	 * @return a
	 *         <code>java.util.Properties object containing remedy plugin defined properties
	 */
	
	private Optional<Configuration> getProperties() {
		if (m_config != null) return Optional.of(m_config);
		String propsFile = System.getProperty("opennms.home") + "/etc/remedy.properties";
		
		LOG.debug("loading properties from: {}", propsFile);
		
		Configuration config = null;
		
		try {
			config = new PropertiesConfiguration(propsFile);
		} catch (final ConfigurationException e) {
		    LOG.debug("Unable to load properties from {}", propsFile, e);
		}
		m_config = config;
		return Optional.ofNullable(config);
	
	}
	
	/**
	 * <p>getUserName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUserName() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.username");
		}
		return "";
	}

	
	String getPassword() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.password");
		}
		return "";
	}

	String getAuthentication() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.authentication");
		}
		return "";
	}

	String getLocale() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.locale");
		}
		return "";
	}
	
	String getTimeZone() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.timezone");
		}
		return "";
	}
	
	String getEndPoint() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.endpoint");
		}
		return "";
	}

	String getPortName() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.portname");
		}
		return "";
	}

	String getCreateEndPoint() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.createendpoint");
		}
		return "";
	}

	String getCreatePortName() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.createportname");
		}
		return "";
	}

	List<String> getTargetGroups() {
		List<String> targetGroups=new ArrayList<>();
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent() && properties.get().containsKey("remedy.targetgroups")) {
			targetGroups.addAll(Arrays.asList(properties.get().getString("remedy.targetgroups").split(":")));
		}
		return targetGroups;
	}
	
	String getAssignedGroup() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.assignedgroup");
		}
		return "";
	}

	String getAssignedGroup(String targetGroup) {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent() && properties.get().containsKey("remedy.assignedgroup." + targetGroup))
			return properties.get().getString("remedy.assignedgroup." + targetGroup);

		return getAssignedGroup();
	}

	String getFirstName() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.firstname");
		}
		return "";
	}

	String getLastName() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.lastname");
		}
		return "";
	}

	String getServiceCI() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.serviceCI");
		}
		return "";
	}

	String getServiceCIReconID() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.serviceCIReconID");
		}
		return "";
	}
		
	String getAssignedSupportCompany() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.assignedsupportcompany");
		}
		return "";
	}

	String getAssignedSupportCompany(String targetGroup) {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent() && properties.get().containsKey("remedy.assignedsupportcompany." + targetGroup))
			return properties.get().getString("remedy.assignedsupportcompany." + targetGroup);
		return getAssignedSupportCompany();
	}

	String getAssignedSupportOrganization() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.assignedsupportorganization");
		}
		return "";
	}

	String getAssignedSupportOrganization(String targetGroup) {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent() && properties.get().containsKey("remedy.assignedsupportorganization." + targetGroup))
			return properties.get().getString("remedy.assignedsupportorganization." + targetGroup);
		return getAssignedSupportOrganization();
	}

	String getCategorizationtier1() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.categorizationtier1");
		}
		return "";
	}

	String getCategorizationtier2() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.categorizationtier2");
		}
		return "";
	}

	String getCategorizationtier3() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.categorizationtier3");
		}
		return "";
	}
	
	String getServiceType() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.serviceType");
		}
		return "";
	}
	
	String getReportedSource() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.reportedSource");
		}
		return "";
	}

	String getImpact() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.impact");
		}
		return "";
	}

	String getUrgency() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.urgency");
		}
		return "";
	}
	
	String getResolution() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.resolution");
		}
		return "";
	}

	String getReOpenStatusReason() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.reason.reopen");
		}
		return "";
	}
	
	String getResolvedStatusReason() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.reason.resolved");
		}
		return "";
	}
	
	String getCancelledStatusReason() {
		Optional<Configuration> properties = getProperties();
		if (properties.isPresent()) {
			return properties.get().getString("remedy.reason.cancelled");
		}
		return "";
	}	
}

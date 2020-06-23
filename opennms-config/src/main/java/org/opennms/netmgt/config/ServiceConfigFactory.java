/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;

/**
 * <p>
 * This class is designed to be the main interface between the service
 * configuration information and the users of the information. When initialized
 * the factory loads the configuration from the file system, allowing access to
 * the information by others.
 *
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 */
public final class ServiceConfigFactory implements org.opennms.netmgt.config.api.ServiceConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigFactory.class);

    /**
     * The loaded configuration after is has been unmarshalled
     */
    private ServiceConfiguration m_config;

    /**
     * Private constructor. This constructor used to load the specified
     * configuration file and initialized an instance of the class.
     * 
     * @param configFile
     *            The name of the configuration file.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     */
    public ServiceConfigFactory() {
        reload();
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    @Override
    public synchronized void reload() {
        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
            LOG.debug("ServiceConfigFactory.init: config file path {}", cfgFile.getPath());
            m_config = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);
        } catch (IOException e) {
            // Should never happen
            LOG.error("Could not open configuration file: " + ConfigFileConstants.SERVICE_CONF_FILE_NAME, e);
    }
    }

    /**
     * Returns an array of all the defined configuration information for the
     * <em>Services</em>. If there are no defined services an array of length
     * zero is returned to the caller.
     *
     * @return An array holding a reference to all the Service configuration
     *         instances.
     */
    @Override
    public Service[] getServices() {
        final List<Service> services = new ArrayList<>();
        for (Service s : m_config.getServices()) {
            if (s.isEnabled())
                services.add(s);
        }
        return services.toArray(new Service[services.size()]);
    }
}

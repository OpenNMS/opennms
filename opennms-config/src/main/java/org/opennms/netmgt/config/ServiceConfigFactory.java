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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, Boolean> getServiceNameMap() {
        return m_config.getServices().stream()
                .collect(Collectors.toMap(
                        s -> s.getName().replace("OpenNMS:Name=", ""),
                        s -> s.isEnabled()
                        ));
    }
}

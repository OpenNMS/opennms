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
package org.opennms.netmgt.config.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.ValidateUsing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top-level element for the service-configuration.xml configuration file.
 */
@XmlRootElement(name = "service-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class ServiceConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
    private static final String DEFAULT_CONFIG_RESOURCE = "/defaults/service-configuration.xml";

    /**
     * Service to be launched by the manager.
     */
    @XmlElement(name = "service")
    private List<Service> m_services = new ArrayList<>();

    public ServiceConfiguration() {
    }

    public ServiceConfiguration(final List<Service> services) {
        setServices(services);
    }

    public List<Service> getServices() {
        return m_services;
    }

    public void setServices(final List<Service> services) {
        if (services == m_services) return;
        m_services.clear();
        if (services != null) m_services.addAll(services);
    }

    public void addService(final Service service) {
        m_services.add(service);
    }

    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    public int hashCode() {
        return Objects.hash(m_services);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ServiceConfiguration) {
            final ServiceConfiguration that = (ServiceConfiguration) obj;
            return Objects.equals(this.m_services, that.m_services);
        }
        return false;
    }

    /**
     * Loads the default service configuration from classpath.
     *
     * @return default service configuration, or null if not found
     */
    private static ServiceConfiguration loadDefaults() {
        try (InputStream is = ServiceConfiguration.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (is == null) {
                LOG.warn("Default service configuration not found at: {}", DEFAULT_CONFIG_RESOURCE);
                return null;
            }
            return JaxbUtils.unmarshal(ServiceConfiguration.class, is);
        } catch (IOException e) {
            LOG.error("Failed to load default service configuration from: {}", DEFAULT_CONFIG_RESOURCE, e);
            return null;
        }
    }

    /**
     * Merges user configuration with defaults from classpath.
     * User configuration takes precedence over defaults.
     *
     * @param userConfig the user-defined service configuration
     * @return merged configuration with defaults filled in
     */
    public static ServiceConfiguration mergeWithDefaults(final ServiceConfiguration userConfig) {
        if (userConfig == null) {
            return loadDefaults();
        }

        final ServiceConfiguration defaults = loadDefaults();
        if (defaults == null) {
            LOG.warn("No default configuration found, using user configuration as-is");
            return userConfig;
        }

        // Create a map of default services by name for quick lookup
        final Map<String, Service> defaultServiceMap = new HashMap<>();
        for (Service service : defaults.getServices()) {
            defaultServiceMap.put(service.getName(), service);
        }

        // Merge user services with defaults
        final ServiceConfiguration merged = new ServiceConfiguration();
        for (Service userService : userConfig.getServices()) {
            final Service defaultService = defaultServiceMap.get(userService.getName());
            final Service mergedService = Service.merge(userService, defaultService);
            merged.addService(mergedService);
        }

        return merged;
    }
}

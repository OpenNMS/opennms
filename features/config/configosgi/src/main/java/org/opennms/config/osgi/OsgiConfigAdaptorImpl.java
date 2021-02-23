/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.config.configservice.api.ConfigurationService;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Gets notified of configuration changes that happened in OpenNMS and passes them on to the OSGI world. */
public class OsgiConfigAdaptorImpl implements OsgiConfigAdaptor
        //, ServiceListener
        {

    private final static Logger LOG = LoggerFactory.getLogger(OpenNMSPersistenceManager.class);

    private final ConfigurationService configServiceOpennms;
    private ConfigurationAdmin configurationAdminOsgi;
    private final BundleContext bundleContext;

    public OsgiConfigAdaptorImpl(final BundleContext bundleContext, final ConfigurationService configService) {
        this.bundleContext = bundleContext;
        this.configServiceOpennms = configService;
        this.configServiceOpennms.registerForUpdates(PID, this);
    }

    // Hook to be notified of changes from ConfigurationService
    @Override
    public void configurationHasChanged(final String pid) {
        Objects.requireNonNull(pid);

        // Get opennms config
        Optional<Map<String, String>> dictionary = configServiceOpennms.getConfigurationAsMap(pid);
        if (!dictionary.isPresent()) {
            LOG.warn("Cannot find configuration, even though we were informed of a change on {}. Will ignore it.", pid);
            return;
        }
        Map<String, String> propertiesFromConfigService = dictionary.get();

        // Get osgi config
        Optional<ConfigurationAdmin> configAdminOpt = getConfigurationAdminOsgi();
        if(!configAdminOpt.isPresent()) {
            LOG.warn("Can't get hold of {}, thus I can't update the configuration for {}. Will ignore it.", ConfigurationAdmin.class.getSimpleName(), pid);
            return;
        }
        final Configuration configFromAdminServiceOsgi;
        try {
            configFromAdminServiceOsgi = configAdminOpt.get().getConfiguration(pid, "?org.opennms");
        } catch (IOException e) {
            LOG.warn("Can't get load the configuration for {} from {}. Will ignore it.", pid, ConfigurationAdmin.class.getSimpleName(), e);
            return;
        }
        Dictionary<String, Object> propertiesFromOsgi = Optional.ofNullable(configFromAdminServiceOsgi.getProperties())
                .orElse(new Hashtable<>());

        // Set / update properties. We can't remove properties that are no longer present from configServiceOpennms since osgi stores
        // also internal configuration so we can't do a real diff.
        for (Map.Entry<String, String> entry : propertiesFromConfigService.entrySet()) {
            propertiesFromOsgi.put(entry.getKey(), entry.getValue());
        }
        try {
            configFromAdminServiceOsgi.update(propertiesFromOsgi);
        } catch (final IOException e) {
            LOG.warn("An error occurred trying to update the config for {}. Will ignore it.", pid, e);
        }
    }

    private Optional<ConfigurationAdmin> getConfigurationAdminOsgi() {
        if(this.configurationAdminOsgi == null) {
           this.configurationAdminOsgi = Optional.ofNullable(bundleContext.getServiceReference(ConfigurationAdmin.class))
                   .map(bundleContext::getService)
                   .orElse(null);
        }
        return Optional.ofNullable(configurationAdminOsgi);
    }
}

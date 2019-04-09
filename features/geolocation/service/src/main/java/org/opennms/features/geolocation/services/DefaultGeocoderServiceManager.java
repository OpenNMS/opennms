/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.geolocation.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.GeocoderServiceManager;
import org.opennms.features.geocoder.GeocoderServiceManagerConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

// TODO MVR move to geocoder module
public class DefaultGeocoderServiceManager implements GeocoderServiceManager {

    private static final String PID = "org.opennms.features.geocoder";

    private BundleContext bundleContext;
    private ConfigurationAdmin configurationAdmin;
    private GeocoderServiceManagerConfiguration configuration;

    public DefaultGeocoderServiceManager(BundleContext bundleContext, ConfigurationAdmin configurationAdmin, GeocoderServiceManagerConfiguration configuration) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public void updateConfiguration(GeocoderServiceManagerConfiguration newConfiguration) throws IOException {
        final Configuration configuration = configurationAdmin.getConfiguration(PID);
        final Dictionary<String, Object> currentProperties = configuration.getProperties() == null ? new Hashtable<>() : configuration.getProperties();

        // Only update if changed
        if (!Objects.equals(newConfiguration, configuration)) {
            applyProperties(currentProperties, newConfiguration.asMap());
            configuration.update(currentProperties);
        }
    }

    @Override
    public GeocoderServiceManagerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public GeocoderService getActiveGeocoderService() {
        return getGeocoderService(configuration.getActiveGeocoderId());
    }

    @Override
    public List<GeocoderService> getGeocoderServices() {
        try {
            final List<GeocoderService> registeredServices = bundleContext.getServiceReferences(GeocoderService.class, null)
                    .stream()
                    .map(reference -> bundleContext.getService(reference))
                    .collect(Collectors.toList());
            return registeredServices;
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("Could not retrieve registered GeocoderServices.", e);
        }
    }

    @Override
    public void updateGeocoderConfiguration(String geocoderId, Map<String, Object> newProperties) throws IOException, GeocoderConfigurationException {
        final GeocoderService geocoderService = getGeocoderService(geocoderId);
        final GeocoderConfiguration currentConfiguration = geocoderService.getConfiguration();

        // Only update if configuration has not yet changed
        if (!Objects.equals(currentConfiguration.asMap(), newProperties)) {
            geocoderService.validateConfiguration(newProperties);

            // Updating the configuration will result in a bundle reload to which the configuration belongs
            // Please keep in mind, that the config pid of the geocoder must be PID + geocoderId
            final String configPID = PID + "." + geocoderId;
            final Configuration configuration = configurationAdmin.getConfiguration(configPID, null);
            final Dictionary<String, Object> currentProperties = configuration.getProperties() != null ? configuration.getProperties() : new Hashtable<>();
            applyProperties(currentProperties, newProperties);

            // Ensure file will be created if it does not yet exist
            if (currentProperties.get("felix.fileinstall.filename") == null) {
                final Path configFile = Paths.get(System.getProperty("karaf.etc"), configPID + ".cfg");
                final Properties persistentProperties = new Properties();
                newProperties.entrySet().forEach(e -> persistentProperties.put(e.getKey(), e.getValue().toString()));
                persistentProperties.store(new FileOutputStream(configFile.toFile()), null);
            }
            configuration.update(currentProperties);
        }
    }

    @Override
    public GeocoderService getGeocoderService(String geocoderId) {
        Objects.requireNonNull(geocoderId);
        final GeocoderService geocoderService = getGeocoderServices().stream()
                .filter(service -> geocoderId.equalsIgnoreCase(service.getId())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find GeocoderService with id " + geocoderId));
        return geocoderService;
    }

    // Updates the currentProperties with values from newProperties. Deletes null values. Does not remove keys.
    private static void applyProperties(Dictionary<String, Object> currentProperties, Map<String, Object> newProperties) {
        newProperties.entrySet().forEach(e -> {
            if (e.getValue() == null) {
                currentProperties.remove(e.getKey());
            } else {
                currentProperties.put(e.getKey(), e.getValue().toString());
            }
        });
    }
}

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
package org.opennms.features.geocoder.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
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

public class DefaultGeocoderServiceManager implements GeocoderServiceManager {

    private static final String PID = "org.opennms.features.geocoder";

    private static final Function<String, String> configPidFactory = (geocoderId) -> PID + "." + geocoderId;

    private BundleContext bundleContext;
    private ConfigurationAdmin configurationAdmin;
    private GeocoderServiceManagerConfiguration configuration;

    public DefaultGeocoderServiceManager(BundleContext bundleContext, ConfigurationAdmin configurationAdmin, GeocoderServiceManagerConfiguration configuration) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public void resetConfiguration() throws IOException {
        for (GeocoderService service : getGeocoderServices()) {
            final String configPid = configPidFactory.apply(service.getId());
            final Configuration configuration = configurationAdmin.getConfiguration(configPid, null);
            new ConfigurationWrapper(configuration).delete();
        }
        new ConfigurationWrapper(configurationAdmin.getConfiguration(PID)).delete();
    }

    @Override
    public void updateConfiguration(GeocoderServiceManagerConfiguration newConfiguration) throws IOException {
        final Configuration configuration = configurationAdmin.getConfiguration(PID);
        new ConfigurationWrapper(configuration).update(newConfiguration.asMap());
    }

    @Override
    public GeocoderServiceManagerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public GeocoderService getActiveGeocoderService() {
        if (configuration.getActiveGeocoderId() == null || "".equals(configuration.getActiveGeocoderId())) {
            return null;
        }
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
            final String configPID = configPidFactory.apply(geocoderId);
            final Configuration configuration = configurationAdmin.getConfiguration(configPID, null);
            new ConfigurationWrapper(configuration).update(newProperties);
        }
    }

    @Override
    public GeocoderService getGeocoderService(String geocoderId) {
        Objects.requireNonNull(geocoderId);
        final GeocoderService geocoderService = getGeocoderServices().stream()
                .filter(service -> geocoderId.equalsIgnoreCase(service.getId())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find GeocoderService with id '" + geocoderId + "'"));
        return geocoderService;
    }
}

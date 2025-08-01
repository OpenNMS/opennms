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
package org.opennms.features.geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manager interface to the underlying {@link GeocoderService}s.
 *
 * @author mvrueden
 */
public interface GeocoderServiceManager {

    /**
     * Deletes all configuration files related to the geocoder service.
     * This results in falling back to the defaults
     *
     * @throws IOException when deleting the configuration files failed.
     */
    void resetConfiguration() throws IOException;

    /**
     * Updates the configuration for the service manager
     *
     * @throws IOException when writing the configuration file failed
     */
    void updateConfiguration(GeocoderServiceManagerConfiguration configuration) throws IOException;

    /**
     * Returns the configuration for the service manager
     *
     * @return the configuration for the service manager
     */
    GeocoderServiceManagerConfiguration getConfiguration();

    /**
     * Returns the current active {@link GeocoderService} or null if none is active.
     *
     * @return the current active {@link GeocoderService} or null if none is active.
     */
    GeocoderService getActiveGeocoderService();

    /**
     * Updates the configuration for the {@link GeocoderService} with the provided id.
     *
     * @param geocoderId The id of the {@link GeocoderService} to update
     * @param newProperties The new configuration of the {@link GeocoderService}
     * @throws IOException in case the configuration could not be persisted
     * @throws NoSuchElementException if a {@link GeocoderService} witht he provided <code>geocoderId</code> does not exist.
     */
    void updateGeocoderConfiguration(String geocoderId, Map<String, Object> newProperties) throws IOException, NoSuchElementException;

    /**
     * Returns all registered {@link GeocoderService}. None of those may be active
     *
     * @return all registered {@link GeocoderService}.
     */
    List<GeocoderService> getGeocoderServices();

    /**
     * Returns the {@link GeocoderService} identified by the provided <code>geocoderId</code>
     *
     * @param geocoderId The id of the {@link GeocoderService} to return.
     * @return the {@link GeocoderService} identified by the provided <code>geocoderId</code>
     * @throws NoSuchElementException if the {@link GeocoderService} with the provided <code>geocoderId</code> does not exist.
     */
    GeocoderService getGeocoderService(String geocoderId) throws NoSuchElementException;
}

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

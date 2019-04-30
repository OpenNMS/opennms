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

public interface GeocoderServiceManager {

    // Deletes all configuration files related to the geocoder service.
    // This results in falling back to the defaults
    void resetConfiguration() throws IOException;

    // Updates the configuration for the service manager
    void updateConfiguration(GeocoderServiceManagerConfiguration configuration) throws IOException;

    // Returns the configuration for the service manager
    GeocoderServiceManagerConfiguration getConfiguration();

    // Returns the current active geocoder service, or null
    GeocoderService getActiveGeocoderService();

    // Updates the configuration for the geocoder with the provided id. Throws NoSuchElementException if geocoder does not exist
    void updateGeocoderConfiguration(String geocoderId, Map<String, Object> newProperties) throws IOException;

    // Returns ALL geocoders. None of those may be active
    List<GeocoderService> getGeocoderServices();

    // Returns the geocoder identified by the provided id. Throws NoSuchElementException if geocoder does not exist
    GeocoderService getGeocoderService(String geocoderId);
}

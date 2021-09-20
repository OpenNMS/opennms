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

package org.opennms.features.geocoder;

import java.util.Map;

/**
 * A {@link GeocoderService} capable of resolving an address string to actual coordinates.
 *
 * @author mvrueden
 */
public interface GeocoderService {

    /**
     * A unique identifier for this {@link GeocoderService}.
     * @return The unique identifier for this {@link GeocoderService}.
     */
    String getId();

    /**
     * Resolves the given address string to valid coordinates.
     * Originally it returned only the {@link Coordinates} but that makes it hard to distinguish
     * if there was no valid address or an exception occurred.
     *
     * @param address The address to resolve
     * @return The result of the resolution
     * @throws GeocoderException
     */
    GeocoderResult resolveAddress(final String address) throws GeocoderConfigurationException;

    /**
     * Returns configuration of the {@link GeocoderService}.
     *
     * @return configuration.
     */
    GeocoderConfiguration getConfiguration();

    /**
     * Validates the provided configuration properties.
     *
     * @param properties to validate
     * @throws GeocoderConfigurationException in case the configuration is invalid.
     */
    void validateConfiguration(Map<String, Object> properties) throws GeocoderConfigurationException;
}

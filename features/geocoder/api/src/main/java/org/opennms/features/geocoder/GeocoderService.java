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

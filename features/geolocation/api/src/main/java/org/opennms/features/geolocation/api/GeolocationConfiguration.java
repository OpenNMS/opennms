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
package org.opennms.features.geolocation.api;

import java.util.Map;

/**
 * Interface for the configuration of the map component.
 *
 */
public interface GeolocationConfiguration {

    /**
     * Returns the tile server url.
     *
     * See http://leafletjs.com/reference.html#tilelayer for more details.
     *
     * @return the tile server url
     */
    String getTileServerUrl();

    /**
     * Returns the layer options for the tile layer.
     * The options should contain a 'attribution' tile layer option to honor the contributors appropriate.
     *
     * See http://leafletjs.com/reference.html#tilelayer-options for more details.
     *
     * @return the layer options for the tile layer
     */
    Map<String, String> getOptions();

}

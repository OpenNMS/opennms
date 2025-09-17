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
package org.opennms.features.geolocation.services;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.geolocation.api.GeolocationConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * The default configuration is stored in opennms.properties.
 *
 */
public class DefaultGeolocationConfiguration implements GeolocationConfiguration {

    /**
     * The key under which the tile server url is stored in opennms.properties.
     */
    private static final String URL_KEY = "gwt.openlayers.url";

    /**
     * The key prefix under which all options are stored in opennms.properties.
     */
    public static final String OPTIONS_KEY_PREFIX = "gwt.openlayers.options.";

    public DefaultGeolocationConfiguration() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(getTileServerUrl()), "System Property 'gwt.openlayers.url' is not defined");
    }

    @Override
    public String getTileServerUrl() {
        final String url = System.getProperty(URL_KEY);
        return sanitize(url);
    }

    @Override
    public Map<String, String> getOptions() {
        return System.getProperties().keySet().stream()
                .filter(key -> ((String) key).startsWith(OPTIONS_KEY_PREFIX))
                .map(key -> ((String) key).substring(OPTIONS_KEY_PREFIX.length()))
                .collect(Collectors.toMap(Function.identity(), key -> sanitize(System.getProperty(OPTIONS_KEY_PREFIX + key))));
    }

    private static String sanitize(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }
        // The input may contain ${variable} statements, which must be converted to {variable} in order to work properly
        return input.replaceAll("\\$\\{", "{");
    }
}

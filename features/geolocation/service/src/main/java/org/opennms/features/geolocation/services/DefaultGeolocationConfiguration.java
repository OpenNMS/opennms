/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.geolocation.api.GeolocationConfiguration;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.google.gwt.thirdparty.guava.common.base.Strings;

public class DefaultGeolocationConfiguration implements GeolocationConfiguration {
    private static final String URL_KEY = "gwt.openlayers.url";

    public static final String OPTIONS_KEY_PREFIX = "gwt.openlayers.options.";

    @Override
    public String getTileServerUrl() {
        final String url = System.getProperty(URL_KEY);
        return sanitizeForVaadin(url);
    }

    @Override
    public Map<String, String> getOptions() {
        return System.getProperties().keySet().stream()
                .filter(key -> ((String) key).startsWith(OPTIONS_KEY_PREFIX))
                .map(key -> ((String) key).substring(OPTIONS_KEY_PREFIX.length()))
                .collect(Collectors.toMap(Function.identity(), key ->sanitizeForVaadin(System.getProperty(OPTIONS_KEY_PREFIX + key))));
    }

    @Override
    public String getTileLayerAttribution() {
        return System.getProperty(OPTIONS_KEY_PREFIX + "attribution");
    }

    // TODO MVR invoke...
    public void validate() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(getTileServerUrl()), "System Property 'gwt.openlayers.url' is not defined");

    }

    // TODO MVR do we still need this?! or should we move it to nodemapsconfiguration
    private static String sanitizeForVaadin(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }
        // The input may contain ${variable} statements, which must be converted to {variable} in order to work properly
        return input.replaceAll("\\$\\{", "{");
    }
}

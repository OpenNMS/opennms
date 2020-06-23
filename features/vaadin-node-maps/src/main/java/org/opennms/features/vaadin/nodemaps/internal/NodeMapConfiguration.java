/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;


import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.geolocation.api.GeolocationConfiguration;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Option;

import com.google.common.base.Strings;

/**
 * Configuration object introduced to address NMS-8597 and reduce possible merge conflicts.
 */
public class NodeMapConfiguration {

    private final GeolocationConfiguration geolocationConfiguration;

    public NodeMapConfiguration(GeolocationConfiguration geolocationConfiguration) {
        this.geolocationConfiguration = geolocationConfiguration;
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(geolocationConfiguration.getTileServerUrl())
                && !Strings.isNullOrEmpty(geolocationConfiguration.getOptions().get("attribution"));
    }

    public List<Option> getOptions() {
        return geolocationConfiguration.getOptions().entrySet().stream()
            .map(e -> new Option(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    public String getTileServerUrl() {
        return geolocationConfiguration.getTileServerUrl();
    }

    public String getTileLayerAttribution() {
        return geolocationConfiguration.getOptions().get("attribution");
    }
}

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

package org.opennms.netmgt.telemetry.minion;

import org.opennms.netmgt.telemetry.listeners.api.ListenerDefinition;

import java.util.Map;

public class MapBasedListenerDef implements ListenerDefinition {
    private final String name;
    private final String className;
    private final Map<String, String> parameters;

    public MapBasedListenerDef(Map<String, String> parameters) {
        name = MapUtils.getRequiredString("name", parameters);
        className = MapUtils.getRequiredString("class-name", parameters);
        // Extract the keys from the map that are prefixed with "listener."
        this.parameters = MapUtils.filterKeysByPrefix(parameters, "listener.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameters;
    }

}

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

package org.opennms.netmgt.collection.api;

import org.opennms.core.utils.LocationUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Boilerplate code for basic {@link ServiceCollector} implementations.
 *
 * If the {@link ServiceCollector} is expected to run on both OpenNMS and Minion,
 * consider extending {@link AbstractRemoteServiceCollector} instead.
 *
 * @author jwhite
 */
public abstract class AbstractServiceCollector implements ServiceCollector {

    @Override
    public void initialize() throws CollectionInitializationException  {
        // pass
    }

    @Override
    public void validateAgent(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        // pass
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public String getEffectiveLocation(String location) {
        // Always run at the default location
        return LocationUtils.DEFAULT_LOCATION_NAME;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        // We always run at the default location, so no marshaling should be perfomed
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        // We always run at the default location, so no unmarshaling should be perfomed
        throw new UnsupportedOperationException();
    }
}

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

import java.util.Collections;
import java.util.Map;

import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxy;

/**
 * Used to preserve API compatibility with the previous interface of the
 * {@link ServiceCollector}.
 *
 * Will be removed once all collectors have been ported over.
 *
 * @deprecated extend the AbstractServiceCollector instead!
 * @author jwhite
 */
@Deprecated
public abstract class AbstractLegacyServiceCollector implements ServiceCollector {

    @Override
    public void initialize() throws CollectionInitializationException  {
        initialize(Collections.emptyMap());
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        try {
            initialize(agent, parameters);
        } catch (CollectionInitializationException e) {
            throw new CollectionException("Agent intilization failed", e);
        }
        try {
            return collect(agent, EventIpcManagerFactory.getIpcManager(), parameters);
        } finally {
            release(agent);
        }
    }

    @Override
    public String getEffectiveLocation(String location) {
        return null;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        throw new UnsupportedOperationException();
    }

    // Old API
    public abstract void initialize(Map<String, String> parameters) throws CollectionInitializationException;
    public abstract void release();
    public abstract void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException;
    public abstract void release(CollectionAgent agent);
    public abstract CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException;
}

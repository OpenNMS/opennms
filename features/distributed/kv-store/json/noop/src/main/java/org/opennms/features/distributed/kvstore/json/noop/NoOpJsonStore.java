/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.kvstore.json.noop;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;
import org.opennms.features.distributed.kvstore.api.JsonStore;

public class NoOpJsonStore extends AbstractAsyncKeyValueStore<String> implements JsonStore {
    @Override
    public long put(String key, String value, String context, Integer ttlInSeconds) {
        return 0;
    }

    @Override
    public Optional<String> get(String key, String context) {
        return Optional.empty();
    }

    @Override
    public Optional<Optional<String>> getIfStale(String key, String context, long timestamp) {
        return Optional.empty();
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        return OptionalLong.empty();
    }

    @Override
    public String getName() {
        return "NoOp";
    }

    @Override
    public Map<String, String> enumerateContext(String context) {
        return Collections.emptyMap();
    }

    @Override
    public void delete(String key, String context) {
    }
}

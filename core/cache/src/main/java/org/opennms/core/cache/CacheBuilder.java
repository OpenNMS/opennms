/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.cache;

import java.util.Objects;

import com.google.common.cache.CacheLoader;

public class CacheBuilder<K, V> {

    private CacheConfig config;
    private CacheLoader<K, V> loader;

    public CacheBuilder withConfig(CacheConfig config) {
        this.config = config;
        return this;
    }

    public CacheBuilder withCacheLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
        return this;
    }

    public Cache<K, V> build() {
        Objects.requireNonNull(config);
        Objects.requireNonNull(loader);
        return new Cache(config, loader);
    }

}

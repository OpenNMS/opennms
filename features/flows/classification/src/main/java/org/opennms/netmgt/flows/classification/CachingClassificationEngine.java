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

package org.opennms.netmgt.flows.classification;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingClassificationEngine implements ClassificationEngine {

    // The cache. Must return Optional<String> as values retrieved from the cache loader MUST NOT be null.
    private final LoadingCache<ClassificationRequest, Optional<String>> cache;
    private final ClassificationEngine delegate;

    public CachingClassificationEngine(ClassificationEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(Integer.getInteger("org.opennms.features.flows.classification.caching.maxSize", 5000))
            .expireAfterAccess(Long.getLong("org.opennms.features.flows.classification.caching.expireTime", 60 * 1000), TimeUnit.MILLISECONDS)
            .build(
                new CacheLoader<ClassificationRequest, Optional<String>>() {
                    @Override
                    public Optional<String> load(ClassificationRequest key) {
                        return Optional.ofNullable(delegate.classify(key));
                    }
                });
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        try {
            return cache.get(classificationRequest).orElse(null);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error loading entry from cache", e);
        }
    }

    @Override
    public void reload() {
        this.cache.invalidateAll();
        this.delegate.reload();
    }
}

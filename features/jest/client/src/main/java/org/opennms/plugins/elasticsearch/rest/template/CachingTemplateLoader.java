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

package org.opennms.plugins.elasticsearch.rest.template;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Caches the loading of templates.
 */
public class CachingTemplateLoader implements TemplateLoader {

    private final LoadingCache<TemplateKey, String> cache;

    public CachingTemplateLoader(final TemplateLoader delegate) {
        Objects.requireNonNull(delegate);
        this.cache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<TemplateKey, String>() {
            @Override
            public String load(TemplateKey key) throws Exception {
                return delegate.load(key.getServerVersion(), key.getResource());
            }
        });
    }

    @Override
    public String load(Version serverVersion, String resource) throws IOException {
        try {
            return cache.get(new TemplateKey(serverVersion, resource));
        } catch (ExecutionException e) {
            throw new IOException("Could not read data from cache", e);
        }
    }

    private static final class TemplateKey {
        private final Version serverVersion;
        private final String resource;

        private TemplateKey(Version serverVersion, String resource) {
            this.serverVersion = serverVersion;
            this.resource = resource;
        }

        private Version getServerVersion() {
            return serverVersion;
        }

        private String getResource() {
            return resource;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TemplateKey that = (TemplateKey) o;
            return Objects.equals(serverVersion, that.serverVersion) &&
                    Objects.equals(resource, that.resource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverVersion, resource);
        }
    }
}

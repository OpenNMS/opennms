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
package org.opennms.features.jest.client.template;

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

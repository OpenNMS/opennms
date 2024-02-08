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
package org.opennms.core.mate.api;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

public final class ContextKey implements Comparable<ContextKey> {
    public final String context;
    public final String key;

    public ContextKey(final String context, final String key) {
        this.context = Objects.requireNonNull(context);
        this.key = Objects.requireNonNull(key);
    }

    public ContextKey(final String contextKey) {
        Objects.requireNonNull(contextKey , "contextKey must not be null");
        final String arr[] = contextKey.split(":");

        if (arr.length != 2) {
            throw new IllegalArgumentException("contextKey '" + contextKey + "' must be in the format 'context:key'");
        }

        this.context = arr[0];
        this.key = arr[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContextKey)) {
            return false;
        }
        final ContextKey that = (ContextKey) o;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, key);
    }

    public String getContext() {
        return this.context;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public int compareTo(final ContextKey that) {
        return ComparisonChain.start()
                .compare(this.context, that.context)
                .compare(this.key, that.key)
                .result();
    }
}

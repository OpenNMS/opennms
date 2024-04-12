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
package org.opennms.core.mate.api.parser;

import java.util.Objects;

public class ContextKey {
    private final String context;
    private final String key;

    private ContextKey(final String context, final String key) {
        this.context = context;
        this.key = key;
    }

    @Override
    public String toString() {
        return context + ":" + key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextKey that = (ContextKey) o;
        return Objects.equals(context, that.context) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, key);
    }

    static ContextKey of(final String contextKey) {
        if (contextKey.contains(":")) {
            final int index = contextKey.indexOf(':');
            return new ContextKey(
                    contextKey.substring(0, index),
                    contextKey.substring(index + 1)
            );
        } else {
            return null;
        }
    }

    public String getContext() {
        return context;
    }

    public String getKey() {
        return key;
    }
}
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
import java.util.Optional;
import java.util.Set;

public interface Scope {
    Optional<ScopeValue> get(final ContextKey contextKey);
    Set<ContextKey> keys();

    public static class ScopeValue {
        public final ScopeName scopeName;
        public final String value;

        public ScopeValue(final ScopeName scopeName, final String value) {
            this.scopeName = Objects.requireNonNull(scopeName);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScopeValue that = (ScopeValue) o;
            return scopeName == that.scopeName && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scopeName, value);
        }

        @Override
        public String toString() {
            return "ScopeValue{" +
                    "scopeName=" + scopeName +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public enum ScopeName {
        DEFAULT, GLOBAL, NODE, INTERFACE, SERVICE;
    }
}

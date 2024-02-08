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
package org.opennms.netmgt.flows.api;

import java.util.Objects;

/**
 * Used to associate some object with a direction.
 */
public class Directional<T> {
    private final T value;
    private final boolean isIngress;

    public Directional(T value, boolean isIngress) {
        this.value = Objects.requireNonNull(value);
        this.isIngress = isIngress;
    }

    public T getValue() {
        return value;
    }

    public boolean isIngress() {
        return isIngress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directional<?> that = (Directional<?>) o;
        return isIngress == that.isIngress &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isIngress);
    }

    @Override
    public String toString() {
        return "Directional{" +
                "value=" + value +
                ",ingress=" + isIngress +
                '}';
    }
}

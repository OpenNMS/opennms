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
package org.opennms.netmgt.flows.elastic;

import java.util.Objects;

import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;

import com.google.gson.annotations.SerializedName;

public enum Locality {
    @SerializedName("public")
    PUBLIC("public"),
    @SerializedName("private")
    PRIVATE("private");

    private final String value;

    private Locality(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    public static Locality from(EnrichedFlow.Locality locality) {
        if (locality == null) {
            return null;
        }

        switch (locality) {
            case PUBLIC: return Locality.PUBLIC;
            case PRIVATE: return Locality.PRIVATE;
            default: throw new IllegalStateException();
        }
    }
}

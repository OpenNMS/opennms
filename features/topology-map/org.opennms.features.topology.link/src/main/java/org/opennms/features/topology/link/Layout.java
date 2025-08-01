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
package org.opennms.features.topology.link;

import java.util.Objects;

/**
 * All known layout algorithms.
 */
public enum Layout {

    CIRCLE("Circle Layout"),
    D3("D3 Layout"),
    FR("FR Layout"),
    HIERARCHY("Hierarchy Layout"),
    ISOM("ISOM Layout"),
    KK("KK Layout"),
    REAL("Real Ultimate Layout"),
    SPRING("Spring Layout"),
    MANUAL("Manual Layout");

    private final String label;

    Layout(String label) {
        this.label = Objects.requireNonNull(label);
    }

    public String getLabel() {
        return label;
    }

    public static Layout createFromLabel(String label) {
        for (Layout eachLayout : values()) {
            if (eachLayout.getLabel().equals(label)) {
                return eachLayout;
            }
        }
        return null;
    }
}

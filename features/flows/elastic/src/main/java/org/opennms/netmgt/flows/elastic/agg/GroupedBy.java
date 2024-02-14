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
package org.opennms.netmgt.flows.elastic.agg;

/**
 * Different ways metrics are grouped before top-k
 * calculations are performed.
 */
public enum GroupedBy {
    EXPORTER(null),
    EXPORTER_INTERFACE(EXPORTER),
    EXPORTER_INTERFACE_APPLICATION(EXPORTER_INTERFACE),
    EXPORTER_INTERFACE_HOST(EXPORTER_INTERFACE),
    EXPORTER_INTERFACE_CONVERSATION(EXPORTER_INTERFACE),
    EXPORTER_INTERFACE_TOS(EXPORTER_INTERFACE),
    EXPORTER_INTERFACE_TOS_APPLICATION(EXPORTER_INTERFACE_TOS),
    EXPORTER_INTERFACE_TOS_HOST(EXPORTER_INTERFACE_TOS),
    EXPORTER_INTERFACE_TOS_CONVERSATION(EXPORTER_INTERFACE_TOS);

    private GroupedBy parent;

    GroupedBy(GroupedBy parent) {
        this.parent = parent;
    }

    public GroupedBy getParent() {
        return parent;
    }
}

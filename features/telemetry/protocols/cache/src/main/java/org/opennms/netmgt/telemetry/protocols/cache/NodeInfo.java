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
package org.opennms.netmgt.telemetry.protocols.cache;

import java.util.List;

public class NodeInfo implements org.opennms.integration.api.v1.flows.Flow.NodeInfo {

    // ID of the interface which was selected during IP to node lookup
    private int interfaceId;

    private int nodeId;
    private String foreignId;
    private String foreignSource;
    private List<String> categories = List.of();

    @Override
    public int getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(final int interfaceId) {
        this.interfaceId = interfaceId;
    }

    @Override
    public int getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(final int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getForeignId() {
        return this.foreignId;
    }

    public void setForeignId(final String foreignId) {
        this.foreignId = foreignId;
    }

    @Override
    public String getForeignSource() {
        return this.foreignSource;
    }

    public void setForeignSource(final String foreignSource) {
        this.foreignSource = foreignSource;
    }

    @Override
    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }
}

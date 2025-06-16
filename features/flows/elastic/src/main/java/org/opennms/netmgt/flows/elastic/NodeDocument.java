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

import java.util.LinkedList;
import java.util.List;

import org.opennms.integration.api.v1.flows.Flow.NodeInfo;

import com.google.gson.annotations.SerializedName;

public class NodeDocument {
    @SerializedName("foreign_source")
    private String foreignSource;

    @SerializedName("foreign_id")
    private String foreignId;

    @SerializedName("node_id")
    private Integer nodeId;

    @SerializedName("interface_id")
    private Integer interfaceId;

    @SerializedName("categories")
    private List<String> categories = new LinkedList<>();

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getForeignId() {
        return foreignId;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(final Integer interfaceId) {
        this.interfaceId = interfaceId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public static NodeDocument from(final NodeInfo info) {
        if (info == null) {
            return null;
        }

        final NodeDocument doc = new NodeDocument();
        doc.setForeignSource(info.getForeignSource());
        doc.setForeignId(info.getForeignId());
        doc.setNodeId(info.getNodeId());
        doc.setInterfaceId(info.getInterfaceId());
        doc.setCategories(info.getCategories());
        return doc;
    }
}

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
package org.opennms.web.rest.v2.model.graphCollection;

/**
 * The resolved resource behind a graph collection graph, with the parent one
 * level up so the view can show "Node: foo / SNMP Interface: eth0".
 */
public class GraphCollectionResourceDto {
    private String id;
    private String label;
    private String typeName;
    private String typeLabel;
    private String link;
    private GraphCollectionResourceDto parent;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(final String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

    public GraphCollectionResourceDto getParent() {
        return parent;
    }

    public void setParent(final GraphCollectionResourceDto parent) {
        this.parent = parent;
    }
}

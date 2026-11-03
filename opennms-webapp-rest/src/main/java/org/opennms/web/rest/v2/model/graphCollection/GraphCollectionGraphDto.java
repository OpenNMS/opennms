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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * One graph in a graph collection. kind says what the entry is: "prefab", the
 * only kind today, is a prefabricated graph name drawn for one resource over a
 * named timespan; "adhoc", reserved for graphs built in the ad-hoc graph builder,
 * is rejected until it is implemented. Omitting kind means "prefab".
 *
 * New prefab entries address the resource with resourceId; nodeId, nodeSource,
 * domain and interfaceId are the pre-resourceId form still found in older
 * configuration files and are carried through untouched.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphCollectionGraphDto {
    private String kind;
    private String title;
    private String resourceId;
    private String graphtype;
    private String timespan;
    private String extlink;
    private String nodeId;
    private String nodeSource;
    private String domain;
    private String interfaceId;

    public String getKind() {
        return kind;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGraphtype() {
        return graphtype;
    }

    public void setGraphtype(final String graphtype) {
        this.graphtype = graphtype;
    }

    public String getTimespan() {
        return timespan;
    }

    public void setTimespan(final String timespan) {
        this.timespan = timespan;
    }

    public String getExtlink() {
        return extlink;
    }

    public void setExtlink(final String extlink) {
        this.extlink = extlink;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(final String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(final String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(final String interfaceId) {
        this.interfaceId = interfaceId;
    }
}

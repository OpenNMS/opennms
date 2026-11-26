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

package org.opennms.web.rest.v2.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A shared group file, a set of groups that sources can import.")
public class XmlGroupFileDto {

    @Schema(description = "Unique id of the group file, formerly the file name in etc/xml-datacollection without .xml.", example = "elasticsearch-cluster-stats")
    private String id;

    @Schema(description = "The groups of the file.")
    private List<XmlGroupDto> groups;

    @Schema(description = "Version of the group file as read, the same value as the ETag. Ignored on writes.")
    private String version;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<XmlGroupDto> getGroups() {
        return groups;
    }

    public void setGroups(final List<XmlGroupDto> groups) {
        this.groups = groups;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}

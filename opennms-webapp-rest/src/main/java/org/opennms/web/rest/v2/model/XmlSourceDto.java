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

@Schema(description = "A document fetched from a URL and the groups collected from it.")
public class XmlSourceDto {

    @Schema(description = "URL of the document. Placeholders such as {ipaddr} are replaced at collection time.", example = "http://{ipaddr}:9200/_cluster/stats")
    private String url;

    @Schema(description = "Request settings, or null to use a plain GET.")
    private XmlRequestDto request;

    @Schema(description = "Ids of shared group files whose groups are collected from this source as well.")
    private List<String> importGroups;

    @Schema(description = "Groups defined for this source only.")
    private List<XmlGroupDto> groups;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public XmlRequestDto getRequest() {
        return request;
    }

    public void setRequest(final XmlRequestDto request) {
        this.request = request;
    }

    public List<String> getImportGroups() {
        return importGroups;
    }

    public void setImportGroups(final List<String> importGroups) {
        this.importGroups = importGroups;
    }

    public List<XmlGroupDto> getGroups() {
        return groups;
    }

    public void setGroups(final List<XmlGroupDto> groups) {
        this.groups = groups;
    }
}

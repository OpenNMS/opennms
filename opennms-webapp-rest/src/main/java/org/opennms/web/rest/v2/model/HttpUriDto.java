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

@Schema(description = "A request the HTTP collector sends, and the attributes it extracts from the response.")
public class HttpUriDto {

    @Schema(description = "Name of the URI, unique within its collection.", example = "document-counts", required = true)
    private String name;

    @Schema(description = "The request and how its response is matched.", required = true)
    private HttpUrlDto url;

    @Schema(description = "Values taken from the groups of the `matches` regular expression.")
    private List<HttpAttribDto> attributes;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public HttpUrlDto getUrl() {
        return url;
    }

    public void setUrl(final HttpUrlDto url) {
        this.url = url;
    }

    public List<HttpAttribDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<HttpAttribDto> attributes) {
        this.attributes = attributes;
    }
}

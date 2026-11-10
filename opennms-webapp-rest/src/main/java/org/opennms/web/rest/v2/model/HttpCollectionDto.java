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

@Schema(description = "An HTTP collection: the URIs the HTTP collector requests and the values it extracts from the responses. Writes replace the whole collection rather than patching it, so a URI left out of the body is deleted.")
public class HttpCollectionDto {

    @Schema(description = "Unique name of the collection. Referenced by a collectd package service through its `collection` parameter.", example = "doc-count", required = true)
    private String name;

    @Schema(description = "How the collected values are stored.", required = true)
    private HttpRrdDto rrd;

    @Schema(description = "The URIs to request, in collection order.")
    private List<HttpUriDto> uris;

    @Schema(description = "Opaque version of the collection, also returned as the ETag header. Send it back as If-Match on a write to be rejected with a 412 instead of overwriting someone else's change.", accessMode = Schema.AccessMode.READ_ONLY)
    private String version;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public HttpRrdDto getRrd() {
        return rrd;
    }

    public void setRrd(final HttpRrdDto rrd) {
        this.rrd = rrd;
    }

    public List<HttpUriDto> getUris() {
        return uris;
    }

    public void setUris(final List<HttpUriDto> uris) {
        this.uris = uris;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}

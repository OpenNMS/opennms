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
package org.opennms.web.rest.v2;

import java.util.Date;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Wire representation of a custom topology view.
 *
 * <p>The {@code definition} (the canvas: nodes, edges, labels, viewport,
 * optional background) is carried as a nested {@link JsonNode} so it appears
 * as real JSON in requests and responses rather than an escaped string. The
 * REST resource converts between this node and the opaque string stored on the
 * {@code TopologyView} entity.
 */
public class TopologyViewDTO {

    @Schema(description = "Server-generated identifier, a UUID on this build. Ignored on input.",
            example = "1e3df566-9268-4ab2-92d0-2c6877fde67d")
    @JsonProperty("id")
    private String id;

    @Schema(description = """
            Display name, trimmed. Required on create and unique across views; a colliding create or
            rename is refused with 409. Optional on update.""",
            example = "Core and distribution")
    @JsonProperty("name")
    private String name;

    @Schema(description = """
            The canvas, as free-form JSON. The server stores it verbatim as a string and does not
            validate its shape. Required on create; omitted on update it leaves the stored definition
            alone.""",
            type = "object")
    @JsonProperty("definition")
    private JsonNode definition;

    @Schema(description = """
            Authenticated principal that created the view. Set by the server from the request's
            principal; a value sent in the body is ignored.""",
            example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("owner")
    private String owner;

    // Serialized as epoch milliseconds, not as the date-time string the
    // auto-derived schema for a Date would claim.
    @Schema(description = "Creation time, epoch milliseconds. Set by the server.",
            type = "integer", format = "int64", example = "1787727339697",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("created")
    private Date created;

    @Schema(description = """
            Time of the last update, epoch milliseconds. Null until the view has been updated at least
            once. Set by the server.""",
            type = "integer", format = "int64", example = "1787727349607", nullable = true,
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("lastModified")
    private Date lastModified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public void setDefinition(JsonNode definition) {
        this.definition = definition;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}

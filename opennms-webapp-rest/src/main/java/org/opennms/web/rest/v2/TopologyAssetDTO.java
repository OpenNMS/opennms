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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Wire shape of a topology image asset's metadata (the bytes are served
 * separately, by id). See {@link TopologyAssetRestService}.
 */
public class TopologyAssetDTO {

    @Schema(description = "Server-generated identifier, a UUID on this build. Also the path segment used to fetch the bytes.",
            example = "defb7a91-9287-459a-ba1d-6879fee1cbfd")
    private String id;

    @Schema(description = "Display name supplied at upload time, trimmed. Not unique.", example = "core-switch")
    private String name;

    @Schema(description = "Which role the image plays, and therefore which size cap applies.",
            example = "icon", allowableValues = {"background", "icon"})
    private String kind;

    @Schema(description = "MIME type recorded from the upload's Content-Type header, without parameters.",
            example = "image/png", allowableValues = {"image/png", "image/jpeg", "image/gif", "image/webp"})
    private String mimeType;

    @Schema(description = "Size of the stored image in bytes.", example = "36667")
    private long sizeBytes;

    @Schema(description = "Authenticated principal that uploaded the asset. Null when the upload ran without a principal.",
            example = "admin")
    private String owner;

    // Serialized as epoch milliseconds, not as the date-time string the
    // auto-derived schema for a Date would claim.
    @Schema(description = "Upload time, epoch milliseconds.", type = "integer", format = "int64", example = "1787727367630")
    private Date created;

    @Schema(description = "Last time the bytes or metadata changed, epoch milliseconds. Backs the ETag on the byte URL.",
            type = "integer", format = "int64", example = "1787727367630")
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

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
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

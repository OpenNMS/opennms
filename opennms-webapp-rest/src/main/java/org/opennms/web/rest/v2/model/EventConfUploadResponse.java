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

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Describes the JSON that the upload endpoint assembles; it is not returned as a type. */
@Schema(name = "EventConfUploadResponse",
        description = "Per-file outcome of an eventconf upload. A file appears in exactly one of the two lists.")
public class EventConfUploadResponse {

    @Schema(description = "Files that were parsed and stored.")
    public List<StoredFile> success;

    @Schema(description = "Files that were rejected. The remaining files are still stored.")
    public List<RejectedFile> errors;

    @Schema(name = "EventConfUploadStoredFile")
    public static class StoredFile {

        @Schema(description = "Source name derived from the uploaded filename, without path or extension.",
                example = "Cisco.syslog.events")
        public String file;

        @Schema(description = "Number of events the file contained.", example = "132")
        public Integer eventCount;

        @Schema(description = "Vendor derived from the part of the name before the first dot.", example = "Cisco")
        public String vendor;

        @Schema(description = "One entry per event in the file.")
        public List<StoredEvent> events;
    }

    @Schema(name = "EventConfUploadStoredEvent")
    public static class StoredEvent {

        @Schema(example = "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN")
        public String uei;

        @Schema(example = "Cisco Syslog: LINK-3-UPDOWN")
        public String label;

        @Schema(description = "Currently mirrors the event label rather than the event description.",
                example = "Cisco Syslog: LINK-3-UPDOWN")
        public String description;

        @Schema(example = "true")
        public Boolean enabled;
    }

    @Schema(name = "EventConfUploadRejectedFile")
    public static class RejectedFile {

        @Schema(description = "Source name derived from the uploaded filename.", example = "Broken.events")
        public String file;

        @Schema(description = "Exception class and message from the failed parse or store.",
                example = "UnmarshalException: unexpected element (uri:\"\", local:\"evnts\")")
        public String error;
    }
}

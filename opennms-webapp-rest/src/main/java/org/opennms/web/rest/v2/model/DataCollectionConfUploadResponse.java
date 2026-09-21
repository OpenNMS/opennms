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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Documentation-only: describes the two-list body POST /datacollectionconf/upload
 * builds from an ad-hoc Map. Nothing returns this type.
 */
@Schema(description = "Per-file outcome of a data collection config upload. A 200 only means the request "
        + "was processed: files that failed to parse or failed to persist appear under `errors` while "
        + "the rest are still stored.")
public class DataCollectionConfUploadResponse {

    @ArraySchema(schema = @Schema(implementation = SuccessEntry.class),
            arraySchema = @Schema(description = "One entry per source stored and, on the bulk path, per "
                    + "profile created or updated."))
    private List<SuccessEntry> success;

    @ArraySchema(schema = @Schema(implementation = ErrorEntry.class),
            arraySchema = @Schema(description = "One entry per file that could not be processed."))
    private List<ErrorEntry> errors;

    public List<SuccessEntry> getSuccess() {
        return success;
    }

    public void setSuccess(List<SuccessEntry> success) {
        this.success = success;
    }

    public List<ErrorEntry> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorEntry> errors) {
        this.errors = errors;
    }

    @Schema(description = "A file that was accepted. On the sources-only path only `file` is present, "
            + "and it carries the parsed <datacollection-group> name rather than the uploaded filename. "
            + "On the bulk path (a <datacollection-config> in the batch) `file` is the uploaded "
            + "basename and either `source` or `profile` names what was written.")
    public static class SuccessEntry {

        @Schema(description = "Source name on the sources-only path, uploaded basename on the bulk path.",
                example = "Cisco")
        private String file;

        @Schema(description = "Name of the source that was created or updated. Bulk path only.",
                example = "Cisco")
        private String source;

        @Schema(description = "Name of the profile that was created or updated from a <snmp-collection> "
                + "entry. Bulk path only.",
                example = "default")
        private String profile;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }
    }

    @Schema(description = "A file that could not be processed.")
    public static class ErrorEntry {

        @Schema(description = "Source name or uploaded basename the failure is attributed to.",
                example = "Broken")
        private String file;

        @Schema(description = "Exception simple name and message.",
                example = "UnmarshalException: null")
        private String error;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}

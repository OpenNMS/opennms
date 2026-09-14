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

/**
 * Documentation-only: describes the single-key error object several
 * /datacollectionconf handlers build from an ad-hoc Map. Nothing returns this type.
 */
@Schema(description = "Single-key error object returned by the /datacollectionconf endpoints that wrap "
        + "their message in an object rather than returning a bare string.")
public class DataCollectionConfErrorResponse {

    @Schema(description = "Human-readable reason the request was rejected.",
            example = "Invalid offset/limit values (limit must be 1..1000)")
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

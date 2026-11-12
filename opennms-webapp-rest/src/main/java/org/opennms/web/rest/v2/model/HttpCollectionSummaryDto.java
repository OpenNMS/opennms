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

@Schema(description = "An HTTP collection without its URIs, for list views.")
public class HttpCollectionSummaryDto {

    @Schema(description = "Name of the collection.", example = "doc-count", required = true)
    private String name;

    @Schema(description = "RRD step in seconds.", example = "300")
    private Integer rrdStep;

    @Schema(description = "Number of URIs the collection requests.", example = "1")
    private Integer uriCount;

    @Schema(description = "Number of attributes over all URIs.", example = "3")
    private Integer attributeCount;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getRrdStep() {
        return rrdStep;
    }

    public void setRrdStep(final Integer rrdStep) {
        this.rrdStep = rrdStep;
    }

    public Integer getUriCount() {
        return uriCount;
    }

    public void setUriCount(final Integer uriCount) {
        this.uriCount = uriCount;
    }

    public Integer getAttributeCount() {
        return attributeCount;
    }

    public void setAttributeCount(final Integer attributeCount) {
        this.attributeCount = attributeCount;
    }
}

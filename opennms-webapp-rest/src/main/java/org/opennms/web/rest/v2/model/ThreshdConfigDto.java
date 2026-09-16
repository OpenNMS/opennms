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

@Schema(description = """
        The complete threshd daemon configuration. Writes replace the stored document rather than
        patching it.""")
public class ThreshdConfigDto {

    @Schema(description = "Worker threads the threshd daemon uses. Must be greater than 0.",
            example = "5", required = true, minimum = "1")
    private Integer threads;

    @Schema(description = "Threshd packages. The schema requires at least one.")
    private List<ThreshdPackageDto> packages;

    @Schema(description = "Thresholder bindings. Named in the singular to match the XML element and the "
            + "key used in the stored configuration document.")
    private List<ThresholderDto> thresholder;

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = threads;
    }
    public List<ThreshdPackageDto> getPackages() {
        return packages;
    }

    public void setPackages(final List<ThreshdPackageDto> packages) {
        this.packages = packages;
    }
    public List<ThresholderDto> getThresholder() {
        return thresholder;
    }

    public void setThresholder(final List<ThresholderDto> thresholder) {
        this.thresholder = thresholder;
    }
}

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

    @Schema(description = "Threshd packages. The schema requires at least one.")
    private List<ThreshdPackageDto> packages;

    public List<ThreshdPackageDto> getPackages() {
        return packages;
    }

    public void setPackages(final List<ThreshdPackageDto> packages) {
        this.packages = packages;
    }
}

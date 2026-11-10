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

@Schema(description = "The settings that apply to every HTTP collection.")
public class HttpCollectionSettingsDto {

    @Schema(description = "Directory the HTTP collector writes its RRD files to.", example = "/opt/opennms/share/rrd/snmp/", required = true)
    private String rrdRepository;

    @Schema(description = "Opaque version of the settings, also returned as the ETag header. Send it back as If-Match on a write to be rejected with a 412 instead of overwriting someone else's change.", accessMode = Schema.AccessMode.READ_ONLY)
    private String version;

    public String getRrdRepository() {
        return rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        this.rrdRepository = rrdRepository;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}

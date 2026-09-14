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
package org.opennms.web.rest.v1.config;

import java.time.ZoneId;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DatetimeformatConfig", description = "Time zone and date format the web UI renders timestamps with.")
public class DatetimeformatConfig {

    @Schema(description = "IANA time zone id, taken from the user's session when one is set and from the server default otherwise.", example = "America/New_York")
    private String zoneId;

    @Schema(description = "java.time date format pattern, from the org.opennms.ui.datettimeformat system property. Defaults to yyyy-MM-dd'T'HH:mm:ssxxx.", example = "yyyy-MM-dd'T'HH:mm:ssxxx")
    private String datetimeformat;

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId.getId();
    }

    public String getDatetimeformat() {
        return datetimeformat;
    }

    public void setDatetimeformat(String datetimeformat) {
        this.datetimeformat = datetimeformat;
    }
}

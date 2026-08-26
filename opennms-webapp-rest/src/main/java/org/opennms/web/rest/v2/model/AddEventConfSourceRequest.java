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

@Schema(name = "AddEventConfSourceRequest", description = "Request body used to create an empty EventConf source.")
public class AddEventConfSourceRequest {

    @Schema(description = "Source name. Conventionally the event file basename, without the '.xml' extension.",
            example = "Cisco.syslog.events", required = true)
    private String name;

    @Schema(description = "Free-form description of the source.",
            example = "Syslog events forwarded by Cisco IOS devices")
    private String description;

    @Schema(description = "Vendor the source belongs to. Defaults to the part of the name before the first dot, and must not exceed 128 characters.",
            example = "Cisco")
    private String vendor;

    public AddEventConfSourceRequest() {
    }
    public AddEventConfSourceRequest(String name, String description, String vendor) {
        this.name = name;
        this.description = description;
        this.vendor = vendor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}

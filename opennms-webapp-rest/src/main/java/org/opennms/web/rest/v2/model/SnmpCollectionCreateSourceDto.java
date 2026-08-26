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

import java.util.ArrayList;
import java.util.List;

@Schema(description = """
        Request body for creating an empty SNMP data collection source. Both fields are required.""")
public class SnmpCollectionCreateSourceDto {
    @Schema(description = "Source name, unique across sources. Surrounding whitespace is trimmed. This "
            + "becomes the `<datacollection-group name=...>` of the source and the value profiles "
            + "reference in `sourceNames`.",
            example = "Acme Packet", required = true)
    private String name;

    @Schema(description = "Names of existing profiles to attach the new source to. At least one is "
            + "required, and every name has to already exist.",
            example = "[\"default\"]", required = true)
    private List<String> profiles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        if (profiles == null) {
            this.profiles.clear();
        } else {
            this.profiles = profiles;
        }
    }
}

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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A value collected from an XML group.")
public class XmlObjectDto {

    @Schema(description = "Name of the attribute, used as data source name.", example = "status")
    private String name;

    @Schema(description = "Attribute type: gauge, counter or string.", example = "gauge")
    private String type;

    @Schema(description = "XPath (or JSONPath) of the value, relative to the resource element.", example = "status")
    private String xpath;

    @Schema(description = "Value mappings applied before the value is stored.")
    private List<XmlMappingDto> mappings;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(final String xpath) {
        this.xpath = xpath;
    }

    public List<XmlMappingDto> getMappings() {
        return mappings;
    }

    public void setMappings(final List<XmlMappingDto> mappings) {
        this.mappings = mappings;
    }
}

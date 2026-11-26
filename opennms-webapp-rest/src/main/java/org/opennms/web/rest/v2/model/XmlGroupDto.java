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

@Schema(description = "A group of values collected for one resource type.")
public class XmlGroupDto {

    @Schema(description = "Name of the group, unique within a source.", example = "cluster-stats")
    private String name;

    @Schema(description = "Resource type the values are stored under: node or a generic resource type.", example = "node")
    private String resourceType;

    @Schema(description = "XPath selecting one element per resource.", example = "/")
    private String resourceXpath;

    @Schema(description = "XPath, relative to the resource element, of the resource instance name.")
    private String keyXpath;

    @Schema(description = "XPath of the timestamp of the collected values.")
    private String timestampXpath;

    @Schema(description = "Format of the timestamp, as understood by java.text.SimpleDateFormat.")
    private String timestampFormat;

    @Schema(description = "XPaths whose values are joined to build the resource instance name; used instead of keyXpath.")
    private List<String> resourceKeys;

    @Schema(description = "Values collected for each resource.")
    private List<XmlObjectDto> objects;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceXpath() {
        return resourceXpath;
    }

    public void setResourceXpath(final String resourceXpath) {
        this.resourceXpath = resourceXpath;
    }

    public String getKeyXpath() {
        return keyXpath;
    }

    public void setKeyXpath(final String keyXpath) {
        this.keyXpath = keyXpath;
    }

    public String getTimestampXpath() {
        return timestampXpath;
    }

    public void setTimestampXpath(final String timestampXpath) {
        this.timestampXpath = timestampXpath;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(final String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public List<String> getResourceKeys() {
        return resourceKeys;
    }

    public void setResourceKeys(final List<String> resourceKeys) {
        this.resourceKeys = resourceKeys;
    }

    public List<XmlObjectDto> getObjects() {
        return objects;
    }

    public void setObjects(final List<XmlObjectDto> objects) {
        this.objects = objects;
    }
}

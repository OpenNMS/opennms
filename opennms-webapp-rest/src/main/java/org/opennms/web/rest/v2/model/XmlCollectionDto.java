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

@Schema(description = "An XML data collection as referenced by the collection parameter of a collectd service.")
public class XmlCollectionDto {

    @Schema(description = "Unique name of the collection.", example = "xml-elasticsearch-cluster-stats")
    private String name;

    @Schema(description = "RRD layout of the collected data.")
    private XmlRrdDto rrd;

    @Schema(description = "Documents collected by this collection.")
    private List<XmlSourceDto> sources;

    @Schema(description = "Version of the collection as read, the same value as the ETag. Ignored on writes.")
    private String version;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public XmlRrdDto getRrd() {
        return rrd;
    }

    public void setRrd(final XmlRrdDto rrd) {
        this.rrd = rrd;
    }

    public List<XmlSourceDto> getSources() {
        return sources;
    }

    public void setSources(final List<XmlSourceDto> sources) {
        this.sources = sources;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}

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

@Schema(description = "The complete XML data collection configuration, as downloaded and uploaded.")
public class XmlCollectionConfigDto {

    @Schema(description = "Directory the collected data is written to when RRD storage is used.", example = "/opt/opennms/share/rrd/snmp/")
    private String rrdRepository;

    @Schema(description = "Every XML data collection.")
    private List<XmlCollectionDto> collections;

    @Schema(description = "Every shared group file.")
    private List<XmlGroupFileDto> groupFiles;

    public String getRrdRepository() {
        return rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        this.rrdRepository = rrdRepository;
    }

    public List<XmlCollectionDto> getCollections() {
        return collections;
    }

    public void setCollections(final List<XmlCollectionDto> collections) {
        this.collections = collections;
    }

    public List<XmlGroupFileDto> getGroupFiles() {
        return groupFiles;
    }

    public void setGroupFiles(final List<XmlGroupFileDto> groupFiles) {
        this.groupFiles = groupFiles;
    }
}

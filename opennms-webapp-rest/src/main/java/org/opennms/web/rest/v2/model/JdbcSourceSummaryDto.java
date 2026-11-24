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

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A JDBC data collection source without its collections, for list views. A source is one stored configuration, formerly one XML file.")
public class JdbcSourceSummaryDto {

    @Schema(description = "Id of the source. `default` is the former `jdbc-datacollection-config.xml`, every other id a former file of `jdbc-datacollection.d/`.", example = "postgresql")
    private String id;

    @Schema(description = "Directory the collected data is written to.", example = "${install.share.dir}/rrd/snmp/")
    private String rrdRepository;

    @Schema(description = "Number of collections in the source.", example = "1")
    private Integer collectionCount;

    @Schema(description = "Names of the collections in the source, in stored order.")
    private List<String> collectionNames = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getRrdRepository() {
        return rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        this.rrdRepository = rrdRepository;
    }

    public Integer getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(final Integer collectionCount) {
        this.collectionCount = collectionCount;
    }

    public List<String> getCollectionNames() {
        return collectionNames;
    }

    public void setCollectionNames(final List<String> collectionNames) {
        this.collectionNames = collectionNames;
    }
}

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

@Schema(description = "A JDBC data collection source with all of its collections.")
public class JdbcSourceDto {

    @Schema(description = "Id of the source; letters, digits, `.`, `_` and `-`.", example = "postgresql")
    private String id;

    @Schema(description = "Directory the collected data is written to.", example = "${install.share.dir}/rrd/snmp/")
    private String rrdRepository;

    @Schema(description = "Read only: the repository the collector actually uses. The collector uses the repository of the first source that sets one, normally `default`, for all collections.", example = "${install.share.dir}/rrd/snmp/")
    private String effectiveRrdRepository;

    @Schema(description = "The collections, in stored order.")
    private List<JdbcCollectionDto> collections = new ArrayList<>();

    @Schema(description = "Entity tag of the source, the same value as the `ETag` header. Ignored on writes.")
    private String version;

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

    public String getEffectiveRrdRepository() {
        return effectiveRrdRepository;
    }

    public void setEffectiveRrdRepository(final String effectiveRrdRepository) {
        this.effectiveRrdRepository = effectiveRrdRepository;
    }

    public List<JdbcCollectionDto> getCollections() {
        return collections;
    }

    public void setCollections(final List<JdbcCollectionDto> collections) {
        this.collections = collections;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}

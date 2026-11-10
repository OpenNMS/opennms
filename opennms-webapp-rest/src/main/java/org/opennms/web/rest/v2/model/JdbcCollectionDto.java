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

@Schema(description = "A JDBC collection: the queries a JdbcCollector service runs, and how the results are stored. A collectd service selects it with its `collection` parameter.")
public class JdbcCollectionDto {

    @Schema(description = "Name of the collection, unique across all sources.", example = "PostgreSQL")
    private String name;

    @Schema(description = "RRD step in seconds.", example = "300")
    private Integer rrdStep;

    @Schema(description = "RRA definitions.")
    private List<String> rras = new ArrayList<>();

    @Schema(description = "The queries, in the order they run.")
    private List<JdbcQueryDto> queries = new ArrayList<>();

    @Schema(description = "Read only: collectd services that use the collection, as `package/service`.")
    private List<String> usedBy = new ArrayList<>();

    @Schema(description = "Read only: id of an earlier source that defines a collection of the same name. When set, the collector ignores this collection.")
    private String shadowedBy;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getRrdStep() {
        return rrdStep;
    }

    public void setRrdStep(final Integer rrdStep) {
        this.rrdStep = rrdStep;
    }

    public List<String> getRras() {
        return rras;
    }

    public void setRras(final List<String> rras) {
        this.rras = rras;
    }

    public List<JdbcQueryDto> getQueries() {
        return queries;
    }

    public void setQueries(final List<JdbcQueryDto> queries) {
        this.queries = queries;
    }

    public List<String> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(final List<String> usedBy) {
        this.usedBy = usedBy;
    }

    public String getShadowedBy() {
        return shadowedBy;
    }

    public void setShadowedBy(final String shadowedBy) {
        this.shadowedBy = shadowedBy;
    }
}

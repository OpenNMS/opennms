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

@Schema(description = "A query of a JDBC collection.")
public class JdbcQueryDto {

    @Schema(description = "Name of the query, unique in its collection. Also the attribute group of the collected values.", example = "pg_dead_tuples")
    private String name;

    @Schema(description = "Interval in milliseconds after which the availability of the query is checked again. 0 checks every time.", example = "0")
    private Integer recheckInterval;

    @Schema(description = "Interface types the query applies to, usually `all` or `ignore`.", example = "ignore")
    private String ifType;

    @Schema(description = "Resource type of the rows when the query returns more than one row.", example = "pgDatabase")
    private String resourceType;

    @Schema(description = "Column holding the resource instance when the query returns more than one row.", example = "datname")
    private String instanceColumn;

    @Schema(description = "The SQL statement.", example = "SELECT COUNT(*) AS node_count FROM node")
    private String queryString;

    @Schema(description = "The result columns to store.")
    private List<JdbcColumnDto> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getRecheckInterval() {
        return recheckInterval;
    }

    public void setRecheckInterval(final Integer recheckInterval) {
        this.recheckInterval = recheckInterval;
    }

    public String getIfType() {
        return ifType;
    }

    public void setIfType(final String ifType) {
        this.ifType = ifType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getInstanceColumn() {
        return instanceColumn;
    }

    public void setInstanceColumn(final String instanceColumn) {
        this.instanceColumn = instanceColumn;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(final String queryString) {
        this.queryString = queryString;
    }

    public List<JdbcColumnDto> getColumns() {
        return columns;
    }

    public void setColumns(final List<JdbcColumnDto> columns) {
        this.columns = columns;
    }
}

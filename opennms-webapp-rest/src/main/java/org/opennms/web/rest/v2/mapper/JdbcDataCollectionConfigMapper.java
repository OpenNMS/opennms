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
package org.opennms.web.rest.v2.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.opennms.netmgt.config.jdbc.JdbcRrd;
import org.opennms.netmgt.config.jdbc.JdbcStatement;
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcColumnDto;
import org.opennms.web.rest.v2.model.JdbcQueryDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;
import org.opennms.web.rest.v2.model.JdbcSourceSummaryDto;

/**
 * Converts the JDBC data collection model to the DTOs of the REST API and back.
 *
 * <p>{@code toEntity} trims names and turns blank optional values into absent ones, so that what is stored
 * matches what a hand-written XML file would hold. It expects a DTO that passed the validator.</p>
 */
public final class JdbcDataCollectionConfigMapper {

    private JdbcDataCollectionConfigMapper() {
    }

    public static JdbcSourceSummaryDto toSummaryDto(final String id, final JdbcDataCollectionConfig config) {
        final JdbcSourceSummaryDto dto = new JdbcSourceSummaryDto();
        dto.setId(id);
        dto.setRrdRepository(config.getRrdRepository());
        dto.setCollectionCount(config.getJdbcDataCollections().size());
        dto.setCollectionNames(config.getJdbcDataCollections().stream()
                .map(JdbcDataCollection::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * @return the source without the read-only fields ({@code version}, {@code effectiveRrdRepository},
     *         {@code usedBy}, {@code shadowedBy}), which the caller fills in
     */
    public static JdbcSourceDto toDto(final String id, final JdbcDataCollectionConfig config) {
        final JdbcSourceDto dto = new JdbcSourceDto();
        dto.setId(id);
        dto.setRrdRepository(config.getRrdRepository());
        dto.setCollections(config.getJdbcDataCollections().stream()
                .map(JdbcDataCollectionConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static JdbcCollectionDto toDto(final JdbcDataCollection collection) {
        final JdbcCollectionDto dto = new JdbcCollectionDto();
        dto.setName(collection.getName());
        final JdbcRrd rrd = collection.getJdbcRrd();
        if (rrd != null) {
            dto.setRrdStep(rrd.getStep());
            dto.setRras(new ArrayList<>(rrd.getJdbcRras()));
        }
        dto.setQueries(collection.getQueries().stream()
                .map(JdbcDataCollectionConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static JdbcQueryDto toDto(final JdbcQuery query) {
        final JdbcQueryDto dto = new JdbcQueryDto();
        dto.setName(query.getQueryName());
        dto.setRecheckInterval(query.getRecheckInterval());
        dto.setIfType(query.getIfType());
        dto.setResourceType(query.getResourceType());
        dto.setInstanceColumn(query.getInstanceColumn());
        dto.setQueryString(query.getJdbcStatement() == null ? null : query.getJdbcStatement().getJdbcQuery());
        dto.setColumns(query.getJdbcColumns().stream()
                .map(JdbcDataCollectionConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static JdbcColumnDto toDto(final JdbcColumn column) {
        final JdbcColumnDto dto = new JdbcColumnDto();
        dto.setName(column.getColumnName());
        dto.setDataSourceName(column.getDataSourceName());
        dto.setType(column.getDataType() == null ? null : column.getDataType().getName());
        dto.setAlias(column.getAlias());
        return dto;
    }

    public static JdbcDataCollectionConfig toEntity(final JdbcSourceDto dto) {
        final JdbcDataCollectionConfig config = new JdbcDataCollectionConfig();
        config.setRrdRepository(blankToNull(dto.getRrdRepository()));
        final List<JdbcDataCollection> collections = new ArrayList<>();
        if (dto.getCollections() != null) {
            dto.getCollections().forEach(collection -> collections.add(toEntity(collection)));
        }
        config.setJdbcDataCollections(collections);
        return config;
    }

    public static JdbcDataCollection toEntity(final JdbcCollectionDto dto) {
        final JdbcDataCollection collection = new JdbcDataCollection();
        collection.setName(trim(dto.getName()));

        final JdbcRrd rrd = new JdbcRrd();
        rrd.setStep(dto.getRrdStep());
        rrd.setJdbcRras(dto.getRras() == null ? new ArrayList<>() : dto.getRras().stream()
                .map(JdbcDataCollectionConfigMapper::trim)
                .collect(Collectors.toList()));
        collection.setJdbcRrd(rrd);

        final List<JdbcQuery> queries = new ArrayList<>();
        if (dto.getQueries() != null) {
            dto.getQueries().forEach(query -> queries.add(toEntity(query)));
        }
        collection.setQueries(queries);
        return collection;
    }

    public static JdbcQuery toEntity(final JdbcQueryDto dto) {
        final JdbcQuery query = new JdbcQuery();
        query.setQueryName(trim(dto.getName()));
        query.setRecheckInterval(dto.getRecheckInterval() == null ? 0 : dto.getRecheckInterval());
        query.setIfType(trim(dto.getIfType()));
        query.setResourceType(blankToNull(dto.getResourceType()));
        query.setInstanceColumn(blankToNull(dto.getInstanceColumn()));

        final JdbcStatement statement = new JdbcStatement();
        // not trimmed: the statement is stored as written
        statement.setJdbcQuery(dto.getQueryString());
        query.setJdbcStatement(statement);

        final List<JdbcColumn> columns = new ArrayList<>();
        if (dto.getColumns() != null) {
            dto.getColumns().forEach(column -> columns.add(toEntity(column)));
        }
        query.setJdbcColumns(columns);
        return query;
    }

    public static JdbcColumn toEntity(final JdbcColumnDto dto) {
        final JdbcColumn column = new JdbcColumn();
        column.setColumnName(trim(dto.getName()));
        column.setDataSourceName(blankToNull(dto.getDataSourceName()));
        column.setDataType(AttributeType.parse(trim(dto.getType())));
        column.setAlias(trim(dto.getAlias()));
        return column;
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private static String blankToNull(final String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}

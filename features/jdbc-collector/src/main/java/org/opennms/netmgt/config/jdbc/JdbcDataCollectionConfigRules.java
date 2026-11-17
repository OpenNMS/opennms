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
package org.opennms.netmgt.config.jdbc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The checks a JDBC data collection configuration must pass before it is stored, beyond what the schema of the
 * configuration manager checks: values the collector cannot work without, value ranges and uniqueness.
 *
 * <p>The configuration manager runs them on every write (see
 * {@link org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigDaoCm}). The v2 REST API checks the same rules on its
 * DTOs first to name the offending field, and the UI mirrors them in
 * {@code ui/src/lib/jdbcDataCollectionValidator.ts}; keep the three in step.</p>
 */
public final class JdbcDataCollectionConfigRules {

    /** The longest datasource name RRDtool can store; the alias of a column becomes that name. */
    public static final int MAX_ALIAS_LENGTH = 19;

    /**
     * The id of a configuration becomes part of the REST path and of the file name of a download, and was the
     * name of a file in {@code jdbc-datacollection.d/}, so it is kept to what is safe in all three.
     */
    public static final Pattern SOURCE_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    public static final Pattern RRA = Pattern.compile("RRA:(AVERAGE|MIN|MAX|LAST):\\d*\\.?\\d+:\\d+:\\d+");

    private JdbcDataCollectionConfigRules() {
    }

    /**
     * @return the problem, or null when there is none
     */
    public static String validateSourceId(final String id) {
        if (isBlank(id)) {
            return "The source id is required.";
        }
        if (!SOURCE_ID.matcher(id).matches()) {
            return "Source id '" + id + "' must start with a letter or digit and contain only letters, digits, '.', '_' and '-'.";
        }
        return null;
    }

    /**
     * @return the first problem of the configuration, or null when there is none
     */
    public static String validate(final JdbcDataCollectionConfig config) {
        if (config == null) {
            return "The configuration is required.";
        }
        final Set<String> names = new HashSet<>();
        for (final JdbcDataCollection collection : orEmpty(config.getJdbcDataCollections())) {
            final String message = validateCollection(collection);
            if (message != null) {
                return message;
            }
            if (!names.add(collection.getName().trim())) {
                return "Collection '" + collection.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    static String validateCollection(final JdbcDataCollection collection) {
        if (collection == null) {
            return "The collection is required.";
        }
        if (isBlank(collection.getName())) {
            return "The collection name is required.";
        }
        final String name = collection.getName().trim();
        final JdbcRrd rrd = collection.getJdbcRrd();
        if (rrd == null || rrd.getStep() == null || rrd.getStep() < 1) {
            return "Collection '" + name + "': the RRD step must be a positive number of seconds.";
        }
        final List<String> rras = rrd.getJdbcRras();
        if (rras == null || rras.isEmpty()) {
            return "Collection '" + name + "': at least one RRA is required.";
        }
        for (final String rra : rras) {
            if (rra == null || !RRA.matcher(rra.trim()).matches()) {
                return "Collection '" + name + "': RRA '" + rra + "' is not of the form RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>.";
            }
        }
        final Set<String> queryNames = new HashSet<>();
        final List<JdbcQuery> queries = orEmpty(collection.getQueries());
        for (int i = 0; i < queries.size(); i++) {
            final JdbcQuery query = queries.get(i);
            final String message = validateQuery(query);
            if (message != null) {
                return "Collection '" + name + "', query " + describeQuery(query, i) + ": " + message;
            }
            if (!queryNames.add(query.getQueryName().trim())) {
                return "Collection '" + name + "': query '" + query.getQueryName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    private static String validateQuery(final JdbcQuery query) {
        if (query == null) {
            return "the query is required.";
        }
        if (isBlank(query.getQueryName())) {
            return "the name is required.";
        }
        if (query.getRecheckInterval() < 0) {
            return "the recheck interval must not be negative.";
        }
        if (isBlank(query.getIfType())) {
            return "the interface type is required.";
        }
        if (query.getJdbcStatement() == null || isBlank(query.getJdbcStatement().getJdbcQuery())) {
            return "the SQL statement is required.";
        }
        // A query returning several rows needs both to build one resource per row.
        if (isBlank(query.getResourceType()) != isBlank(query.getInstanceColumn())) {
            return "the resource type and the instance column must be set together.";
        }
        final List<JdbcColumn> columns = orEmpty(query.getJdbcColumns());
        if (columns.isEmpty()) {
            return "at least one column is required.";
        }
        final Set<String> aliases = new HashSet<>();
        for (int i = 0; i < columns.size(); i++) {
            final JdbcColumn column = columns.get(i);
            final String message = validateColumn(column);
            if (message != null) {
                return "column " + (i + 1) + ": " + message;
            }
            if (!aliases.add(column.getAlias().trim())) {
                return "alias '" + column.getAlias().trim() + "' is used by more than one column.";
            }
        }
        return null;
    }

    private static String validateColumn(final JdbcColumn column) {
        if (column == null) {
            return "the column is required.";
        }
        if (isBlank(column.getColumnName())) {
            return "the name is required.";
        }
        if (column.getDataType() == null) {
            return "the type is required, one of gauge, counter or string.";
        }
        if (isBlank(column.getAlias())) {
            return "the alias is required.";
        }
        if (column.getAlias().trim().length() > MAX_ALIAS_LENGTH) {
            return "alias '" + column.getAlias().trim() + "' is longer than " + MAX_ALIAS_LENGTH
                    + " characters, the longest datasource name RRDtool can store.";
        }
        return null;
    }

    private static String describeQuery(final JdbcQuery query, final int index) {
        return query == null || isBlank(query.getQueryName()) ? String.valueOf(index + 1) : "'" + query.getQueryName().trim() + "'";
    }

    private static <T> List<T> orEmpty(final List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}

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
package org.opennms.web.rest.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcColumnDto;
import org.opennms.web.rest.v2.model.JdbcQueryDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;

/**
 * Checks of the JDBC data collection DTOs that the schema of the configuration manager does not make: required
 * values the collector cannot work without, value ranges and uniqueness.
 *
 * <p>Every method returns the first problem as a message for the client, or null when there is none. The UI
 * mirrors these rules in {@code ui/src/lib/jdbcDataCollectionValidator.ts}; keep the two in step.</p>
 */
public final class JdbcDataCollectionConfigValidator {

    /** The longest datasource name RRDtool can store; the alias of a column becomes that name. */
    public static final int MAX_ALIAS_LENGTH = 19;

    /** The id becomes part of the REST path and was a file name, so it is kept to what is safe in both. */
    private static final Pattern SOURCE_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    private static final Pattern RRA = Pattern.compile("RRA:(AVERAGE|MIN|MAX|LAST):\\d*\\.?\\d+:\\d+:\\d+");

    private JdbcDataCollectionConfigValidator() {
    }

    public static String validateSourceId(final String id) {
        if (isBlank(id)) {
            return "The source id is required.";
        }
        if (!SOURCE_ID.matcher(id).matches()) {
            return "Source id '" + id + "' must start with a letter or digit and contain only letters, digits, '.', '_' and '-'.";
        }
        return null;
    }

    /** Validates a whole source, as it comes in with an upload. */
    public static String validateSource(final JdbcSourceDto source) {
        if (source == null) {
            return "The source is required.";
        }
        final List<JdbcCollectionDto> collections = source.getCollections();
        if (collections == null) {
            return null;
        }
        final Set<String> names = new HashSet<>();
        for (final JdbcCollectionDto collection : collections) {
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

    public static String validateCollection(final JdbcCollectionDto collection) {
        if (collection == null) {
            return "The collection is required.";
        }
        if (isBlank(collection.getName())) {
            return "The collection name is required.";
        }
        final String name = collection.getName().trim();
        if (collection.getRrdStep() == null || collection.getRrdStep() < 1) {
            return "Collection '" + name + "': the RRD step must be a positive number of seconds.";
        }
        if (collection.getRras() == null || collection.getRras().isEmpty()) {
            return "Collection '" + name + "': at least one RRA is required.";
        }
        for (final String rra : collection.getRras()) {
            if (rra == null || !RRA.matcher(rra.trim()).matches()) {
                return "Collection '" + name + "': RRA '" + rra + "' is not of the form RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>.";
            }
        }
        if (collection.getQueries() == null) {
            return null;
        }
        final Set<String> queryNames = new HashSet<>();
        for (int i = 0; i < collection.getQueries().size(); i++) {
            final JdbcQueryDto query = collection.getQueries().get(i);
            final String message = validateQuery(query);
            if (message != null) {
                return "Collection '" + name + "', query " + describeQuery(query, i) + ": " + message;
            }
            if (!queryNames.add(query.getName().trim())) {
                return "Collection '" + name + "': query '" + query.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    /** @return the problem without the context of the collection, which the caller adds */
    static String validateQuery(final JdbcQueryDto query) {
        if (query == null) {
            return "the query is required.";
        }
        if (isBlank(query.getName())) {
            return "the name is required.";
        }
        if (query.getRecheckInterval() != null && query.getRecheckInterval() < 0) {
            return "the recheck interval must not be negative.";
        }
        if (isBlank(query.getIfType())) {
            return "the interface type is required.";
        }
        if (isBlank(query.getQueryString())) {
            return "the SQL statement is required.";
        }
        // A query returning several rows needs both to build one resource per row.
        if (isBlank(query.getResourceType()) != isBlank(query.getInstanceColumn())) {
            return "the resource type and the instance column must be set together.";
        }
        if (query.getColumns() == null || query.getColumns().isEmpty()) {
            return "at least one column is required.";
        }
        final Set<String> aliases = new HashSet<>();
        for (int i = 0; i < query.getColumns().size(); i++) {
            final JdbcColumnDto column = query.getColumns().get(i);
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

    static String validateColumn(final JdbcColumnDto column) {
        if (column == null) {
            return "the column is required.";
        }
        if (isBlank(column.getName())) {
            return "the name is required.";
        }
        if (isBlank(column.getType()) || AttributeType.parse(column.getType().trim()) == null) {
            return "type '" + column.getType() + "' is not one of gauge, counter or string.";
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

    private static String describeQuery(final JdbcQueryDto query, final int index) {
        return query == null || isBlank(query.getName()) ? String.valueOf(index + 1) : "'" + query.getName().trim() + "'";
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}

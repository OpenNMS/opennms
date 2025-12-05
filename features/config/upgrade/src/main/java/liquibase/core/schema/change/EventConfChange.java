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
package liquibase.core.schema.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.database.jvm.JdbcConnection;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;


@DatabaseChange(name = "eventConfEventChange",
        description = "Update EventConfEvent entries by converting XML content to JSON and updating the 'content' field",
        priority = ChangeMetaData.PRIORITY_DATABASE)
public class EventConfChange extends AbstractChange {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfChange.class);

    private Map<Long, String> cachedEvents = null;
    private SqlStatement[] cachedSqlStatements = null;

    @Override
    public SqlStatement[] generateStatements(Database database) {

        // Return cached statements if already created
        if (cachedSqlStatements != null) {
            return cachedSqlStatements;
        }

        // Load events only once
        if (cachedEvents == null) {
            cachedEvents = loadEvents((JdbcConnection) database.getConnection());
        }

        // Generate SQL statements only for rows that need migration
        cachedSqlStatements = cachedEvents.entrySet()
                .stream()
                .map(entry -> createUpdateStatement(entry.getKey(), entry.getValue()))
                .toArray(SqlStatement[]::new);

        return cachedSqlStatements;
    }

    @Override
    public String getConfirmationMessage() {
        return "EventConfEvent XML content successfully converted to JSON where applicable.";
    }

    /**
     * Load events that need migration (content = '{}' and xml_content IS NOT NULL)
     */
    private Map<Long, String> loadEvents(JdbcConnection databaseConnection) {
        Map<Long, String> eventMap = new LinkedHashMap<>();
        String defaultContent = "{}";

        try {
            Connection conn = databaseConnection.getUnderlyingConnection();
            String sql = "SELECT id, xml_content FROM eventconf_events WHERE content = ?::jsonb AND xml_content IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, defaultContent);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Long id = rs.getLong("id");
                        String xmlContent = rs.getString("xml_content");
                        eventMap.put(id, xmlContent);
                    }
                }
            }

        } catch (SQLException e) {
            LOG.error("Error loading EventConfEvent rows for migration", e);
            throw new RuntimeException(e);
        }

        return eventMap;
    }

    /**
     * Create an UpdateStatement to convert XML → JSON and update the content field
     */
    private SqlStatement createUpdateStatement(Long id, String xmlContent) {
        try {
            // Unmarshal XML to Event object
            Event xmlEvent = JaxbUtils.unmarshal(Event.class, xmlContent);
            // Marshal Event object to JSON
            String jsonContent = JsonUtils.marshal(xmlEvent);

            // Prepare SQL update statement
            UpdateStatement update = new UpdateStatement(null, null, "eventconf_events");
            update.addNewColumnValue("content", jsonContent);
            update.addNewColumnValue("modified_by", "system-upgrade");
            update.addNewColumnValue("last_modified", new liquibase.statement.DatabaseFunction("CURRENT_TIMESTAMP"));
            update.setWhereClause("id=" + id);

            LOG.info("Prepared update statement for EventConfEvent id={}", id);

            return update;

        } catch (Exception e) {
            LOG.error("Failed to unmarshal XML for EventConfEvent id={}", id, e);
            throw new RuntimeException("Failed to convert XML to JSON for EventConfEvent id=" + id, e);
        }
    }
}

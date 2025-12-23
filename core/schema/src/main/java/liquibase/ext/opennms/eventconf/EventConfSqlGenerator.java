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
package liquibase.ext.opennms.eventconf;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventConfSqlGenerator extends AbstractSqlGenerator<EventConfSqlStatement> {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfSqlGenerator.class);

    @Override
    public boolean supports(EventConfSqlStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    @Override
    public ValidationErrors validate(EventConfSqlStatement statement, Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        return errors;
    }

    @Override
    public Sql[] generateSql(EventConfSqlStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {

        try {
            // Get connection from database
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            Connection conn = connection.getUnderlyingConnection();

            // Load events that need to be updated
            Map<Long, String> eventsToUpdate = new LinkedHashMap<>();
            String defaultContent = "{}";

            String selectSql = "SELECT id, xml_content FROM eventconf_events WHERE content = ?::jsonb " +
                    "AND xml_content IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, defaultContent);
                LOG.info("Querying for events with default content...");

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Long id = rs.getLong("id");
                        String xmlContent = rs.getString("xml_content");
                        eventsToUpdate.put(id, xmlContent);
                    }
                }
            }

            LOG.info("Found {} events to update", eventsToUpdate.size());

            // Generate SQL statements for each event
            List<Sql> sqlStatements = new ArrayList<>();

            String updateSql = "UPDATE eventconf_events " +
                    "SET content = ?::jsonb, " +
                    "    modified_by = 'system-upgrade', " +
                    "    last_modified = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                for (Map.Entry<Long, String> entry : eventsToUpdate.entrySet()) {
                    Long id = entry.getKey();
                    String xmlContent = entry.getValue();
                    try {
                        // Convert XML to JSON (simplified version - adjust as needed)
                        String jsonContent = convertXmlToJson(xmlContent);
                        // Execute the SQL update using a prepared statement
                        updatePs.setString(1, jsonContent);
                        updatePs.setLong(2, id);
                        updatePs.executeUpdate();
                        LOG.info("Updated eventconf_events for id={}", id);
                    } catch (Exception e) {
                        LOG.error("Failed to convert XML for id={}", id, e);
                        throw new RuntimeException(String.format("Failed to convert XML for id=%d", id), e);
                    }
                }
            }

            return sqlStatements.toArray(new Sql[0]);

        } catch (Exception e) {
            LOG.error("Database error in EventConfSqlGenerator", e);
            throw new RuntimeException("Database error in EventConfSqlGenerator", e);
        }
    }

    private String convertXmlToJson(String xmlContent) {
        try {
            Event xmlEvent = JaxbUtils.unmarshal(Event.class, xmlContent);
            return JsonUtils.marshal(xmlEvent);
        } catch (Exception e) {
            LOG.error("Failed to convert XML to JSON", e);
            throw new RuntimeException("Failed to convert XML to JSON", e);
        }
    }

    private String escapeSql(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }
}


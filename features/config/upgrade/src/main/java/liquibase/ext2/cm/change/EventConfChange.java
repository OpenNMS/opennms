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
package liquibase.ext2.cm.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.xml.eventconf.Event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@DatabaseChange(name = "eventConfEventChange",
        description = "Update EventConfEvent entries by converting XML content to JSON and updating the 'content' field",
        priority = ChangeMetaData.PRIORITY_DATABASE)
public class EventConfChange extends AbstractChange {

    private Map<Long, String> cachedEvents;

    @Override
    public String getConfirmationMessage() {
        return "EventConfEvent content updated to JSON successfully.";
    }

    public SqlStatement[] generateStatements(Database database) {
        if (cachedEvents == null) {
            cachedEvents = loadEvents((JdbcConnection) database.getConnection());
        }

        return cachedEvents.entrySet()
                .stream()
                .map(entry -> {
                    return createUpdateStatement(entry.getKey(), entry.getValue());
                })
                .toArray(SqlStatement[]::new);
    }

    private Map<Long, String> loadEvents(JdbcConnection conn) {
        Map<Long, String> eventMap = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement("SELECT id, xml_content FROM eventconf_events");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                String xmlContent = rs.getString("xml_content");
                eventMap.put(id, xmlContent);
            }
        } catch (SQLException | DatabaseException e) {
            throw new RuntimeException(e);
        }
        return eventMap;
    }

    private UpdateStatement createUpdateStatement(Long id, String xmlContent) {
        String jsonContent;
        try {
            Event xmlEvent = JaxbUtils.unmarshal(Event.class, xmlContent);
            jsonContent = JsonUtils.marshal(xmlEvent);
            if (jsonContent == null) {
                jsonContent = "{}";
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed marshall Object to JSON for id=" + id, e);
        }

        UpdateStatement update = new UpdateStatement(null, null, "eventconf_events");
        update.addNewColumnValue("content", jsonContent);
        update.addNewColumnValue("modified_by", "system-upgrade");
        update.addNewColumnValue("last_modified", new liquibase.statement.DatabaseFunction("CURRENT_TIMESTAMP"));
        update.setWhereClause("id=" + id);

        return update;
    }
}

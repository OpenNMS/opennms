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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.UpdateEventConfEventStatement;
import liquibase.statement.SqlStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@DatabaseChange(name = "eventConfEventChange",
        description = "Update EventConfEvent entries by converting XML content to JSON and updating the 'content' field",
        priority = ChangeMetaData.PRIORITY_DATABASE)
public class EventConfChange extends AbstractCmChange {
    @Override
    public String getConfirmationMessage() {
        return "EventConfEvent content updated to JSON successfully.";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (!(database instanceof CmDatabase)) {
            return new SqlStatement[0];
        }

        Map<Long, String> eventMap = loadEvents(database);

        return eventMap.entrySet()
                .stream()
                .map(e -> new UpdateEventConfEventStatement(e.getKey(), e.getValue()))
                .toArray(SqlStatement[]::new);
    }

    @Override
    protected ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        return validationErrors;
    }

    private Map<Long, String> loadEvents(Database database) {
        Map<Long, String> eventMap = new HashMap<>();
        JdbcConnection conn = (JdbcConnection) database.getConnection();

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
}

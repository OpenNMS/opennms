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
package liquibase.ext2.cm.statement;

import liquibase.database.jvm.JdbcConnection;
import liquibase.ext2.cm.database.CmDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import java.sql.PreparedStatement;
import java.util.Date;

public class UpdateEventConfEventStatement extends AbstractCmStatement {

    private final Long id;
    private final String xmlContent;

    public UpdateEventConfEventStatement(Long id, String xmlContent) {
        this.id = id;
        this.xmlContent = xmlContent;
    }

    @Override
    public void execute(CmDatabase database) {
        JdbcConnection conn = (JdbcConnection) database.getConnection();
        final String sql = "UPDATE eventconf_events SET content = ?::jsonb, modified_by=?, last_modified = CURRENT_TIMESTAMP WHERE id = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql) ) {
            Event xmlEvent = JaxbUtils.unmarshal(Event.class, xmlContent);
            String jsonContent = JsonUtils.marshal(xmlEvent);
            jsonContent = jsonContent == null ? "{}" : jsonContent;
            ps.setString(1, jsonContent);
            ps.setString(2, "system-upgrade");
            ps.setLong(3, id);
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to update EventConfEvent id=" + id, ex);
        }
    }
}

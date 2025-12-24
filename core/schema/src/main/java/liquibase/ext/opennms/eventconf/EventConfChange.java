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

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@DatabaseChange(name = "eventConfEventChange",
        description = "Update EventConfEvent entries by converting XML content to JSON and updating the 'content' field",
        priority = ChangeMetaData.PRIORITY_DATABASE)
public class EventConfChange extends AbstractChange {
    private static final Logger LOG = LoggerFactory.getLogger(EventConfChange.class);

    @Override
    public SqlStatement[] generateStatements(Database database) {

        // Create and return a custom SQL statement that will be processed by our generator
        return new SqlStatement[] {
                new EventConfSqlStatement()
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "EventConfEvent XML content successfully converted to JSON where applicable.";
    }

}
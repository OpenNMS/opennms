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
package liquibase.ext.opennms.createindex;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

@DatabaseChange(name="createIndex", description = "Creates an index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT + 1, appliesTo = "index")
public class CreateIndexWithWhereChange extends liquibase.change.core.CreateIndexChange {
    private static final Logger LOG = LogService.getLog(CreateIndexWithWhereChange.class);
    private String m_where;

    public CreateIndexWithWhereChange() {
        super();
    }

    public String getWhere() {
        return m_where;
    }

    public void setWhere(final String where) {
        m_where = where;
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        final SqlStatement[] superStatements = super.generateStatements(database);
        if (m_where == null) return superStatements;

        if (superStatements.length != 1) {
            LOG.warning("expected 1 create index statement, but got " + superStatements.length);
            return superStatements;
        }

        return new SqlStatement[]{
                new CreateIndexWithWhereStatement((CreateIndexStatement)superStatements[0], m_where)
        };
    }

}

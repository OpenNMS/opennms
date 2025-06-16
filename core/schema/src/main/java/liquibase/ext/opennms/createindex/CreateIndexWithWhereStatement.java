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

import liquibase.change.AddColumnConfig;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

public class CreateIndexWithWhereStatement extends CreateIndexStatement implements SqlStatement {

    private String m_where;

    public CreateIndexWithWhereStatement(final String indexName, final String tableCatalogName, final String tableSchemaName, final String tableName, final Boolean isUnique, final String associatedWith, final AddColumnConfig... columns) {
        super(indexName, tableCatalogName, tableSchemaName, tableName, isUnique, associatedWith, columns);
    }

    public CreateIndexWithWhereStatement(final CreateIndexStatement statement, final String where) {
        this(statement.getIndexName(), statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), statement.isUnique(), statement.getAssociatedWith(), statement.getColumns());
        m_where = where;
    }

    public String getWhere() {
        return m_where;
    }

    public CreateIndexWithWhereStatement setWhere(final String where) {
        m_where = where;
        return this;
    }
}

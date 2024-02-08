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
package liquibase.ext.opennms.autoincrement;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeNote;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;

@DatabaseChange(name = "addAutoIncrement",
description = "Converts an existing column to be an auto-increment (a.k.a 'identity') column",
priority = ChangeMetaData.PRIORITY_DEFAULT + 1, appliesTo = "column",
databaseNotes = {@DatabaseChangeNote(
    database = "sqlite", notes = "If the column type is not INTEGER it is converted to INTEGER"
)}
)
public class AddNamedAutoIncrementChange extends AddAutoIncrementChange {

    private String m_sequenceName;

    public AddNamedAutoIncrementChange() {
        super();
    }

    public String getSequenceName() {
        return m_sequenceName;
    }

    public void setSequenceName(final String sequenceName) {
        m_sequenceName = sequenceName;
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        final List<SqlStatement> statements = new ArrayList<>();
        if (database instanceof PostgresDatabase) {
            String sequenceName = m_sequenceName;
            if (m_sequenceName == null) {
                sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
                statements.add(new CreateSequenceStatement(getCatalogName(), getSchemaName(), sequenceName));
            }
            statements.add(new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), null, false));
            statements.add(new AddDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), new DatabaseFunction("NEXTVAL('"+sequenceName+"')")));
            return statements.toArray(new SqlStatement[0]);
        } else {
            return super.generateStatements(database);
        }
    }

}

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
package liquibase.ext.opennms.dropconstraint;

import java.util.ArrayList;
import java.util.List;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintCascadeStatement extends DropForeignKeyConstraintStatement {

    private Boolean m_cascade;

    public DropForeignKeyConstraintCascadeStatement(final DropForeignKeyConstraintStatement statement, Boolean cascade) {
        super(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName(), statement.getConstraintName());
        m_cascade = cascade;
    }

    public DropForeignKeyConstraintCascadeStatement setCascade(final Boolean cascade) {
        m_cascade = cascade;
        return this;
    }

    public Boolean getCascade() {
        return m_cascade;
    }

    public static SqlStatement[] createFromSqlStatements(final SqlStatement[] superSql, final Boolean cascade) {
        final List<SqlStatement> statements = new ArrayList<>();

        for (final SqlStatement statement : superSql) {
            if (statement instanceof DropForeignKeyConstraintStatement) {
                statements.add(new DropForeignKeyConstraintCascadeStatement((DropForeignKeyConstraintStatement)statement, cascade));
            } else {
                statements.add(statement);
            }
        }
        return statements.toArray(new SqlStatement[0]);
    }
}

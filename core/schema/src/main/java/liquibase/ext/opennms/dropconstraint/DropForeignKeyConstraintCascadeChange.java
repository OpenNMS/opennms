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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="dropForeignKeyConstraint", description = "Drops an existing foreign key", priority = ChangeMetaData.PRIORITY_DEFAULT + 1, appliesTo = "foreignKey")
public class DropForeignKeyConstraintCascadeChange extends DropForeignKeyConstraintChange {

    private String m_cascade = "false";

    public DropForeignKeyConstraintCascadeChange() {
        super();
    }

    public String getCascade() {
        return m_cascade;
    }

    public void setCascade(final String cascade) {
        m_cascade = cascade;
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        return DropForeignKeyConstraintCascadeStatement.createFromSqlStatements(super.generateStatements(database), Boolean.valueOf(m_cascade));
    }

}

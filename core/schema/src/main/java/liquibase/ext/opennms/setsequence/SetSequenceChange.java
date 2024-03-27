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
package liquibase.ext.opennms.setsequence;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="setSequence", description="Set Sequence", priority=ChangeMetaData.PRIORITY_DEFAULT)
public class SetSequenceChange extends AbstractChange {
    private String m_schemaName;
    private String m_tableName;
    private String m_column;
    private String m_sequenceName;
    private Integer m_value;

    public SetSequenceChange() {
        super();
    }

    public void setSchemaName(final String schemaName) {
        m_schemaName = schemaName;
    }
    
    public String getSchemaName() {
        return m_schemaName;
    }

    public void setTableName(final String tableName) {
        m_tableName = tableName;
    }

    public String getTableName() {
        return m_tableName;
    }

    public void setColumn(final String column) {
        m_column = column;
    }
    
    public String getColumn() {
        return m_column;
    }

    public void setSequenceName(final String sequenceName) {
        m_sequenceName = sequenceName;
    }

    public String getSequenceName() {
        return m_sequenceName;
    }

    public void setValue(final String value) {
        m_value = value == null? null : Integer.valueOf(value);
    }

    public String getValue() {
        return m_value == null? null : m_value.toString();
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        final SetSequenceStatement statement = new SetSequenceStatement(getSequenceName());
        statement.setValue(m_value);
        statement.addTable(m_tableName, m_schemaName, m_column);
        return new SqlStatement[] { statement };
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + m_sequenceName + " updated";
    }

}

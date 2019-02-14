/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package liquibase.ext.opennms.setsequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import liquibase.statement.SqlStatement;

public class SetSequenceStatement implements SqlStatement {
	private final String m_sequenceName;
	private final List<String> m_tables = new ArrayList<String>();
	private Map<String, String> m_columns = new LinkedHashMap<String, String>();
	private Map<String, String> m_schemas = new LinkedHashMap<String, String>();
	private Integer m_value;

	public SetSequenceStatement(final String sequenceName) {
		m_sequenceName = sequenceName;
	}

        @Override
	public boolean skipOnUnsupported() {
		return true;
	}

	public String getSequenceName() {
		return m_sequenceName;
	}

	public List<String> getTables() {
		return m_tables;
	}

	public Map<String,String> getColumns() {
		return m_columns;
	}

	public Map<String,String> getSchemas() {
		return m_schemas;
	}
	
	public Integer getValue() {
		return m_value;
	}

	public SetSequenceStatement setValue(final Integer value) {
		m_value = value;
		return this;
	}
	
	SetSequenceStatement addTable(final String name, final String column) {
		getTables().add(name);
		getColumns().put(name, column);
		return this;
	}

	SetSequenceStatement addTable(final String name, final String schemaName, final String column) {
		getTables().add(name);
		getColumns().put(name, column);
		getSchemas().put(name, schemaName);
		return this;
	}
	
        @Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("sequenceName", m_sequenceName)
			.append("value", m_value)
			.append("tables", m_tables)
			.append("columns", m_columns)
			.append("schemas", m_schemas)
			.toString();
	}
}

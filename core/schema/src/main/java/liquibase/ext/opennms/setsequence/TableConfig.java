/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

public class TableConfig {

	private String m_name;
	private String m_schemaName;
	private String m_column;

	public TableConfig() {
	}

	public TableConfig(final String name, final String schemaName, final String column) {
		m_name = name;
		m_schemaName = schemaName;
		m_column = column;
	}

	public void setName(final String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setSchemaName(final String schemaName) {
		m_schemaName = schemaName;
	}

	public String getSchemaName() {
		return m_schemaName;
	}

	public void setColumn(final String column) {
		m_column = column;
	}

	public String getColumn() {
		return m_column;
	}
}

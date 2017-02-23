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

package org.opennms.netmgt.config.api;

import java.util.List;

import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.filter.Table;

/**
 * An interface for DatabaseSchemaConfigFactory
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public interface DatabaseSchemaConfig {
	
	/**
	 * <p>getDatabaseSchema</p>
	 * 
	 * @return a DatabaseSchema
	 */
	DatabaseSchema getDatabaseSchema();
	
	/**
	 * <p>getPrimaryTable</p>
	 * 
	 * @return a Table
	 */
	Table getPrimaryTable();
	
	/**
	 * <p>getTableByName</p>
	 * 
	 * @param a String
	 * @return a Table
	 */
	Table getTableByName(final String name);
	
	/**
	 * <p>findTableByVisibleColumn</p>
	 * 
	 * @param a String
	 * @return a Table
	 */
	Table findTableByVisibleColumn(final String colName);

	/**
	 * <p>getTableCount</p>
	 * 
	 * @return an int
	 */
	int getTableCount();
	
	/**
	 * <p>getJoinTables</p>
	 * 
	 * @param a List of Tables
	 * @return a List of Strings
	 */
	List<String> getJoinTables(final List<Table> tables);
	
	/**
	 * <p>constructJoinExprForTables</p>
	 * 
	 * @param a List of Tables
	 * @return a String
	 */
	String constructJoinExprForTables(final List<Table> tables);

	String addColumn(List<Table> tables, String column);
}

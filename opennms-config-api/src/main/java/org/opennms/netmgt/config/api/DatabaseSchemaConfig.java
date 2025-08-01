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

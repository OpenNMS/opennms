/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This OpenNMS capsd plugin checks if a given server is running a server that
 * can talk JDBC on a given interface. If true then the interface is "saved" for
 * future service state checking. This plugin is slow; Stablishing a connection
 * between the client and the server is an slow operation. A connection pool
 * doesn't make any sense when discovering a database, Also opening and closing
 * a connection every time helps to discover problems like a RDBMS running out
 * of connections.
 * <p>
 * More plugin information available at: <a
 * href="http://www.opennms.org/users/docs/docs/html/devref.html">OpenNMS
 * developer site </a>
 * </p>
 *
 * @author Jose Vicente Nunez Zuleta (josevnz@users.sourceforge.net) - RHCE,
 *         SJCD, SJCP
 * @version 0.1 - 07/22/2002
 * @since 0.1
 */
public final class JDBCStoredProcedurePlugin extends JDBCPlugin {
    
    private static final Logger LOG = LoggerFactory.getLogger(JDBCStoredProcedurePlugin.class);
	/**
	 * The stored procedure checked by the plugin
	 */
	private final static String DEFAULT_STORED_PROCEDURE = "isRunning";

	/**
	 * Class constructor. Load the JDBC drivers.
	 */
	public JDBCStoredProcedurePlugin() {
		super();
		LOG.info("JDBCStoredProcedurePlugin class loaded");
	}

	/** {@inheritDoc} */
        @Override
	public boolean checkStatus(Connection con, Map<String, Object> qualifiers) {
		boolean status = false;
		CallableStatement cs = null;
		try {
			String storedProcedure = ParameterMap.getKeyedString(qualifiers, "stored-procedure", DEFAULT_STORED_PROCEDURE);
			String procedureCall = "{ ? = call test." + storedProcedure + "()}";
			cs = con.prepareCall(procedureCall);
			LOG.debug("Calling stored procedure: {}", procedureCall);
			cs.registerOutParameter(1, java.sql.Types.BIT);
			cs.executeUpdate();
			status = cs.getBoolean(1);
			LOG.debug("Stored procedure returned: {}", status);
		} catch (final SQLException sqlEx) {
		    LOG.debug("JDBC stored procedure call not functional: {}", sqlEx.getSQLState(), sqlEx);
		} finally {
			closeStmt(cs);
		}
		return status;
	}

} // End of class


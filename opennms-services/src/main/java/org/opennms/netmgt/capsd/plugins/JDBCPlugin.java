//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.DBTools;
import org.opennms.netmgt.capsd.AbstractPlugin;

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
public class JDBCPlugin extends AbstractPlugin {
    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "JDBC";

    /**
     * Default number of retries for TCP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * Class constructor. Load the JDBC drivers.
     */
    public JDBCPlugin() {
        super();
        log().info("JDBCPlugin class loaded");
    }

	/**
	 * <p>log</p>
	 *
	 * @return a {@link org.opennms.core.utils.ThreadCategory} object.
	 */
	protected ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

    /**
     * Checks if a given server is listening o a given interface
     * @param hostname
     *            name of the RDBMS server
     * @param user
     *            Database user
     * @param password
     *            Database password
     * @param db_url
     *            Database connection url
     * @param timeout
     *            Default login timeout
     * @param retries
     *            Number of retrys before giving up a connection attempts
     * @param db_driver
     *            JDBC driver to use
     * 
     * @see DBTools#constructUrl
     */

    private boolean isServer(String hostname, Map<String, Object> qualifiers) {
    	
        String user = ParameterMap.getKeyedString(qualifiers, "user", DBTools.DEFAULT_DATABASE_USER);
        String password = ParameterMap.getKeyedString(qualifiers, "password", DBTools.DEFAULT_DATABASE_PASSWORD);
        String db_url = ParameterMap.getKeyedString(qualifiers, "url", DBTools.DEFAULT_URL);
        int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
        String db_driver = ParameterMap.getKeyedString(qualifiers, "driver", DBTools.DEFAULT_JDBC_DRIVER);


        boolean status = false;
        Connection con = null;
        Statement statement = null;
        boolean connected = false;

        for (int attempts = 0; attempts <= retries && !connected;) {
            log().info("Trying to detect JDBC server on '" + hostname + "', attempts #: " + attempts);

            try {

                log().debug("Loading JDBC driver: '" + db_driver + "'");
                Driver driver = (Driver)Class.forName(db_driver).newInstance();
                log().debug("JDBC driver loaded: '" + db_driver + "'");

                String url = DBTools.constructUrl(db_url, hostname);
                log().debug("Constructed JDBC url: '" + url + "'");

                Properties props = new Properties();
                props.setProperty("user", user);
                props.setProperty("password", password);
                props.setProperty("timeout", String.valueOf(timeout/1000));
                con = driver.connect(url, props);
                connected = true;
                log().debug("Got database connection: '" + con + "' (" + url + ", " + user + ", " + password + ")");
                
                status = checkStatus(con, qualifiers);

                if (status)
                	log().info("JDBC server detected on: '" + hostname + "', attempts #:" + attempts);
                
            } catch (Exception e) {
                log().info(e.getMessage());
                e.printStackTrace();
            } finally {
                attempts++;
                closeStmt(statement);
                closeConn(con);
            }
        }
        return status;
    }
    
    /**
     * <p>checkStatus</p>
     *
     * @param con a {@link java.sql.Connection} object.
     * @param qualifiers a {@link java.util.Map} object.
     * @return a boolean.
     */
    public boolean checkStatus(Connection con, Map<String, Object> qualifiers )
    {
    	boolean status = false;
    	ResultSet result = null;
    	try
    	{
    		DatabaseMetaData metadata = con.getMetaData();
    		log().debug("Got database metadata");

    		result = metadata.getCatalogs();
    		while (result.next())
    		{
    			result.getString(1);
    			log().debug("Metadata catalog: '" + result.getString(1) + "'");
    		}

    		// The JDBC server was detected using JDBC, update the status
    		if ( result != null )
    			status = true;
    	}
    	catch ( SQLException sqlException )
    	{
    		log().warn(sqlException.toString());
    	}
    	finally
    	{
    		closeResult(result);
    	}
    	return status;
    }


	private void closeConn(Connection con) {
		if (con != null) {
		    try {
		        con.close();
		    } catch (SQLException ignore) {
		    }
		}
	}

	/**
	 * <p>closeStmt</p>
	 *
	 * @param statement a {@link java.sql.Statement} object.
	 */
	protected void closeStmt(Statement statement) {
		if (statement != null) {
		    try {
		        statement.close();
		    } catch (SQLException ignore) {
		    }
		}
	}

	private void closeResult(ResultSet result) {
		if (result != null) {
		    try {
		        result.close();
		    } catch (SQLException ignore) {
		    }
		}
	}

    /**
     * Returns the default protocol name
     *
     * @return String Protocol Name
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Default checking method, asuming all the parameters by default. This
     * method is likely to skip some machines because the default password is
     * empty. is recomended to use the parametric method instead (unless your
     * DBA is dummy enugh to leave a JDBC server with no password!!!).
     */
    public boolean isProtocolSupported(InetAddress address) {
        boolean status = false;

        try {
            status = isServer(address.getCanonicalHostName(), new HashMap<String, Object>());
        } catch (Exception exp) {
            log().error(exp.getMessage());
        }
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * Checking method, receives all the parameters as a Map. Currently
     * supported:
     * <ul>
     * <li><b>port </b>- Port where the JDBC server is listening (defaults to
     * DEFAULT_PORT). Type: Integer
     * <li><b>user </b>- Database user (defaults to DEFAULT_DATABASE_USER if
     * not provided). Type String
     * <li><b>password </b>- Database password (defaults to
     * DEFAULT_DATABASE_PASSWORD). Type String
     * <li><b>timeout </b>- Timeout
     * <li><b>retry </b>- How many times will try to check for the service
     * </ul>
     */
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        boolean status = false;

        if (address == null) {
            throw new NullPointerException(getClass().getName() + ": Internet address cannot be null");
        }
        if (qualifiers == null) {
            throw new NullPointerException(getClass().getName() + ": Map argument cannot be null");
        }

        try {
            status = isServer(address.getCanonicalHostName(), qualifiers);
        } catch (Exception exp) {
            log().error(exp.getMessage());
        }
        return status;
    }

} // End of class

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.DBTools;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This OpenNMS capsd plugin checks if a given server is running a server that can talk
 * JDBC on a given interface. If true then the interface is "saved" for future
 * service state checking.
 * This plugin is slow; Stablishing a connection between the client and the
 * server is an slow operation. A connection pool doesn't make any sense when
 * discovering a database, Also opening and closing a connection every time helps to discover problems like a RDBMS running out of connections.
 * <p>More plugin information available at: <a href="http://www.opennms.org/users/docs/docs/html/devref.html">OpenNMS developer site</a></p>
 * @author Jose Vicente Nunez Zuleta (josevnz@users.sourceforge.net) - RHCE, SJCD, SJCP
 * @version 0.1 - 07/22/2002
 * @since 0.1
 */
public final class JDBCPlugin 
	extends AbstractPlugin 
{
        /**
         * The protocol supported by the plugin
         */
        private final static String     PROTOCOL_NAME   = "JDBC";

        /**
         * Default number of retries for TCP requests
         */
        private final static int        DEFAULT_RETRY   = 0;

        /**
         * Default timeout (in milliseconds) for TCP requests
         */
        private final static int        DEFAULT_TIMEOUT = 5000; // in milliseconds


  	/**
   	* Class constructor. Load the JDBC drivers.
   	*/
  	public JDBCPlugin() 
	{
    	Category log = ThreadCategory.getInstance(getClass());
    		log.info(getClass().getName() + ": JDBCPlugin class loaded");
  	}

  	/**
   	* Checks if a given server is listening o a given interface
   	* @param user Database user
   	* @param password Database password
   	* @param hostname name of the RDBMS server
   	* @param db_url Database connection url
   	* @param timeout Default login timeout
   	* @param retries Number of retrys before giving up a connection attempts
   	* @param db_driver JDBC driver to use
   	* @see DBTools#constructUrl
   	*/

  	private boolean isServer(String user, String password, String hostname, String db_url, int timeout, int retries, String db_driver)  
	{
    		Category log = ThreadCategory.getInstance(getClass());

    		boolean status   = false;
    		Connection con   = null;
    		Statement statement   = null;
    		ResultSet result = null;
    		boolean connected = false;

    		for (int attempts = 1; attempts <= retries && ! connected; ) 
		{
      			log.info(getClass().getName() +  ": Trying to detect JDBC server on '" + hostname + "', attempts #: " + attempts);
      			try 
			{

				log.debug(getClass().getName() + ": Loading JDBC driver: '" + db_driver + "'");
				Class.forName(db_driver).newInstance();
				log.debug(getClass().getName() + ": JDBC driver loaded: '" + db_driver + "'");

				String url = DBTools.constructUrl(db_url, hostname);
				log.debug(getClass().getName() + ": Constructed JDBC url: '" + url + "'");

        			DriverManager.setLoginTimeout(timeout);
        			con = DriverManager.getConnection(url, user, password);
				connected = true;
				log.debug(getClass().getName() + ": Got database connection: '" + con + "' (" + url + ", " + user + ", " + password + ")");

				DatabaseMetaData metadata = con.getMetaData();
				log.debug(getClass().getName() + ": Got database metadata");

        			result = metadata.getCatalogs();
        			while (result.next()) 
				{
           				result.getString(1);
	   				log.debug(getClass().getName() + ": Metadata catalog: '" + result.getString(1) + "'");
        			}

        			// The JDBC server was detected using JDBC, update the status
        			status = true;
        			log.info(getClass().getName() + ": JDBC server detected on: '" + hostname + "', attempts #:" + attempts);
      			} 
			catch (NullPointerException nullExp) {
	      			log.error(nullExp.toString());
      			} 
			catch (IllegalArgumentException illegalExp) {
	      			log.error(illegalExp.toString());
      			} 
			catch (InstantiationException insExp) {
	      			log.error(insExp.toString());
      			} 
			catch (IllegalAccessException illegalExp) {
	      			log.error(illegalExp.toString());
      			} 
			catch (ClassNotFoundException classExp) {
	      			log.error(classExp.toString());
      			} 
			catch (SQLException sqlException) {
	      			log.error(sqlException.toString());
      			} 
			finally {
        			attempts++;
        			if (result != null) {
          			try  { 
					result.close(); } 
				catch (SQLException ignore) {}
        			}
        			if (statement != null) {
          			try  { 
					statement.close(); } 
				catch (SQLException ignore) {}
        			}
        			if (con != null) {
          			try  { 
					con.close(); } 
				catch (SQLException ignore) {}
        		}
      		}
    	}
	return status;
 }

  	/**
   	* Returns the default protocol name
   	* @return String Protocol Name
   	*/
  	public String getProtocolName() 
	{
     		return PROTOCOL_NAME;
  	}

  	/**
   	* Default checking method, asuming all the parameters by default.
   	* This method is likely to skip some machines because the default password is empty.
   	* is recomended to use the parametric method instead (unless your DBA is dummy enugh to leave
   	* a JDBC server with no password!!!).
   	* @param address Address of the JDBC server to poll
   	* @return True if a JDBC server is running on this server, false otherwise
   	*/
  	public boolean isProtocolSupported(InetAddress address) 
	{
	  Category log = ThreadCategory.getInstance(getClass());
	  boolean status = false;

		String db_user = DBTools.DEFAULT_DATABASE_USER;
		String db_pass = DBTools.DEFAULT_DATABASE_PASSWORD;
		String db_hostname = address.getCanonicalHostName();
		String db_url = DBTools.DEFAULT_URL;
		int db_timeout = DEFAULT_TIMEOUT;
		int db_retries = DEFAULT_RETRY;
		String db_driver = DBTools.DEFAULT_JDBC_DRIVER;

	  	try 
		{
			status = isServer(db_user, db_pass, db_hostname, db_url, db_timeout, db_retries, db_driver);
	  	} 
		catch (Exception exp) {
		  log.error(exp.toString());
	  	} 
		return status;
  	}

  	/**
   	* Checking method, receives all the parameters as a Map.
	* Currently supported:
   	* <ul>
   	* <li> <b>port</b>     - Port where the JDBC server is listening (defaults to DEFAULT_PORT). Type: Integer
   	* <li> <b>user</b>     - Database user (defaults to DEFAULT_DATABASE_USER if not provided). Type String
   	* <li> <b>password</b> - Database password (defaults to DEFAULT_DATABASE_PASSWORD). Type String
   	* <li> <b>timeout</b> - Timeout
   	* <li> <b>retry</b> - How many times will try to check for the service
   	* </ul>
   	* @param address Address of the JDBC server to poll
   	* @param qualifiers Set of properties to be passed to the JDBC driver.
   	* @throws NullPointerException if the properties or the address are not defined
   	* @return True if a JDBC server is running on this server, false otherwise
   	*/
  	public boolean isProtocolSupported(InetAddress address, Map qualifiers) 
	{
    		Category log = ThreadCategory.getInstance(getClass());

    		boolean status = false;

    		if (address == null) {
	    		throw new NullPointerException(getClass().getName() + ": Internet address cannot be null");
    		}
    		if (qualifiers == null) {
	    		throw new NullPointerException(getClass().getName() + ": Map argument cannot be null");
    		}

		String db_user = ParameterMap.getKeyedString(qualifiers, "user", DBTools.DEFAULT_DATABASE_USER);
		String db_pass = ParameterMap.getKeyedString(qualifiers, "password", DBTools.DEFAULT_DATABASE_PASSWORD);
		String db_hostname = address.getCanonicalHostName();
		String db_url = ParameterMap.getKeyedString(qualifiers, "url", DBTools.DEFAULT_URL);
                int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
                int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
		String db_driver = ParameterMap.getKeyedString(qualifiers, "driver", DBTools.DEFAULT_JDBC_DRIVER);

		try 
		{
			status = isServer(db_user, db_pass, db_hostname, db_url, timeout, retries, db_driver);
		} 
		catch (Exception exp) {
			log.info(exp.toString());
		} 
		return status;
  	}

} // End of class

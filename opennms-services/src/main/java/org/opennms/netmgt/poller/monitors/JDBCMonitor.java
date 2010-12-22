
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:

//2004 May 19: Added response time information to poller. Bug 830
//2003 May 01: Added this JDBC poller, based on generic poller code.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

//Tab Size = 8


package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.DBTools;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * This class implements a basic JDBC monitoring framework; The idea is than
 * these tests doesn't take too long (or too much resources to run) and provide
 * the basic healt information about the polled server. See
 * <code>src/services/org/opennms/netmgt/poller</code> OpenNMS plugin
 * information at <a
 * href="http://www.opennms.org/users/docs/docs/html/devref.html">OpenNMS
 * developer site </a>
 *
 * @author Jose Vicente Nunez Zuleta (josevnz@users.sourceforge.net) - RHCE,
 *         SJCD, SJCP version 0.1 - 07/23/2002 * version 0.2 - 08/05/2002 --
 *         Added retry logic, input validations to poller.
 * @since 0.1
 * @version $Id: $
 */

// NOTE: This requires that the JDBC Drivers for the dbs be included with the remote poller
@Distributable
public class JDBCMonitor extends AbstractServiceMonitor {
	/**
	 * Number of miliseconds to wait before timing out a database login using
	 * JDBC Hint: 1 minute is 6000 miliseconds.
	 */
	public static final int DEFAULT_TIMEOUT = 3000;

	/**
	 * Default number of times to retry a test
	 */
	public static final int DEFAULT_RETRY = 0;

	/**
	 * Class constructor.
	 *
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public JDBCMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		log().info("JDBCmonitor class loaded");
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called after the framework loads the plugin.
	 */
	public void initialize(Map<String, Object> parameters) {
		super.initialize(parameters);
		log().debug("Calling init");
	}

	/**
	 * Release any used services by the plugin,normally during framework exit
	 * For now this method is just an 'adaptor', does nothing
	 *
	 * @throws java.lang.RuntimeException
	 *             Thrown if an error occurs during deallocation.
	 */
	public void release() {
		log().debug("Shuting down plugin");
	}

	/**
	 * This method is called when an interface that support the service is added
	 * to the scheduling service.
	 *
	 * @throws java.lang.RuntimeException
	 *             Thrown if an unrecoverable error occurs that prevents the
	 *             interface from being monitored.
	 * @throws org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException
	 *             Thrown if the passed interface is invalid for this monitor.
	 * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
	 */
	public void initialize(MonitoredService svc) {
		super.initialize(svc);
		log().debug("initialize");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <P>
	 * This method is the called whenever an interface is being removed from the
	 * scheduler. For now this method is just an 'adaptor', does nothing
	 */
	public void release(MonitoredService svc) {
		log().debug("Shuting down plugin");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Network interface to poll for a given service. Make sure you're using the
	 * latest (at least 5.5) <a
	 * href="http://www.sybase.com/detail_list/1,6902,2912,00.html">JConnect
	 * version </a> or the plugin will not be able to tell exactly if the
	 * service is up or not.
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
	 * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
	 * @see <a
	 *      href="http://manuals.sybase.com/onlinebooks/group-jc/jcg0550e/prjdbc/@Generic__BookTextView/9332;pt=1016#X">Error
	 *      codes for JConnect </a>
	 */
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		NetworkInterface<InetAddress> iface = svc.getNetInterface();

		// Assume that the service is down
		PollStatus status = PollStatus.unavailable();
		Driver driver = null;
		Connection con = null;
		Statement statement = null;
		ResultSet resultset = null;

		if (iface.getType() != NetworkInterface.TYPE_INET) {
			log().error("Unsupported interface type, only TYPE_INET currently supported");
			throw new NetworkInterfaceNotSupportedException(getClass().getName() + ": Unsupported interface type, only TYPE_INET currently supported");
		}

		if (parameters == null) {
			throw new NullPointerException("parameter cannot be null");
		}
		try {
			String driverClass = ParameterMap.getKeyedString(parameters, "driver", DBTools.DEFAULT_JDBC_DRIVER);
			driver = (Driver)Class.forName(driverClass).newInstance();
		} catch (Exception exp) {
			throw new RuntimeException("Unable to load driver class: "+exp.toString(), exp);
		}

		log().info("Loaded JDBC driver");

		// Get the JDBC url host part
		InetAddress ipv4Addr = (InetAddress) iface.getAddress();
		String url = null;
		url = DBTools.constructUrl(ParameterMap.getKeyedString(parameters, "url", DBTools.DEFAULT_URL), ipv4Addr.getCanonicalHostName());
		log().debug("JDBC url: " + url);
		
		TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

		String db_user = ParameterMap.getKeyedString(parameters, "user", DBTools.DEFAULT_DATABASE_USER);
		String db_pass = ParameterMap.getKeyedString(parameters, "password", DBTools.DEFAULT_DATABASE_PASSWORD);

		Properties props = new Properties();
		props.setProperty("user", db_user);
		props.setProperty("password", db_pass);
		props.setProperty("timeout", String.valueOf(tracker.getTimeoutInSeconds()));


		for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
			try {
				con = driver.connect(url, props);

				// We are connected, upgrade the status to unresponsive
				status = PollStatus.unresponsive();

				if (con != null) {
					log().debug("JDBC Connection Established");

					tracker.startAttempt();

					status = checkDatabaseStatus(con, parameters);

					if (status.isAvailable()) {
						double responseTime = tracker.elapsedTimeInMillis();
						status = PollStatus.available(responseTime);

						log().debug("JDBC service is AVAILABLE on: " + ipv4Addr.getCanonicalHostName());
						log().debug("poll: responseTime= " + responseTime + "ms");

						break;
					}
				} // end if con
			} catch (SQLException sqlEx) {
				
				status = logDown(Level.INFO, "JDBC service is not responding on: " + ipv4Addr.getCanonicalHostName() + ", " + sqlEx.getSQLState() + ", " + sqlEx.toString(), sqlEx);

			} finally {
				closeResultSet(resultset);
				closeStmt(statement);
				closeConnection(con);
			}
		}
		return status;
	}

	private void closeConnection(Connection con) {
		if (con == null) return;
		try {
			con.close();
		} catch (SQLException ignore) {
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

	private void closeResultSet(ResultSet resultset) {
		if (resultset != null) {
			try {
				resultset.close();
			} catch (SQLException ignore) {
			}
		}
	}

	/**
	 * <p>checkDatabaseStatus</p>
	 *
	 * @param con a {@link java.sql.Connection} object.
	 * @param parameters a {@link java.util.Map} object.
	 * @return a {@link org.opennms.netmgt.model.PollStatus} object.
	 */
	public PollStatus checkDatabaseStatus( Connection con, Map<String,Object> parameters )
	{
		PollStatus status = PollStatus.unavailable("Unable to retrieve database catalogs");
		ResultSet resultset = null;
		try
		{
			// We are connected, upgrade the status to unresponsive
			status = PollStatus.unresponsive();

			DatabaseMetaData metadata = con.getMetaData();
			resultset = metadata.getCatalogs();
			while (resultset.next())
			{
				resultset.getString(1);
			}

			// The query worked, assume than the server is ok
			if (resultset != null)
			{
				status = PollStatus.available();
			}
		}
		catch (SQLException sqlEx)
		{
			status = logDown(Level.DEBUG, "JDBC service failed to retrieve metadata: " + sqlEx.getSQLState() + ", " + sqlEx.toString(), sqlEx);
		}
		finally
		{
			closeResultSet(resultset);
		}
		return status;
	}

} // End of class

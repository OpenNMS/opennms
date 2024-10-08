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

import org.opennms.core.utils.DBTools;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class JDBCMonitor extends ParameterSubstitutingMonitor {
    
    public static final Logger LOG = LoggerFactory.getLogger(JDBCMonitor.class);
    
	/**
	 * Number of milliseconds to wait before timing out a database login
	 * JDBC Hint: 1 minute is 60000 milliseconds.
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
		LOG.info("JDBCmonitor class loaded");
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
	 * @see <a
	 *      href="http://manuals.sybase.com/onlinebooks/group-jc/jcg0550e/prjdbc/@Generic__BookTextView/9332;pt=1016#X">Error
	 *      codes for JConnect </a>
	 */
        @Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
		// Assume that the service is down
		PollStatus status = PollStatus.unavailable();
		Driver driver = null;
		Connection con = null;
		Statement statement = null;
		ResultSet resultset = null;

		if (parameters == null) {
			throw new NullPointerException("parameter cannot be null");
		}

		String driverClass = ParameterMap.getKeyedString(parameters, "driver", DBTools.DEFAULT_JDBC_DRIVER);
		try {
			driver = (Driver)Class.forName(driverClass).newInstance();
			LOG.debug("Loaded JDBC driver: {}", driverClass);
		} catch (Throwable exp) {
			throw new RuntimeException("Unable to load driver class: "+exp.toString(), exp);
		}

		LOG.info("Loaded JDBC driver");

		// Get the JDBC url host part
		InetAddress ipAddr = svc.getAddress();
		String url = null;
		url = DBTools.constructUrl(resolveKeyedString(parameters, "url", DBTools.DEFAULT_URL), ipAddr.getCanonicalHostName());
		LOG.debug("JDBC url: {}", url);
		
		TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

		String db_user = resolveKeyedString(parameters, "user", DBTools.DEFAULT_DATABASE_USER);
		String db_pass = resolveKeyedString(parameters, "password", DBTools.DEFAULT_DATABASE_PASSWORD);

		Properties props = new Properties();
		props.setProperty("user", db_user);
		props.setProperty("password", db_pass);
		props.setProperty("timeout", String.valueOf(tracker.getTimeoutInSeconds()));


		for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
			try {
				con = driver.connect(url, props);

				// We are connected, upgrade the status to unresponsive
				status = PollStatus.unresponsive();

				if (con == null) {
					LOG.error("Wrong kind of JDBC driver ({}) to connect to the JDBC URL: {}", driverClass, url);
					status = PollStatus.unavailable("Wrong kind of JDBC driver to connect to the JDBC URL");
					break;
				} else {
					LOG.debug("JDBC Connection Established");

					tracker.startAttempt();

					status = checkDatabaseStatus(con, parameters);

					if (status.isAvailable()) {
						double responseTime = tracker.elapsedTimeInMillis();
						status = PollStatus.available(responseTime);

						LOG.debug("JDBC service is AVAILABLE on: {}", ipAddr.getCanonicalHostName());
						LOG.debug("poll: responseTime= {}ms", responseTime);

						break;
					}
				} // end if con
			} catch (SQLException sqlEx) {
				
				String reason = "JDBC service is not responding on: " + ipAddr.getCanonicalHostName() + ", " + sqlEx.getSQLState() + ", " + sqlEx.toString();
                LOG.debug(reason, sqlEx);
                status = PollStatus.unavailable(reason);

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
	 * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
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
			String reason = "JDBC service failed to retrieve metadata: " + sqlEx.getSQLState() + ", " + sqlEx.toString();
            LOG.debug(reason, sqlEx);
            status = PollStatus.unavailable(reason);
		}
		finally
		{
			closeResultSet(resultset);
		}
		return status;
	}

} // End of class

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
// Modifications:
//
// 2005 SEP 19: Refactored to remove calls to DriverManager as it can (and does) cause thread deadlocks.
// 2005 Mar 10: Refactored to allow support for StoredProcedure poller and other potential subclasses.
// 2004 May 19: Added response time information to poller. Bug 830
// 2003 May 01: Added this JDBC poller, based on generic poller code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.lang.Integer;
import java.sql.Driver;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.DBTools;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.utils.ParameterMap;

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
 */
public class JDBCMonitor extends IPv4LatencyMonitor {
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
     */

    public JDBCMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Category log = ThreadCategory.getInstance(getClass());
        log.debug(getClass().getName() + ": JDBCmonitor class loaded");
    }

    /**
     * This method is called after the framework loads the plugin.
     * 
     * @param parameters
     *            Configuration parameters passed to the plugin
     * @throws RuntimeException
     *             If there is any error that prevents the plugin from running
     */
    public void initialize(PollerConfig pollerConfig, Map parameters) {
        super.initialize(pollerConfig, parameters);
        Category log = ThreadCategory.getInstance(getClass());
        log.debug(getClass().getName() + ": Calling init");
        return;
    }

    /**
     * Release any used services by the plugin,normally during framework exit
     * For now this method is just an 'adaptor', does nothing
     * 
     * @throws RuntimeException
     *             Thrown if an error occurs during deallocation.
     */
    public void release() {
        Category log = ThreadCategory.getInstance(getClass());
        log.debug(getClass().getName() + ": Shuting down plugin");
        return;
    }

    /**
     * This method is called when an interface that support the service is added
     * to the scheduling service.
     * 
     * @param iface
     *            The network interface to poll
     * @throws java.lang.RuntimeException
     *             Thrown if an unrecoverable error occurs that prevents the
     *             interface from being monitored.
     * @throws org.opennms.netmgt.poller.monitors.NetworkInterfaceNotSupportedException
     *             Thrown if the passed interface is invalid for this monitor.
     */
    public void initialize(NetworkInterface iface) {
        Category log = ThreadCategory.getInstance(getClass());
        if (!(iface.getAddress() instanceof InetAddress)) {
            throw new NetworkInterfaceNotSupportedException(getClass().getName() + ": Address type not supported");
        }
        log.debug(getClass().getName() + ": initialize");
        return;
    }

    /**
     * <P>
     * This method is the called whenever an interface is being removed from the
     * scheduler. For now this method is just an 'adaptor', does nothing
     * 
     * @param iface
     *            The network interface that was being monitored.
     * 
     * @throws java.lang.RuntimeException
     *             Thrown if an unrecoverable error occurs that prevents the
     *             interface from being monitored.
     */
    public void release(NetworkInterface iface) {
        Category log = ThreadCategory.getInstance(getClass());
        log.debug(getClass().getName() + ": Shuting down plugin");
        return;
    }

   /**
   * Network interface to poll for a given service. Make sure you're using the
   * latest (at least 5.5) <a
   * href="http://www.sybase.com/detail_list/1,6902,2912,00.html">JConnect
   * version </a> or the plugin will not be able to tell exactly if the
   * service is up or not.
   * 
   * @param iface
   *            The interface to poll
   * @param parameters
   *            Parameters to pass when polling the interface Currently
   *            recognized Map keys:
   *            <ul>
   *            <li>user - Database user
   *            <li>password - User password
   *            <li>port - server port
   *            <li>timeout - Number of miliseconds to wait before sending a
   *            timeout
   *            <li>driver - The JDBC driver to use
   *            <li>url - The vendor specific jdbc URL
   *            </ul>
   * @return int An status code that shows the status of the service
   * @throws java.lang.RuntimeException
   *             Thrown if an unrecoverable error occurs that prevents the
   *             interface from being monitored.
   * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#SURPRESS_EVENT_MASK
   * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#SERVICE_AVAILABLE
   * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#SERVICE_UNAVAILABLE
   * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#SERVICE_UNRESPONSIVE
   * @see <a
   *      href="http://manuals.sybase.com/onlinebooks/group-jc/jcg0550e/prjdbc/@Generic__BookTextView/9332;pt=1016#X">Error
   *      codes for JConnect </a>
   */
   public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg)
   {
      Category log = ThreadCategory.getInstance(getClass());

      // Asume that the service is down
      int status = SERVICE_UNAVAILABLE;
      Connection con = null;
      Driver driver = null;

      if (iface.getType() != NetworkInterface.TYPE_IPV4)
      {
         log.error(getClass().getName() + ": Unsupported interface type, only TYPE_IPV4 currently supported");
         throw new NetworkInterfaceNotSupportedException(getClass().getName() + ": Unsupported interface type, only TYPE_IPV4 currently supported");
      }
      if (parameters == null)
      {
         throw new NullPointerException();
      }
      try
      {
         String db_driver = ParameterMap.getKeyedString(parameters, "driver", DBTools.DEFAULT_JDBC_DRIVER);
         driver = (Driver)Class.forName(db_driver).newInstance();
      }
      catch (Exception exp)
      {
         exp.printStackTrace();
         throw new RuntimeException(exp.toString());
      }
      log.debug(getClass().getName() + ": Loaded JDBC driver");

      // Get the JDBC url host part
      InetAddress ipv4Addr = (InetAddress) iface.getAddress();
      String url = null;
      url = DBTools.constructUrl(ParameterMap.getKeyedString(parameters, "url", DBTools.DEFAULT_URL), ipv4Addr.getCanonicalHostName());
      log.debug(getClass().getName() + ": JDBC url: " + url);

      int retries = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
      String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
      String dsName = ParameterMap.getKeyedString(parameters, "ds-name", null);
      if (dsName == null)
      {
         dsName = DS_NAME;
      }

      if (rrdPath == null) {
         log.info("poll: RRD repository not specified in parameters, latency data will not be stored.");
      }

      int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
      String user = ParameterMap.getKeyedString(parameters, "user", DBTools.DEFAULT_DATABASE_USER);
      String password = ParameterMap.getKeyedString(parameters, "password", DBTools.DEFAULT_DATABASE_PASSWORD);
     
      Properties props = new Properties();
      props.setProperty("user", user);
      props.setProperty("password", password);
      Integer to = new Integer(timeout / 1000);
      props.setProperty("timeout", to.toString());

      for (int attempts = 0; attempts <= retries; attempts++)
      {
         try
         {
            log.debug(getClass().getName() + ": Attempting connection...");
            con = driver.connect(url, props);
            log.debug(getClass().getName() + ": Connection attempt complete.");

            if (con != null)
            {
               log.debug(getClass().getName() + ": Connection established.");
               long sentTime = System.currentTimeMillis();
               status = checkStatus(log, parameters, con);
               if (status == SERVICE_AVAILABLE)
               {
                  // The server is ok
                  long responseTime = System.currentTimeMillis() - sentTime;
                  if (log.isDebugEnabled())
                  {
                     log.debug(getClass().getName() + ": JDBC service is AVAILABLE on: " + ipv4Addr.getCanonicalHostName());
                     log.debug("poll: responseTime= " + responseTime + "ms");
                  }
                  // Update response time
                  if (responseTime >= 0 && rrdPath != null)
                  {
                     try
                     {
                        this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                     }
                     catch (RuntimeException rex)
                     {
                        log.debug("There was a problem writing the RRD:" + rex);
                     }
                  }
                  break;
               }
            } // end if con
            else
            {
               log.debug(getClass().getName() + ": Connection failed.");
            }
         }
         catch (SQLException sqlEx)
         {
            if (log.isDebugEnabled())
            {
               log.debug(getClass().getName() + ": JDBC service is not responding on: " + ipv4Addr.getCanonicalHostName() + ", " + sqlEx.getSQLState() + ", " + sqlEx.toString());
               sqlEx.printStackTrace();
            }
         }
         finally
         {
            if (con != null)
            {
               try
               {
                  con.close();
               }
               catch (SQLException ignore)
               {
               }
               con = null;
            }
         }
      }
      return status;
   }

   public int checkStatus( Category log, Map parameters, Connection con )
   {
      int status = SERVICE_UNAVAILABLE;
      ResultSet resultset = null;
      try
      {
         // We are connected, upgrade the status to unresponsive
         status = SERVICE_UNRESPONSIVE;

         DatabaseMetaData metadata = con.getMetaData();
         resultset = metadata.getCatalogs();
         while (resultset.next())
         {
            resultset.getString(1);
         }

         // The query worked, assume than the server is ok
         if (resultset != null)
         {
            status = SERVICE_AVAILABLE;
         }
      }
      catch (SQLException sqlEx)
      {
         if (log.isDebugEnabled())
         {
            log.debug(getClass().getName() + ": JDBC service failed to retrieve metadata: " + sqlEx.getSQLState() + ", " + sqlEx.toString());
            sqlEx.printStackTrace();
         }
      }
      finally
      {
         if (resultset != null)
         {
            try
            {
               resultset.close();
            }
            catch (SQLException ignore)
            {
            }
            resultset = null;
         }
      }
      return status;
   }
} // End of class


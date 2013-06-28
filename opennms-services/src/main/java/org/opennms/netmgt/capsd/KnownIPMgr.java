/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;

/**
 * This class represents a singular instance that is used to check address to
 * determine their pollablility. If the address has already been discovered or
 * is part of the exclude list then the manager can be used to check.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
final class KnownIPMgr {
    private static final Logger LOG = LoggerFactory.getLogger(KnownIPMgr.class);
    /**
     * The SQL statement used to extract the list of currently known IP
     * addresses from the IP Interface table.
     */
    private final static String IP_LOAD_SQL = "SELECT ipAddr, nodeid, ipLastCapsdPoll FROM ipInterface";

    /**
     * The SQL statment used to update the last capabilities check time.
     */
    private final static String IP_UPDATE_TIME_SQL = "UPDATE ipInterface SET ipLastCapsdPoll = ? WHERE ipAddr = ? AND nodeid = ?";

    /**
     * The set of all discovered addresses
     */
    private static Map<InetAddress, Object> m_known = new TreeMap<InetAddress, Object>(new InetAddressComparator());

    /**
     * This class is used to encapsulate the elements of importants from the IP
     * interface table in the OpenNMS database. The main elements are the
     * interface address, the node identifier, and the last time the interface
     * was checked.
     * 
     * @author <a href="mailto:weave@oculan.com">Weave </a>
     * @author <a href="http://www.opennms.org">OpenNMS </a>
     * 
     */
    final static class IPInterface {
        /**
         * The internet address of the interface.
         */
        private InetAddress m_interface;

        /**
         * The last date that the interface was checked for capabilities.
         */
        private Timestamp m_lastCheck;

        /**
         * The node identifier for this interface.
         */
        private int m_nodeid;

        /**
         * Constructs a new instance to represent the IP interface.
         * 
         * @param iface
         *            The IP Interface address.
         * @param nodeid
         *            The node identifier for the address.
         * 
         */
        IPInterface(InetAddress iface, int nodeid) {
            m_interface = iface;
            m_nodeid = nodeid;
            m_lastCheck = new Timestamp((new Date()).getTime());
        }

        /**
         * Constructs a new instance to represent the IP interface.
         * 
         * @param iface
         *            The IP Interface address.
         * @param nodeid
         *            The node identifier for the address.
         * @param date
         *            The last date this address was checked.
         * 
         */
        IPInterface(InetAddress iface, int nodeid, Timestamp date) {
            m_interface = iface;
            m_lastCheck = date;
            m_nodeid = nodeid;
        }

        /**
         * Constructs a new instance to represent the IP interface.
         * 
         * @param iface
         *            The IP Interface address.
         * @param nodeid
         *            The node identifier for the address.
         * @param date
         *            The last date this address was checked.
         * 
         * @throws java.text.ParseException
         *             Thrown if the date is malformed.
         */
        IPInterface(InetAddress iface, int nodeid, String date) throws ParseException {
            m_interface = iface;
            m_nodeid = nodeid;
            java.util.Date tmpDate = EventConstants.parseToDate(date);
            m_lastCheck = new Timestamp(tmpDate.getTime());
        }

        /**
         * Returns the internet address of the interface.
         */
        InetAddress getAddress() {
            return m_interface;
        }

        /**
         * Returns the node identifier for this interface.
         * 
         */
        int getNodeId() {
            return m_nodeid;
        }

        /**
         * Returns the data on which the interface was last checked.
         */
        Timestamp getLastCheckTime() {
            return m_lastCheck;
        }

        /**
         * Sets the time the interface was last checked.
         */
        void setLastCheckTime(Timestamp time) {
            m_lastCheck = time;
        }

        /**
         * Updates the the last check time in the database to match the time
         * encapsulated in this instance.
         * 
         * @param db
         *            The database to update the result in.
         * 
         * @throws java.sql.SQLException
         *             Thrown if an error occurs updating the database entry.
         */
        void update(Connection db) throws SQLException {
            final DBUtils d = new DBUtils(getClass());
            try {
                PreparedStatement stmt = db.prepareStatement(KnownIPMgr.IP_UPDATE_TIME_SQL);
                d.watch(stmt);
                stmt.setTimestamp(1, m_lastCheck);
                stmt.setString(2, InetAddressUtils.str(m_interface));
                stmt.setInt(3, m_nodeid);

                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }

    /**
     * Default construct for the instance. This constructor always throws an
     * exception to the caller.
     * 
     * @throws java.lang.UnsupportedOperationException
     *             Always thrown.
     * 
     */
    private KnownIPMgr() {
        throw new UnsupportedOperationException("Construction is not supported");
    }

    /**
     * Clears and synchronizes the internal known IP address cache with the
     * current information contained in the database. To synchronize the cache
     * the method opens a new connection to the database, loads the address, and
     * then closes it's connection.
     * 
     * @throws java.util.MissingResourceException
     *             Thrown if the method cannot find the database configuration
     *             file.
     * @throws java.sql.SQLException
     *             Thrown if the connection cannot be created or a database
     *             error occurs.
     * 
     */
    static synchronized void dataSourceSync() throws SQLException {

        // Get the database connection
        //
        Connection c = null;
        try {
            // open the connection
            //
            c = DataSourceFactory.getInstance().getConnection();

            // Run with it
            //
            // c.setReadOnly(true);

            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(IP_LOAD_SQL);

            if (rs != null) {
                m_known.clear();
                while (rs.next()) {
                    // extract the IP address.
                    //
                    String ipstr = rs.getString(1);
                    InetAddress addr = null;
                    addr = InetAddressUtils.addr(ipstr);
                    if (addr == null) {
                        LOG.warn("KnownIPMgr: failed to convert address {}", ipstr);
                        continue;
                    }

                    // get the node identifier
                    //
                    int nid = rs.getInt(2);

                    // get the last check time
                    //
                    Timestamp lastCheck = rs.getTimestamp(3);
                    m_known.put(addr, new IPInterface(addr, nid, lastCheck));
                }
                rs.close();
            }

            s.close();
        } finally {
            try {
                if (c != null)
                    c.close();
            } catch (SQLException sqlE) {
            }
        }
    }

    /**
     * Returns true if the node has been discovered and added to the discovered
     * IP manager.
     * 
     * @param addr
     *            The IP Address to query.
     * 
     * @return True if the address is known to the manager.
     * 
     */
    static synchronized boolean isKnown(InetAddress addr) {
        return m_known.containsKey(addr);
    }

    /**
     * Returns true if the node has been discovered and added to the discovered
     * IP manager. If the address cannot be converted to an
     * {@link java.net.InetAddressInetAddress} instance then an exception is
     * generated.
     * 
     * @param ipAddr
     *            The IP Address to query.
     * 
     * @return True if the address is known to the manager.
     * 
     * @throws java.io.UnknownHostException
     *             Thrown if the address name could not be converted.
     * 
     */
    static boolean isKnown(String ipAddr) throws UnknownHostException {
        return isKnown(InetAddressUtils.addr(ipAddr));
    }

    /**
     * Adds a new address to the list of discovered address.
     * 
     * @param addr
     *            The address to add to the discovered set.
     * 
     * @return True if the address was not already discovered.
     */
    static synchronized boolean addKnown(InetAddress addr) {
        return (m_known.put(addr, new Date()) == null);
    }

    /**
     * Adds a new address to the list of discovered address.
     * 
     * @param addr
     *            The address to add to the discovered set.
     * 
     * @return True if the address was not already discovered.
     * 
     * @throws java.net.UnknownHost
     *             Thrown if the address cannot be converted.
     */
    static boolean addKnown(String addr) throws UnknownHostException {
        return addKnown(InetAddressUtils.addr(addr));
    }

    /**
     * Returns the current snapshot set of all the known internet addresses in
     * the set of known nodes.
     * 
     * @return The arrry of all currently known InetAddress objects.
     * 
     */
    static synchronized InetAddress[] knownSet() {
        InetAddress[] set = new InetAddress[m_known.size()];
        return m_known.keySet().toArray(set);
    }

} // end KnownIPMgr


/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * <p>JdbcCapsdDbSyncer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JdbcCapsdDbSyncer implements InitializingBean, CapsdDbSyncer {
    
    /**
     * <P>
     * LightWeightIfEntry is designed to hold specific information about an IP
     * interface in the database such as its IP address, its parent node id, and
     * its managed status and represents a lighter weight version of the
     * DbIpInterfaceEntry class.
     * </P>
     */
    protected static final class LightWeightIfEntry {
        /**
         * Represents NULL value for 'ifIndex' field in the ipInterface table
         */
        protected final static int NULL_IFINDEX = -1;

        protected final static int NULL_IFTYPE = -1;

        protected final static int LOOPBACK_IFTYPE = 24;

        private int m_nodeId;

        private int m_ifIndex;

        private int m_ifType;

        private String m_address;

        private char m_managementState;

        private char m_snmpPrimaryState;

        private boolean m_primaryStateChanged;

        /**
         * <P>
         * Constructs a new LightWeightIfEntry object.
         * </P>
         * 
         * @param nodeId
         *            Interface's parent node id
         * @param ifIndex
         *            Interface's index
         * @param address
         *            Interface's ip address
         * @param managementState
         *            Interface's management state
         * @param snmpPrimaryState
         *            Interface's primary snmp interface state
         * @param ifType
         *            Interface's type as determined via SNMP
         */
        public LightWeightIfEntry(int nodeId, int ifIndex, String address, char managementState, char snmpPrimaryState, int ifType) {
            m_nodeId = nodeId;
            m_ifIndex = ifIndex;
            m_address = address;
            m_managementState = managementState;
            m_snmpPrimaryState = snmpPrimaryState;
            m_ifType = ifType;
            m_primaryStateChanged = false;
        }

        /**
         * <P>
         * Returns the IP address of the interface.
         * </P>
         */
        public String getAddress() {
            return m_address;
        }

        /**
         * <P>
         * Returns the parent node id of the interface.
         * </P>
         */
        public int getNodeId() {
            return m_nodeId;
        }

        /**
         * <P>
         * Returns the ifIndex of the interface.
         * </P>
         */
        public int getIfIndex() {
            return m_ifIndex;
        }

        /**
         * <P>
         * Returns the ifType of the interface.
         * </P>
         */
        public int getIfType() {
            return m_ifType;
        }

        /**
         * 
         */
        public char getManagementState() {
            return m_managementState;
        }

        /**
         * 
         */
        public char getSnmpPrimaryState() {
            return m_snmpPrimaryState;
        }

        /**
         * 
         */
        public void setSnmpPrimaryState(char state) {
            if (state != m_snmpPrimaryState) {
                m_snmpPrimaryState = state;
                m_primaryStateChanged = true;
            }
        }

        /**
         * 
         */
        public boolean hasSnmpPrimaryStateChanged() {
            return m_primaryStateChanged;
        }
    }

    /**
     * The SQL statement used to retrieve all non-deleted/non-forced unamanaged
     * IP interfaces from the 'ipInterface' table.
     */
    private static final String SQL_DB_RETRIEVE_IP_INTERFACE = 
        "SELECT ip.nodeid, ip.ipaddr, ip.ismanaged " +
        "FROM ipinterface as ip " +
        "JOIN node as n ON ip.nodeid = n.nodeid " +
        "WHERE ip.ipaddr!='0.0.0.0' " +
        "AND ip.isManaged!='D' " +
        "AND ip.isManaged!='F' " +
        "AND n.foreignSource is null";
    
    /**
     * The SQL statement used to retrieve all non-deleted/non-forced unamanaged
     * IP interfaces from the 'ipInterface' table with the local OpenNMS server
     * restriction.
     */
    private static final String SQL_DB_RETRIEVE_IP_INTERFACE_IN_LOCAL_SERVER = 
        "SELECT ip.nodeid, ip.ipaddr, ip.ismanaged " + 
        "FROM ipinterface as ip " +
        "JOIN node as n ON n.nodeid = ip.nodeid " +
        "JOIN servermap as s ON ip.ipaddr = s.ipaddr " + 
        "WHERE ip.ipaddr!='0.0.0.0' " + 
        "AND ip.isManaged!='D' " + 
        "AND ip.isManaged!='F' " + 
        "AND s.servername = ? " +
        "AND n.foreignSource is null";
    
    /**
     * SQL statement to retrieve all non-deleted IP addresses from the
     * ipInterface table which support SNMP.
     */
    private static final String SQL_DB_RETRIEVE_SNMP_IP_INTERFACES = 
        "SELECT DISTINCT ipinterface.nodeid,ipinterface.ipaddr,ipinterface.ifindex,ipinterface.issnmpprimary,snmpinterface.snmpiftype,snmpinterface.snmpifindex " +
        "FROM ipinterface " +
        "JOIN node ON node.nodeid = ipinterface.nodeid " +
        "JOIN snmpinterface ON ipinterface.snmpinterfaceid = snmpinterface.id " +
        "JOIN ifservices ON ifservices.ipinterfaceid = ipinterface.id " +
        "JOIN service ON ifservices.serviceid = service.serviceid " +
        "WHERE ipinterface.ismanaged!='D' " +
        "AND ifservices.status != 'D' " +
        "AND service.servicename='SNMP' " +
        "AND node.foreignSource is null";
    
    /**
     * SQL statement used to update the 'isSnmpPrimary' field of the ipInterface
     * table.
     */
    private static final String SQL_DB_UPDATE_SNMP_PRIMARY_STATE = "UPDATE ipinterface SET issnmpprimary=? WHERE nodeid=? AND ipaddr=? AND ismanaged!='D'";

    /**
     * The SQL statement used to retrieve all non-deleted/non-forced unamanaged
     * services for a nodeid/ip from the 'ifservices' table.
     */
    private static final String SQL_DB_RETRIEVE_IF_SERVICES = "SELECT serviceid, status FROM ifservices WHERE nodeid=? AND ipaddr=? AND status!='D' AND status!='F'";

    /**
     * The SQL statement which updates the 'isManaged' field in the ipInterface
     * table for a specific node/ipAddr pair
     */
    private static final String SQL_DB_UPDATE_IP_INTERFACE = "UPDATE ipinterface SET ismanaged=? WHERE nodeid=? AND ipaddr=? AND isManaged!='D' AND isManaged!='F'";

    /**
     * The SQL statement which updates the 'status' field in the ifServices
     * table for a specific node/ipAddr pair
     */
    private static final String SQL_DB_UPDATE_ALL_SERVICES_FOR_NIP = "UPDATE ifservices SET status=? WHERE nodeid=? AND ipaddr=? AND status!='D' AND status!='F'";

    private static final String SQL_DB_UPDATE_SERVICE_FOR_NIP = "UPDATE ifservices SET status=? WHERE nodeid=? AND ipaddr=? AND serviceid=? AND status!='D' AND status!='F'";

//    /**
//     * The SQL statement used to determine if an IP address is already in the
//     * ipInterface table and there is known.
//     */
//    private static final String RETRIEVE_IPADDR_SQL = 
//        "SELECT ip.ipaddr " +
//        "FROM ipinterface as ip " +
//        "JOIN node as n ON ip.nodeid = n.nodeid " +
//        "WHERE ip.ipaddr=? " +
//        "AND ip.ismanaged!='D'" +
//        "AND n.foreignSource is null";
//
//    /**
//     * The SQL statement used to determine if an IP address is already in the
//     * ipInterface table and if so what its parent nodeid is.
//     */
//    private static final String RETRIEVE_IPADDR_NODEID_SQL = 
//        "SELECT ip.nodeid " +
//        "FROM ipinterface as ip " +
//        "JOIN node as n ON ip.nodeid = n.nodeid " +
//        "WHERE ip.ipaddr=? " +
//        "AND ip.ismanaged!='D' " +
//        "AND n.foreignSource is null";

    /**
     * The SQL statement used to load the currenly defined service table.
     */
    private static final String SVCTBL_LOAD_SQL = "SELECT serviceID, serviceName FROM service";

    /**
     * The SQL statement used to add a new entry into the service table
     */
    private static final String SVCTBL_ADD_SQL = "INSERT INTO service (serviceID, serviceName) VALUES (?,?)";

    /**
     * The SQL statement used to mark all ifservices table entries which refer
     * to the specified serviceId as deleted.
     */
    private static final String DELETE_IFSERVICES_SQL = 
            "update ifservices " + 
            "   set status = 'D' " + 
            " where serviceid = ?" +
            "   and id in (" + 
            "   select svc.id" + 
            "     from ifservices as svc" + 
            "     join ipinterface as ip" + 
            "       on (ip.id = svc.ipinterfaceid)" + 
            "     join node as n" + 
            "       on (n.nodeid = ip.nodeid)" + 
            "    where n.foreignsource is null)"; 
    

    /**
     * The SQL statement used to get the next value for a service identifier.
     * This is a sequence defined in the database.
     */
    private static final String DEFAULT_NEXT_SVC_ID_SQL = "SELECT nextval('serviceNxtId')";


    /**
     * The SQL statement used to determine if an IP address is already in the
     * ipInterface table and if so what its parent nodeid is.
     */
    public static final String RETRIEVE_IPADDR_NODEID_SQL = 
        "SELECT ip.nodeid " +
        "FROM ipinterface as ip " +
        "JOIN node as n ON ip.nodeid = n.nodeid " +
        "WHERE ip.ipaddr=? " +
        "AND ip.ismanaged!='D' " +
        "AND n.foreignSource is null";
    
    /**
     * The SQL statement used to determine if an IP address is already in the
     * ipInterface table and there is known.
     */
    public static final String RETRIEVE_IPADDR_SQL = 
        "SELECT ip.ipaddr " +
        "FROM ipinterface as ip " +
        "JOIN node as n ON ip.nodeid = n.nodeid " +
        "WHERE ip.ipaddr=? " +
        "AND ip.ismanaged!='D'" +
        "AND n.foreignSource is null";


    
    private CapsdConfig m_capsdConfig;
    
    /**
     * The map of service identifiers, mapped by the service id to the service
     * name.
     */
    private Map<Integer, String> m_serviceIdToName = new HashMap<Integer,String>();
    
    /**
     * The map of service identifiers, mapped by the service name to the service
     * id.
     */
    private Map<String, Integer> m_serviceNameToId = new HashMap<String, Integer>();

    private OpennmsServerConfigFactory m_opennmsServerConfig;

    private CollectdConfigFactory m_collectdConfig;

    private PollerConfig m_pollerConfig;

    private String m_nextSvcIdSql = DEFAULT_NEXT_SVC_ID_SQL;
    
    private JdbcTemplate m_jdbcTemplate;

    /**
     * <p>Constructor for JdbcCapsdDbSyncer.</p>
     */
    public JdbcCapsdDbSyncer() {
        
    }
    
    
    /**
     * {@inheritDoc}
     *
     * Returns the service ID from the service table that was loaded
     * during class initialization for the specified name.
     */
    @Override
    public Integer getServiceId(String name) {
        Assert.notNull(name, "name argument must not be null");
        return m_serviceNameToId.get(name);
    }

    /**
     * {@inheritDoc}
     *
     * Returns the service name from the service table that was loaded
     * during class initialization for the specified ID.
     */
    @Override
    public String getServiceName(Integer id) {
        Assert.notNull(id, "id argument must not be null");
        return m_serviceIdToName.get(id);
    }


    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#syncServices()
     */
    /**
     * <p>syncServices</p>
     */
    @Override
    public void syncServices() {
        m_jdbcTemplate.execute(new ConnectionCallback<Object>() {
            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                syncServices(con);
                return null;
            }
            
        });
    }
    
    
    /**
     * <p>syncServices</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void syncServices(Connection conn) throws SQLException {
        
        List<String> serviceNames = syncServicesTable(conn);
    
        PreparedStatement delFromIfServicesStmt = null;
        final DBUtils d = new DBUtils(getClass());
        try { 
            
            List<String> protocols = getCapsdConfig().getConfiguredProtocols();
            
            /*
             * now iterate over the services from the 'service' table
             * and determine if any no longer exist in the list of
             * configured protocols
             */
           for(String service : serviceNames) {
                if (!protocols.contains(service)) {
                    if (log().isDebugEnabled()) {
                        log().debug("syncServices: service " + service + " exists in the database but not in the Capsd config file.");
                    }
    
                    Integer id = m_serviceNameToId.get(service);
    
                    // Delete 'ifServices' table entries which refer to the
                    // service
                    if (log().isDebugEnabled()) {
                        log().debug("syncServices: deleting all references to service id " + id + " from the IfServices table.");
                    }
                    delFromIfServicesStmt = conn.prepareStatement(DELETE_IFSERVICES_SQL);
                    d.watch(delFromIfServicesStmt);
                    delFromIfServicesStmt.setInt(1, id.intValue());
                    delFromIfServicesStmt.executeUpdate();
                    log().info("syncServices: deleted service id " + id + " for service '" + service + "' from the IfServices table.");
                }
            }
        } finally {
            d.cleanUp();
        }
        
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#syncServicesTable()
     */
    /**
     * <p>syncServicesTable</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> syncServicesTable() {
        return m_jdbcTemplate.execute(new ConnectionCallback<List<String>>() {

            public List<String> doInConnection(Connection con) throws SQLException, DataAccessException {
                return syncServicesTable(con);
            }
            
        });
    }
    
    /**
     * <p>syncServicesTable</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    public List<String> syncServicesTable(Connection conn) throws SQLException {
        log().debug("syncServicesTable: synchronizing services list with the database");
        
        List<String> serviceNames;
        final DBUtils d = new DBUtils(getClass());

        try {
            PreparedStatement insStmt = conn.prepareStatement(SVCTBL_ADD_SQL);
            d.watch(insStmt);
            PreparedStatement nxtStmt = conn.prepareStatement(getNextSvcIdSql());
            d.watch(nxtStmt);
            PreparedStatement loadStmt = conn.prepareStatement(SVCTBL_LOAD_SQL);
            d.watch(loadStmt);

            // go ahead and load the table first if it can be loaded
            serviceNames = new ArrayList<String>();
            ResultSet rs = loadStmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                Integer id = new Integer(rs.getInt(1));
                String name = rs.getString(2);
    
                m_serviceIdToName.put(id, name);
                m_serviceNameToId.put(name, id);
                serviceNames.add(name);
            }
    
            /*
             * now iterate over the configured protocols
             * and make sure that each is represented in the database.
             */
            for (String protocol : getCapsdConfig().getConfiguredProtocols()) {
                log().debug("syncServicesTable: checking protocol '" + protocol + "'.");
                if (!serviceNames.contains(protocol)) {
                    log().debug("syncServicesTable: protocol '" + protocol + "' is not in the database... adding.");
                    
                    // get the next identifier
                    rs = nxtStmt.executeQuery();
                    d.watch(rs);
                    rs.next();
                    int id = rs.getInt(1);
                    rs.close();
                    
                    log().debug("syncServicesTable: using id " + id + " for protocol '" + protocol + "'.");
    
                    insStmt.setInt(1, id);
                    insStmt.setString(2, protocol);
                    insStmt.executeUpdate();
    
                    m_serviceIdToName.put(id, protocol);
                    m_serviceNameToId.put(protocol, id);
    
                    serviceNames.add(protocol);
                    
                    log().info("syncServicesTable: added service entry to the database for protocol '" + protocol + "' with id of  " + id);
                }
            }
        } finally {
            d.cleanUp();
        }
        return serviceNames;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#syncManagementState()
     */
    /**
     * <p>syncManagementState</p>
     */
    @Override
    public void syncManagementState() {
        m_jdbcTemplate.execute(new ConnectionCallback<Object>() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                syncManagementState(con);
                return null;
            }
            
        });
    }

    /**
     * <p>syncManagementState</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void syncManagementState(Connection conn) throws SQLException {
        boolean verifyServer = getOpennmsServerConfig().verifyServer();
        String localServer = getOpennmsServerConfig().getServerName();
    
        if (log().isDebugEnabled()) {
            log().debug("syncManagementState: local server: " + localServer + " verify server: " + verifyServer);
        }
    
        if (conn == null) {
            log().error("CapsdConfigFactory.syncManagementState: Sync failed...must have valid database connection.");
            return;
        }
    
        // Get default management state.
        //
        String managementPolicy = getCapsdConfig().getConfiguration().getManagementPolicy();
        boolean managedByDefault = (managementPolicy == null || managementPolicy.equalsIgnoreCase("managed"));
        if (log().isDebugEnabled()) {
            log().debug("syncManagementState: managed_by_default: " + managedByDefault);
        }
    
        //
        // Retrieve list of interfaces and their managed status from the
        // database
        // NOTE: Interfaces with an 'isManaged' field equal to 'D' (Deleted) or
        // 'F' (Forced Unmanaged) are
        // not eligible to be managed and will not be included in the interfaces
        // retrieved from the database. Likewise, interfaces with IP address of
        // '0.0.0.0' will also be excluded by the SQL query.
        //
    
        // prepare the SQL statement to query the database
        PreparedStatement ipRetStmt = null;
        final DBUtils d = new DBUtils(getClass());
        List<LightWeightIfEntry> ifList = new ArrayList<LightWeightIfEntry>();

        try {
            if (verifyServer) {
                ipRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_IP_INTERFACE_IN_LOCAL_SERVER);
                d.watch(ipRetStmt);
                ipRetStmt.setString(1, localServer);
            } else {
                ipRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_IP_INTERFACE);
                d.watch(ipRetStmt);
            }
        
            ResultSet result = null;
            
            // run the statement
            result = ipRetStmt.executeQuery();
            d.watch(result);
    
            // Build array list of CapsdInterface objects representing each
            // of the interfaces retrieved from the database
            while (result.next()) {
                // Node Id
                int nodeId = result.getInt(1);
    
                // IP address
                String address = result.getString(2);
                if (address == null) {
                    log().warn("invalid ipInterface table entry, no IP address, skipping...");
                    continue;
                }
    
                // Management State
                char managedState = DbIpInterfaceEntry.STATE_UNKNOWN;
                String str = result.getString(3);
                if (str != null) {
                    managedState = str.charAt(0);
                }
    
                ifList.add(new LightWeightIfEntry(nodeId, LightWeightIfEntry.NULL_IFINDEX, address, managedState, DbIpInterfaceEntry.SNMP_UNKNOWN, LightWeightIfEntry.NULL_IFTYPE));
            }
        } finally {
            d.cleanUp();
        }

        try {
            // For efficiency, prepare the SQL statements in advance
            PreparedStatement ifUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_IP_INTERFACE);
            d.watch(ifUpdateStmt);
            PreparedStatement allSvcUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_ALL_SERVICES_FOR_NIP);
            d.watch(allSvcUpdateStmt);
            PreparedStatement svcRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_IF_SERVICES);
            d.watch(svcRetStmt);
            PreparedStatement svcUpdateStmt = conn.prepareStatement(SQL_DB_UPDATE_SERVICE_FOR_NIP);
            d.watch(svcUpdateStmt);
    
            /*
             * Loop through interface list and determine if there has been a
             * change in the managed status of the interface based on the
             * newly loaded package configuration data.
             */
            for (LightWeightIfEntry ifEntry : ifList) {
                String ipaddress = ifEntry.getAddress();
    
                // Convert to InetAddress object
                InetAddress ifAddress = null;
                ifAddress = InetAddressUtils.addr(ipaddress);
            	if (ifAddress == null) {
                    log().warn("Failed converting ip address " + ipaddress + " to InetAddress.");
                    continue;
                }
    
                // Check interface address against Capsd config information to
                // determine
                // if interface management state should be managed or unmanaged.
                boolean address_is_unmanaged = getCapsdConfig().isAddressUnmanaged(ifAddress);
                if (log().isDebugEnabled()) {
                    log().debug("syncManagementState: " + ipaddress + " unmanaged based on capsd config?: " + address_is_unmanaged);
                }
    
                if (address_is_unmanaged) {
                    // Interface not managed, check current
                    // management state for this interface.
                    if (ifEntry.getManagementState() != DbIpInterfaceEntry.STATE_UNMANAGED) {
                        // Update management state to unmanaged for the
                        // interface as well as for its services.
    
                        // Update the 'ipInterface' table
                        ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_UNMANAGED }));
                        ifUpdateStmt.setInt(2, ifEntry.getNodeId());
                        ifUpdateStmt.setString(3, ipaddress);
                        ifUpdateStmt.executeUpdate();
    
                        // Update the 'ifServices' table
                        allSvcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_UNMANAGED }));
                        allSvcUpdateStmt.setInt(2, ifEntry.getNodeId());
                        allSvcUpdateStmt.setString(3, ipaddress);
                        allSvcUpdateStmt.executeUpdate();
    
                        if (log().isDebugEnabled()) {
                            log().debug("syncManagementState: update completed for node/interface: " + ifEntry.getNodeId() + "/" + ipaddress + " to unmanaged");
                        }
                    }
                } else {
                    /*
                     * Interface should be managed - check the status against
                     * poller config to see if interface will be polled
                     * 
                     * NOTE: Try to avoid re-evaluating the ip against filters
                     * for each service, try to get the first package here and
                     * for that for service evaluation
                     */
                    final PollerConfig pollerConfig = getPollerConfig();
                    pollerConfig.getReadLock().lock();
                    try {
                        org.opennms.netmgt.config.poller.Package ipPkg = pollerConfig.getFirstPackageMatch(ipaddress);
                        boolean ipToBePolled = false;
                        if (ipPkg != null) {
                            ipToBePolled = true;
                        }
        
                        if (log().isDebugEnabled()) {
                            log().debug("syncManagementState: " + ipaddress + " to be polled based on poller config?: " + ipToBePolled);
                        }
        
                        if ((ifEntry.getManagementState() == DbIpInterfaceEntry.STATE_MANAGED && ipToBePolled) || (ifEntry.getManagementState() == DbIpInterfaceEntry.STATE_NOT_POLLED && !ipToBePolled)) {
                            // current status is right
                            if (log().isDebugEnabled()) {
                                log().debug("syncManagementState: " + ipaddress + " - no change in status");
                            }
                        } else {
                            if (ipToBePolled) {
                                ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_MANAGED }));
                            } else {
                                ifUpdateStmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_NOT_POLLED }));
                            }
        
                            ifUpdateStmt.setInt(2, ifEntry.getNodeId());
                            ifUpdateStmt.setString(3, ipaddress);
                            ifUpdateStmt.executeUpdate();
        
                            if (log().isDebugEnabled()) {
                                log().debug("syncManagementState: update completed for node/interface: " + ifEntry.getNodeId() + "/" + ipaddress);
                            }
                        }
        
                        // get services for this nodeid/ip and update
                        svcRetStmt.setInt(1, ifEntry.getNodeId());
                        svcRetStmt.setString(2, ipaddress);
        
                        ResultSet svcRS = svcRetStmt.executeQuery();
                        d.watch(svcRS);
                        while (svcRS.next()) {
                            int svcId = svcRS.getInt(1);
        
                            char svcStatus = DbIfServiceEntry.STATUS_UNKNOWN;
                            String str = svcRS.getString(2);
                            if (str != null) {
                                svcStatus = str.charAt(0);
                            }
        
                            String svcName = getServiceName(svcId);
                            /*
                             * try the first package that had the ip first, if
                             * service is not enabled, try all packages
                             */
                            char oldStatus = svcStatus;
                            char newStatus = 'U';
                            boolean svcToBePolledLocally = isServicePolledLocally(ipaddress, svcName, ipPkg);
                            boolean svcToBePolledRemotely = isServicePolled(ipaddress, svcName, ipPkg);
                            
                            if (log().isDebugEnabled()) {
                                log().debug("syncManagementState: " + ipaddress + "/" + svcName + " to be polled based on poller config?: " + svcToBePolledLocally);
                            }
        
                            if ((svcStatus == DbIfServiceEntry.STATUS_ACTIVE && svcToBePolledLocally) || (svcStatus == DbIfServiceEntry.STATUS_NOT_POLLED && !ipToBePolled)) {
                                // current status is right
                                if (log().isDebugEnabled()) {
                                    log().debug("syncManagementState: " + ifEntry.getNodeId() + "/" + ipaddress + "/" + svcName + " status = " + svcStatus + " - no change in status");
                                }
                            } else {
                                // Update the 'ifServices' table
                                if (svcStatus == DbIfServiceEntry.STATUS_SUSPEND && svcToBePolledLocally) {
                                    svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_FORCED }));
                                    newStatus = 'F';
                                } else if (svcToBePolledLocally) {
                                    svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_ACTIVE }));
                                    newStatus = 'A';
                                } else if (svcToBePolledRemotely) {
                                    svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_REMOTE }));
                                    newStatus = 'X';
                                } else {
                                    svcUpdateStmt.setString(1, new String(new char[] { DbIfServiceEntry.STATUS_NOT_POLLED }));
                                    newStatus = 'N';
                                }
                                svcUpdateStmt.setInt(2, ifEntry.getNodeId());
                                svcUpdateStmt.setString(3, ipaddress);
                                svcUpdateStmt.setInt(4, svcId);
                                svcUpdateStmt.executeUpdate();
        
                                if (log().isDebugEnabled()) {
                                    log().debug("syncManagementState: update completed for node/interface/svc: " + ifEntry.getNodeId() + "/" + ipaddress + "/" + svcName + " status changed from " + oldStatus + " to " + newStatus);
                                }
                            }
        
                        } // end ifservices result
                    } finally {
                        pollerConfig.getReadLock().unlock();
                    }
                } // interface managed
            } // end while
        } finally {
            d.cleanUp();
        }
    }
    
    private boolean isServicePolled(final String ifAddr, final String svcName, final org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
            if (ipPkg != null) {
                final PollerConfig pollerConfig = getPollerConfig();
                pollerConfig.getReadLock().lock();
                try {
                    svcToBePolled = pollerConfig.isPolled(svcName, ipPkg);
                    if (!svcToBePolled) svcToBePolled = pollerConfig.isPolled(ifAddr, svcName);
                } finally {
                    pollerConfig.getReadLock().unlock();
                }
            }
        return svcToBePolled;
    }

    private boolean isServicePolledLocally(final String ifAddr, final String svcName, final org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
        if (ipPkg != null && !ipPkg.getRemote()) {
            final PollerConfig pollerConfig = getPollerConfig();
            pollerConfig.getReadLock().lock();
            try {
                svcToBePolled = pollerConfig.isPolled(svcName, ipPkg);
                if (!svcToBePolled) svcToBePolled = pollerConfig.isPolledLocally(ifAddr, svcName);
            } finally {
                pollerConfig.getReadLock().unlock();
            }
        }
        return svcToBePolled;
    }



    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#syncSnmpPrimaryState()
     */
    /**
     * <p>syncSnmpPrimaryState</p>
     */
    @Override
    public void syncSnmpPrimaryState() {
        m_jdbcTemplate.execute(new ConnectionCallback<Object>() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                syncSnmpPrimaryState(con);
                return null;
            }
            
        });
    }

    /**
     * <p>syncSnmpPrimaryState</p>
     *
     * @param conn a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public synchronized void syncSnmpPrimaryState(Connection conn) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("Sync failed...must have valid database connection.");
        }
    
        /*
         * Retrieve all non-deleted SNMP-supporting IP interfaces from the
         * ipInterface table and build a map of nodes to interface entry list
         */
        log().debug("syncSnmpPrimaryState: building map of nodes to interfaces...");
    
        Map<Integer, List<LightWeightIfEntry>> nodes = new HashMap<Integer, List<LightWeightIfEntry>>();

        final DBUtils d = new DBUtils(getClass());
        try {
            // prepare the SQL statement to query the database
            PreparedStatement ipRetStmt = conn.prepareStatement(SQL_DB_RETRIEVE_SNMP_IP_INTERFACES);
            d.watch(ipRetStmt);
            ResultSet result = ipRetStmt.executeQuery();
            d.watch(result);
    
            // Iterate over result set and build map of interface
            // entries keyed by node id.
            List<LightWeightIfEntry> ifList = new ArrayList<LightWeightIfEntry>();
            while (result.next()) {
                // Node Id
                int nodeId = result.getInt(1);
    
                // IP address
                String address = result.getString(2);
                if (address == null) {
                    log().warn("invalid ipInterface table entry, no IP address, skipping...");
                    continue;
                }
    
                // ifIndex
                int ifIndex = result.getInt(6);
                if (result.wasNull()) {
                    if (log().isDebugEnabled()) {
                        log().debug("ipInterface table entry for address " + address + " does not have a valid ifIndex ");
                    }
                    ifIndex = LightWeightIfEntry.NULL_IFINDEX;
                } else if (ifIndex < 1) {
                    if (ifIndex == CapsdConfig.LAME_SNMP_HOST_IFINDEX) {
                        if (log().isDebugEnabled()) {
                            log().debug("Using ifIndex = " + CapsdConfig.LAME_SNMP_HOST_IFINDEX + " for address " + address);
                        }
                    } else {
                        if (log().isDebugEnabled()) {
                            log().debug("ipInterface table entry for address " + address + " does not have a valid ifIndex ");
                        }
                        ifIndex = LightWeightIfEntry.NULL_IFINDEX;
                    } 
                }
    
                // Primary SNMP State
                char primarySnmpState = DbIpInterfaceEntry.SNMP_UNKNOWN;
                String str = result.getString(4);
                if (str != null) {
                    primarySnmpState = str.charAt(0);
                }
    
                // ifType
                int ifType = result.getInt(5);
                if (result.wasNull()) {
                    if (log().isDebugEnabled()) {
                        log().debug("snmpInterface table entry for address " + address + " does not have a valid ifType");
                    }
                    ifType = LightWeightIfEntry.NULL_IFTYPE;
                }
    
                // New node or existing node?
                ifList = nodes.get(new Integer(nodeId));
                if (ifList == null) {
                    // Create new interface entry list
                    ifList = new ArrayList<LightWeightIfEntry>();
                    ifList.add(new LightWeightIfEntry(nodeId, ifIndex, address, DbIpInterfaceEntry.STATE_UNKNOWN, primarySnmpState, ifType));
    
                    // Add interface entry list to the map
                    nodes.put(nodeId, ifList);
                } else {
                    // Just add the current interface to the
                    // node's interface list
                    ifList.add(new LightWeightIfEntry(nodeId, ifIndex, address, DbIpInterfaceEntry.STATE_UNKNOWN, primarySnmpState, ifType));
                }
            }
        } finally {
            d.cleanUp();
        }

        /*
         * Iterate over the nodes in the map and determine what the primary
         * SNMP interface for each node should be. Keep track of those
         * interfaces whose primary SNMP interface state has changed so that
         * the database can be updated accordingly.
         */
        if (log().isDebugEnabled()) {
            log().debug("syncSnmpPrimaryState: iterating over nodes in map and checking primary SNMP interface, node count: " + nodes.size());
        }
        Iterator<Integer> niter = nodes.keySet().iterator();
        while (niter.hasNext()) {
            // Get the nodeid (key)
            Integer nId = niter.next();
            if (log().isDebugEnabled()) {
                log().debug("building SNMP address list for node " + nId);
            }
    
            // Lookup the interface list (value)
            List<LightWeightIfEntry> ifEntries = nodes.get(nId);
    
            /*
             * From the interface entries build a list of InetAddress objects
             * eligible to be the primary SNMP interface for the node, and a
             * list of loopback InetAddress objects eligible to be the primary
             * SNMP interface for the node.
             */
            List<InetAddress> addressList = new ArrayList<InetAddress>();
            List<InetAddress> lbAddressList = new ArrayList<InetAddress>();
            for (LightWeightIfEntry lwIf : ifEntries) {
                /*
                 * Skip interfaces which do not have a valid (non-null) ifIndex
                 * as they are not eligible to be the primary SNMP interface
                 */
                if (lwIf.getIfIndex() == LightWeightIfEntry.NULL_IFINDEX) {
                    if (log().isDebugEnabled()) {
                        log().debug("skipping address " + lwIf.getAddress() + ": does not have a valid ifIndex.");
                    }
                    continue;
                }
    
                InetAddress addr = InetAddressUtils.addr(lwIf.getAddress());
                addressList.add(addr);
                if (lwIf.getIfType() == LightWeightIfEntry.LOOPBACK_IFTYPE) {
                    lbAddressList.add(addr);
                }
            }
    
            /*
             * Determine primary SNMP interface from the lists of possible addresses
             * in this order: loopback interfaces in collectd-configuration.xml,
             * other interfaces in collectd-configuration.xml, loopback interfaces in
             * the database, other interfaces in the database.
             */
            boolean strict = true;
            InetAddress primarySnmpIf = null;
            String psiType = null;
            if (lbAddressList != null) {
                primarySnmpIf = getCapsdConfig().determinePrimarySnmpInterface(lbAddressList, strict);
                psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " loopback addresses";
            }
            if (primarySnmpIf == null) {
                primarySnmpIf = getCapsdConfig().determinePrimarySnmpInterface(addressList, strict);
                psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " addresses";
            }
            strict = false;
            if ((primarySnmpIf == null) && (lbAddressList != null)){
                primarySnmpIf = getCapsdConfig().determinePrimarySnmpInterface(lbAddressList, strict);
                psiType = "DB loopback addresses";
            }
            if (primarySnmpIf == null) {
                primarySnmpIf = getCapsdConfig().determinePrimarySnmpInterface(addressList, strict);
                psiType = "DB addresses";
            }
    
            if (log().isDebugEnabled()) {
                if(primarySnmpIf == null) {
                    log().debug("syncSnmpPrimaryState: No primary SNMP interface found for node " + nId);
                } else {
                    log().debug("syncSnmpPrimaryState: primary SNMP interface for node " + nId + " is: " + primarySnmpIf + ", selected from " + psiType);
                }
            }
    
            /*
             * Iterate back over interface list and update primary SNMP
             * iinterface state for this node...if the primary SNMP interface
             * state has changed, update the database to reflect the new state.
             */
            for (LightWeightIfEntry lwIf : ifEntries) {
                if (lwIf.getIfIndex() == LightWeightIfEntry.NULL_IFINDEX) {
                    lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
                } else if (primarySnmpIf == null || !lwIf.getAddress().equals(InetAddressUtils.str(primarySnmpIf))) {
                    if (getCollectdConfig().isServiceCollectionEnabled(lwIf.getAddress(), "SNMP")) {
                        lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_SECONDARY);
                    } else {
                        lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
                    }
                } else {
                    lwIf.setSnmpPrimaryState(DbIpInterfaceEntry.SNMP_PRIMARY);
                }
    
                // Has SNMP primary state changed?
                if (lwIf.hasSnmpPrimaryStateChanged()) {
                    if (log().isDebugEnabled()) {
                        log().debug("syncSnmpPrimaryState: updating " + lwIf.getNodeId() + "/" + lwIf.getAddress() + ", marking with state: " + lwIf.getSnmpPrimaryState());
                    }

                    try {
                        // prepare the SQL statement to query the database
                        PreparedStatement updateStmt = conn.prepareStatement(SQL_DB_UPDATE_SNMP_PRIMARY_STATE);
                        d.watch(updateStmt);
                        updateStmt.setString(1, new String(new char[] { lwIf.getSnmpPrimaryState() }));
                        updateStmt.setInt(2, lwIf.getNodeId());
                        updateStmt.setString(3, lwIf.getAddress());
    
                        updateStmt.executeUpdate();
                    } finally {
                        d.cleanUp();
                    }
                }
            }
        }
    
        log().debug("syncSnmpPrimaryState: sync completed.");
    }


    /**
     * <p>getCapsdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public CapsdConfig getCapsdConfig() {
        return m_capsdConfig;
    }

    /**
     * <p>setCapsdConfig</p>
     *
     * @param capsdConfig a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public void setCapsdConfig(CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }

    /**
     * <p>getOpennmsServerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.OpennmsServerConfigFactory} object.
     */
    public OpennmsServerConfigFactory getOpennmsServerConfig() {
        return m_opennmsServerConfig;
    }
    
    /**
     * <p>setOpennmsServerConfig</p>
     *
     * @param serverConfigFactory a {@link org.opennms.netmgt.config.OpennmsServerConfigFactory} object.
     */
    public void setOpennmsServerConfig(OpennmsServerConfigFactory serverConfigFactory) {
        m_opennmsServerConfig = serverConfigFactory;
    }
    
    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }
    
    /**
     * <p>setPollerConfig</p>
     *
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>getCollectdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.CollectdConfigFactory} object.
     */
    public CollectdConfigFactory getCollectdConfig() {
        return m_collectdConfig;
    }

    /**
     * <p>setCollectdConfig</p>
     *
     * @param collectdConfigFactory a {@link org.opennms.netmgt.config.CollectdConfigFactory} object.
     */
    public void setCollectdConfig(CollectdConfigFactory collectdConfigFactory) {
        m_collectdConfig = collectdConfigFactory;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_jdbcTemplate != null, "property jdbcTemplate must be set to a non-null value");
        Assert.state(m_opennmsServerConfig != null, "property opennmsServerConfig must be set to a non-null value");
        Assert.state(m_pollerConfig != null, "property pollerConfig must be set to a non-null value");
        Assert.state(m_collectdConfig != null, "property collectdConfig must be set to a non-null value");
    }


    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#getInterfaceDbNodeId(java.sql.Connection, java.net.InetAddress, int)
     */
    /**
     * <p>getInterfaceDbNodeId</p>
     *
     * @param dbConn a {@link java.sql.Connection} object.
     * @param ifAddress a {@link java.net.InetAddress} object.
     * @param ifIndex a int.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getInterfaceDbNodeId(Connection dbConn, InetAddress ifAddress, int ifIndex) throws SQLException {
        if (log().isDebugEnabled()) {
            log().debug("getInterfaceDbNodeId: attempting to lookup interface " + InetAddressUtils.str(ifAddress) + "/ifindex: " + ifIndex + " in the database.");
        }
    
        // Set connection as read-only
        // dbConn.setReadOnly(true);
    
        StringBuffer qs = new StringBuffer(RETRIEVE_IPADDR_NODEID_SQL);
        if (ifIndex != -1) {
            qs.append(" AND ifindex=?");
        }
        
        int nodeid = -1;
    
        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement s = dbConn.prepareStatement(qs.toString());
            d.watch(s);
            s.setString(1, InetAddressUtils.str(ifAddress));
    
            if (ifIndex != -1) {
                s.setInt(2, ifIndex);
            }
    
            ResultSet rs = s.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                nodeid = rs.getInt(1);
            }
        } finally {
            d.cleanUp();
        }
    
        return nodeid;
    }


	/* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.CapsdDbSyncerI#isInterfaceInDB(java.sql.Connection, java.net.InetAddress)
     */
    /** {@inheritDoc} */
    @Override
    public boolean isInterfaceInDB(final InetAddress ifAddress) {
        return m_jdbcTemplate.execute(new ConnectionCallback<Boolean>() {

            public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
                    return isInterfaceInDB(con, ifAddress) ? Boolean.TRUE : Boolean.FALSE;
            }
            
        }).booleanValue();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isInterfaceInDB(Connection dbConn, InetAddress ifAddress) throws SQLException {
        boolean result = false;
    
        if (log().isDebugEnabled()) {
            log().debug("isInterfaceInDB: attempting to lookup interface " + InetAddressUtils.str(ifAddress) + " in the database.");
        }
    
        // Set connection as read-only
        //
        // dbConn.setReadOnly(true);
    
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        
        try {
            PreparedStatement s = dbConn.prepareStatement(RETRIEVE_IPADDR_SQL);
            d.watch(s);
            s.setString(1, InetAddressUtils.str(ifAddress));
    
            rs = s.executeQuery();
            d.watch(rs);
            result = rs.next();
        } finally {
            d.cleanUp();
        }
    
        return result;
    }

    /**
     * <p>setNextSvcIdSql</p>
     *
     * @param nextSvcIdSql a {@link java.lang.String} object.
     */
    public void setNextSvcIdSql(String nextSvcIdSql) {
        m_nextSvcIdSql = nextSvcIdSql;
    }

    /**
     * <p>getNextSvcIdSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNextSvcIdSql() {
       return m_nextSvcIdSql;
    }

    /**
     * <p>setJdbcTemplate</p>
     *
     * @param jdbcTemplate a {@link org.springframework.jdbc.core.JdbcTemplate} object.
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }
    

}

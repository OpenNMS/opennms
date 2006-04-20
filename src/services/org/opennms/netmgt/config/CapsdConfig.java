//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CapsdConfigManager.ProtocolInfo;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.capsd.SmbAuth;

public interface CapsdConfig {

    /**
     * This integer value is used to represent the primary snmp interface
     * ifindex in the ipinterface table for SNMP hosts that don't support
     * the MIB2 ipAddrTable
     */
    public static final int LAME_SNMP_HOST_IFINDEX = -100;

    /**
     * Saves the current in-memory configuration to disk and reloads
     */
    public abstract void save() throws MarshalException, IOException, ValidationException;

    /**
     * Return the Capsd configuration object.
     */
    public abstract CapsdConfiguration getConfiguration();

    /**
     * This method is responsible for sync'ing the content of the 'service'
     * table with the protocols listed in the caspd-configuration.xml file.
     * 
     * First a list of services currently contained in the 'service' table in
     * the database is built.
     * 
     * Next, the list of services defined in capsd-configuration.xml is iterated
     * over and if any services are defined but do not yet exist in the
     * 'service' table they are added to the table.
     * 
     * Finally, the list of services in the database is iterated over and if any
     * service exists in the database but is no longer listed in the
     * capsd-configuration.xml file then that the following occurs:
     * 
     * 1. All 'outage' table entries which refer to the service are deleted. 2.
     * All 'ifServices' table entries which refer to the service are deleted.
     * 
     * Note that the 'service' table entry will remain in the database since
     * events most likely exist which refer to the service.
     */
    public abstract void syncServices(Connection conn) throws SQLException;
    
    
    public abstract List syncServicesTable(Connection conn) throws SQLException;

    /**
     * Responsible for syncing up the 'isManaged' field of the ipInterface table
     * and the 'status' field of the ifServices table based on the capsd and
     * poller configurations. Note that the 'sync' only takes place for
     * interfaces and services that are not deleted or force unmanaged.
     * 
     * <pre>
     * Here is how the statuses are set:
     *  If an interface is 'unmanaged' based on the capsd configuration,
     *      ipManaged='U' and status='U'
     * 
     *  If an interface is 'managed' based on the capsd configuration,
     *    1. If the interface is not in any pacakge, ipManaged='N' and status ='N'
     *    2. If the interface in atleast one package but the service is not polled by
     *       by any of the packages, ipManaged='M' and status='N'
     *    3. If the interface in atleast one package and the service is polled by a
     *       package that this interface belongs to, ipManaged='M' and status'=A'
     * 
     * </pre>
     * 
     * @param conn
     *            Connection to the database.
     * 
     * @exception SQLException
     *                Thrown if an error occurs while syncing the database.
     */
    public abstract void syncManagementState(Connection conn) throws SQLException;

    /**
     * Responsible for syncing up the 'isPrimarySnmp' field of the ipInterface
     * table based on the capsd and collectd configurations. Note that the
     * 'sync' only takes place for interfaces that are not deleted. Also, it
     * will prefer a loopback interface over other interfaces.
     * 
     * @param conn
     *            Connection to the database.
     * 
     * @exception SQLException
     *                Thrown if an error occurs while syncing the database.
     */
    public abstract void syncSnmpPrimaryState(Connection conn) throws SQLException;

    /**
     * Returns the list of protocol plugins and the associated actions for the
     * named address. The currently loaded configuration is used to find, build,
     * and return the protocol information. The returns information has all the
     * necessary element to check the address for capabilities.
     * 
     * @param address
     *            The address to get protocol information for.
     * 
     * @return The array of protocol information instances for the address.
     * 
     */
    public abstract ProtocolInfo[] getProtocolSpecification(InetAddress address);

    /**
     * Returns the protocol identifier from the service table that was loaded
     * during class initialization. The identifier is used determines the
     * result. If a String is passed then the integer value is returned. If an
     * interger value is passed then the string protocol name is returned.
     * 
     * @param key
     *            The value used to lookup the result in in the preloaded map.
     * 
     * @return The result of the lookup, either a String or an Integer.
     * 
     */
    public abstract Object getServiceIdentifier(Object key);

    /**
     * Finds the SMB authentication object using the netbios name.
     * 
     * The target of the search.
     */
    public abstract SmbAuth getSmbAuth(String target);

    /**
     * Checks the configuration to determine if the target is managed or
     * unmanaged.
     * 
     * @param target
     *            The target to check against.
     */
    public abstract boolean isAddressUnmanaged(InetAddress target);

    /**
     * 
     */
    public abstract boolean isInterfaceInDB(Connection dbConn, InetAddress ifAddress) throws SQLException;

    /**
     * 
     */
    public abstract int getInterfaceDbNodeId(Connection dbConn, InetAddress ifAddress, int ifIndex) throws SQLException;

    /**
     * 
     */
    public abstract long getRescanFrequency();

    /**
     * 
     */
    public abstract long getInitialSleepTime();

    /**
     * 
     */
    public abstract int getMaxSuspectThreadPoolSize();

    /**
     * 
     */
    public abstract int getMaxRescanThreadPoolSize();

    /**
     * Defines Capsd's behavior when, during a protocol scan, it gets a
     * java.net.NoRouteToHostException exception. If abort rescan property is
     * set to "true" then Capsd will not perform any additional protocol scans.
     */
    public abstract boolean getAbortProtocolScansFlag();

    public abstract boolean getDeletePropagationEnabled();

    /**
     * Return the boolean xmlrpc as string to indicate if notification to
     * external xmlrpc server is needed.
     * 
     * @return boolean flag as a string value
     */
    public abstract String getXmlrpc();
    
    public abstract ProtocolPlugin getProtocolPlugin(String svcName);

    public abstract void addProtocolPlugin(ProtocolPlugin plugin);
    
    public abstract InetAddress determinePrimarySnmpInterface(List addressList, boolean strict);


}
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>CapsdDbSyncer interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CapsdDbSyncer {

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
    void syncServices();
    
    /**
     * Synchronize configured services list with the database.
     *
     * @return a {@link java.util.List} object.
     */
    List<String> syncServicesTable();
    
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
     * @exception SQLException
     *                Thrown if an error occurs while syncing the database.
     */
    void syncManagementState();
    
    /**
     * Responsible for syncing up the 'isPrimarySnmp' field of the ipInterface
     * table based on the capsd and collectd configurations. Note that the
     * 'sync' only takes place for interfaces that are not deleted. Also, it
     * will prefer a loopback interface over other interfaces.
     *
     * @exception SQLException
     *                Thrown if an error occurs while syncing the database.
     */
    void syncSnmpPrimaryState();
    
    /**
     * <p>isInterfaceInDB</p>
     *
     * @param ifAddress a {@link java.net.InetAddress} object.
     * @return a boolean.
     */
    boolean isInterfaceInDB(InetAddress ifAddress);
    
    /**
     * <p>isInterfaceInDB</p>
     *
     * @param dbConn a {@link java.sql.Connection} object.
     * @param ifAddress a {@link java.net.InetAddress} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    boolean isInterfaceInDB(Connection dbConn,
            InetAddress ifAddress) throws SQLException;

    
    /**
     * Returns the service ID from the service table that was loaded
     * during class initialization for the specified name.
     *
     * @param name the name of the service to look up
     * @return The result of the lookup, or null if a matching service
     *          name wasn't found
     */
    Integer getServiceId(String name);

    /**
     * Returns the service name from the service table that was loaded
     * during class initialization for the specified ID.
     *
     * @return The result of the lookup, or null if a matching service
     *          ID wasn't found
     * @param id a {@link java.lang.Integer} object.
     */
    String getServiceName(Integer id);

    /**
     * @param addrString
     * @param string
     * @return
     */
    boolean isServiceCollectionEnabled(String addrString, String string);

    /**
     * <p>determinePrimarySnmpInterface</p>
     *
     * @param addressList a {@link java.util.List} object.
     * @param strict a boolean.
     * @return a {@link java.net.InetAddress} object.
     */
    InetAddress determinePrimarySnmpInterface(List<InetAddress> addressList, boolean strict);

}

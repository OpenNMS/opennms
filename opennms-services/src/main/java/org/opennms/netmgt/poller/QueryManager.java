/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * <p>QueryManager interface.</p>
 *
 * @author brozow
 */
public interface QueryManager {

    /**
     * <p>getNodeLabel</p>
     *
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     * @return a {@link java.lang.String} object.
     */
    String getNodeLabel(int nodeId) throws SQLException;

    /**
     * <p>openOutage</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName TODO
     * @param dbid a int.
     * @param time a {@link java.lang.String} object.
     * @param outageIdSQL a {@link java.lang.String} object.
     */
    void openOutage(String outageIdSQL, int nodeId, String ipAddr, String svcName, int dbid, String time);

    /**
     * <p>resolveOutage</p>
     * 
     * @deprecated Fetch outages by primary key instead of the nodeid/ipAddr/service tuple.
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName TODO
     * @param dbid a int.
     * @param time a {@link java.lang.String} object.
     */
    void resolveOutage(int nodeId, String ipAddr, String svcName, int dbid, String time);

    /**
     * <p>reparentOutages</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param oldNodeId a int.
     * @param newNodeId a int.
     */
    void reparentOutages(String ipAddr, int oldNodeId, int newNodeId);

    /**
     * @param nodeId
     * @return
     */
    List<String[]> getNodeServices(int nodeId);

    void closeOutagesForUnmanagedServices();

    void closeOutagesForNode(Date closeDate, int eventId, int nodeId);

    void closeOutagesForInterface(Date closeDate, int eventId, int nodeId, String ipAddr);

    void closeOutagesForService(Date closeDate, int eventId, int nodeId, String ipAddr, String serviceName);

    void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status);

}

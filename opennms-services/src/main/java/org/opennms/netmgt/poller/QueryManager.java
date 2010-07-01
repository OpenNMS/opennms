/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created November 20, 2004
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
/*
 * Created on Nov 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.poller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

/**
 * <p>QueryManager interface.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface QueryManager {
    /**
     * <p>activeServiceExists</p>
     *
     * @param whichEvent a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName);

    /**
     * <p>getActiveServiceIdsForInterface</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getActiveServiceIdsForInterface(String ipaddr) throws SQLException;

    /**
     * <p>getNodeIDForInterface</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     * @return a int.
     */
    public int getNodeIDForInterface(String ipaddr) throws SQLException;

    /**
     * <p>getNodeLabel</p>
     *
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel(int nodeId) throws SQLException;

    /**
     * <p>getServiceCountForInterface</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     * @return a int.
     */
    public int getServiceCountForInterface(String ipaddr) throws SQLException;

    /**
     * <p>getInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     * @return a {@link java.util.List} object.
     */
    public List<IfKey> getInterfacesWithService(String svcName) throws SQLException;

    /**
     * <p>getServiceLostDate</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return a {@link java.util.Date} object.
     */
    public Date getServiceLostDate(int nodeId, String ipAddr, String svcName, int serviceId);

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource);
    
    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    @Deprecated
    public DataSource getDataSource();

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
    public void openOutage(String outageIdSQL, int nodeId, String ipAddr, String svcName, int dbid, String time);

    /**
     * <p>resolveOutage</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName TODO
     * @param dbid a int.
     * @param time a {@link java.lang.String} object.
     */
    public void resolveOutage(int nodeId, String ipAddr, String svcName, int dbid, String time);

    /**
     * <p>reparentOutages</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param oldNodeId a int.
     * @param newNodeId a int.
     */
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId);
    
    

    /**
     * <p>getCriticalPath</p>
     *
     * @param nodeId a int.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getCriticalPath(int nodeId);
}

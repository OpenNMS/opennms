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
import java.util.Map;

import org.opennms.netmgt.config.DbConnectionFactory;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface QueryManager {
    /**
     * @param whichEvent
     * @param nodeId
     * @param ipAddr
     * @param serviceName
     * @return
     */
    public boolean activeServiceExists(String whichEvent, int nodeId, String ipAddr, String serviceName);

    /**
     * @param nameToId
     * @param idToName
     * @return
     */
    public void buildServiceNameToIdMaps(Map nameToId, Map idToName);

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public List getActiveServiceIdsForInterface(String ipaddr) throws SQLException;

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public int getNodeIDForInterface(String ipaddr) throws SQLException;

    /**
     * @param nodeId
     * @return
     * @throws SQLException
     */
    public String getNodeLabel(int nodeId) throws SQLException;

    /**
     * @param ipaddr
     * @return
     * @throws SQLException
     */
    public int getServiceCountForInterface(String ipaddr) throws SQLException;

    /**
     * @param svcName
     * @return
     * @throws SQLException
     */
    public List getInterfacesWithService(String svcName) throws SQLException;

    /**
     * @param poller
     * @param nodeId
     * @param ipAddr
     * @param svcName
     * @return
     */
    public Date getServiceLostDate(int nodeId, String ipAddr, String svcName, int serviceId);

    /**
     * @param connectionFactory
     */
    public void setDbConnectionFactory(DbConnectionFactory connectionFactory);
}
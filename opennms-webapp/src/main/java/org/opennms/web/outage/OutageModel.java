/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.outage.OutageSummary;

/**
 * As the nonvisual logic for the Services Down (Outage) servlet and JSPs, this
 * class queries the database for current outages and provides utility methods
 * for manipulating that list of outages.
 * 
 * @deprecated Use {@link OutageDao} instead.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public abstract class OutageModel {

    /**
     * <p>getCurrentOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getCurrentOutagesForNode(int nodeId) throws SQLException {
        Outage[] outages = new Outage[0];

        final DBUtils d = new DBUtils(OutageModel.class);

        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement(""
            		+ "SELECT DISTINCT \n" + 
            		"         outages.outageid, outages.iflostservice, outages.ifregainedservice, node.nodeID, \n" + 
            		"         node.nodeLabel, \n" +
                    "         node.location, \n" +
                    "         ipinterface.ipaddr, \n" +
            		"         ipinterface.iphostname, \n" + 
            		"         service.servicename, \n" + 
            		"         ifservices.serviceId \n" + 
            		"    from outages \n" + 
            		"    join ifservices \n" + 
            		"      on ifservices.id = outages.ifserviceid \n" + 
            		"    join ipinterface \n" + 
            		"      on ipinterface.id = ifservices.ipinterfaceid \n" + 
            		"    join node \n" + 
            		"       on node.nodeid = ipinterface.nodeid \n" + 
            		"    join service \n" + 
            		"      on ifservices.serviceid = service.serviceid \n" + 
            		"   where node.nodeid = ? \n" + 
            		"     and outages.ifregainedservice is null \n" + 
            		"     and outages.suppresstime is null \n" + 
            		"      or outages.suppresstime < now() \n" + 
            		"order by iflostservice desc");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            outages = rs2Outages(rs, false);
        } finally {
            d.cleanUp();
        }

        return outages;
    }

    /**
     * Return a list of IP addresses, the number of services down on each IP
     * address, and the longest time a service has been down for each IP
     * address. The list will be sorted by the amount of time it has been down.
     *
     * @param date the starting date for the query
     * @return an array of {@link org.opennms.netmgt.model.outage.OutageSummary} objects.
     * @throws java.sql.SQLException if any.
     */
    public static OutageSummary[] getAllOutageSummaries(Date date) throws SQLException {
        OutageSummary[] summaries = new OutageSummary[0];

        final DBUtils d = new DBUtils(OutageModel.class);

        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT node.nodeid, node.location, outages.iflostservice as timeDown, outages.ifregainedservice as timeUp, node.nodelabel "
                        + "FROM outages, node, ipinterface, ifservices "
                        + "WHERE node.nodeid=ipinterface.nodeid "
                        + "AND ipinterface.id=ifservices.ipinterfaceid AND ifservices.id=outages.ifserviceid "
                        + "AND node.nodeType != 'D' "
                        + "AND ipinterface.ismanaged != 'D' "
                        + "AND ifservices.status != 'D' "
                        + "AND outages.iflostservice >= ? "
                        + "ORDER BY timeDown DESC;"
            );
            d.watch(stmt);
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            List<OutageSummary> list = new ArrayList<>();

            while (rs.next()) {
                int nodeId = rs.getInt("nodeID");
                
                Timestamp timeDown = rs.getTimestamp("timeDown");
                Date downDate = new Date(timeDown.getTime());
                
                Timestamp timeUp = rs.getTimestamp("timeUp");
                Date upDate = null;
                if (timeUp != null) {
                    upDate = new Date(timeUp.getTime());
                }
                
                String nodeLabel = rs.getString("nodelabel");

                list.add(new OutageSummary(nodeId, nodeLabel, downDate, upDate));
            }

            summaries = list.toArray(new OutageSummary[list.size()]);
        } finally {
            d.cleanUp();
        }

        return summaries;
    }

    /**
     * <p>rs2Outages</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @param includesRegainedTime a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    private static Outage[] rs2Outages(ResultSet rs, boolean includesRegainedTime) throws SQLException {
        return rs2Outages(rs, includesRegainedTime, false);
    }

    /*
     * LJK Feb 21, 2002: all these special case result set methods need to be
     * cleaned up
     */
    /**
     * <p>rs2Outages</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @param includesRegainedTime a boolean.
     * @param includesNotifications a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    private static Outage[] rs2Outages(ResultSet rs, boolean includesRegainedTime, boolean includesNotifications) throws SQLException {
        Outage[] outages = null;
        List<Outage> list = new ArrayList<>();

        while (rs.next()) {
            Outage outage = new Outage();

            outage.nodeId = rs.getInt("nodeid");
            
            outage.ipAddress = rs.getString("ipaddr");

            outage.serviceId = rs.getInt("serviceid");

            outage.nodeLabel = rs.getString("nodeLabel");

            outage.location = rs.getString("location");

            outage.hostname = rs.getString("iphostname");

            outage.serviceName = rs.getString("servicename");

            outage.outageId = rs.getInt("outageid");

            Timestamp lostService = rs.getTimestamp("iflostservice");
            if (!rs.wasNull()) {
                outage.lostServiceTime = new Date(lostService.getTime());
            }

            if (includesRegainedTime) {
                Timestamp regainedService = rs.getTimestamp("ifregainedservice");
                if (!rs.wasNull()) {
                    outage.regainedServiceTime = new Date(regainedService.getTime());
                }
            }

            if (includesNotifications) {
                int serviceLostEventId = rs.getInt("svclosteventid");
                if (!rs.wasNull()) {
                    outage.lostServiceEventId = Integer.valueOf(serviceLostEventId);
                }

                int notifyId = rs.getInt("notifyid");
                if (!rs.wasNull()) {
                    outage.lostServiceNotificationId = Integer.valueOf(notifyId);
                }

                outage.lostServiceNotificationAcknowledgedBy = rs.getString("answeredby");
            }

            list.add(outage);
        }

        outages = list.toArray(new Outage[list.size()]);

        return outages;
    }

}

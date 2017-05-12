/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.eventd.AbstractEventUtil;
import org.opennms.netmgt.eventd.EventdConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * EventUtil is used primarily for the event parm expansion - has methods used
 * by all the event components to send in the event and the element to expanded
 * and have the 'expanded' value sent back
 * 
 * @deprecated This JDBC implementation is only used by test classes that are too
 * old to use Spring initialization.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class EventUtilJdbcImpl extends AbstractEventUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EventUtilJdbcImpl.class);

	/**
	 * Retrieve nodeLabel from the node table of the database given a particular
	 * nodeId.
	 * 
	 * @deprecated Replace with standard DAO calls instead of using JDBC
	 * @param nodeId
	 *            Node identifier
	 * 
	 * @return nodeLabel Retrieved nodeLabel
	 * 
	 * @throws SQLException
	 *             if database error encountered
	 */
	@Override
    public String getNodeLabel(long nodeId) throws SQLException {

		String nodeLabel = null;
		java.sql.Connection dbConn = null;
		try {
		    Statement stmt = null;
		    try {
		        // Get datbase connection from the factory
		        dbConn = DataSourceFactory.getInstance().getConnection();

		        // Issue query and extract nodeLabel from result set
		        stmt = dbConn.createStatement();
		        ResultSet rs = stmt
		                .executeQuery("SELECT nodelabel FROM node WHERE nodeid="
		                        + String.valueOf(nodeId));
		        if (rs.next()) {
		            nodeLabel = rs.getString("nodelabel");
		        }
		    } finally {
		        // Close the statement
		        if (stmt != null) {
		            try {
		                stmt.close();
		            } catch (Throwable e) {
		                // do nothing
		            }
		        }
		    }
		} finally {

			// Close the database connection
			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}

		return nodeLabel;
	}

    /**
     * Retrieve nodeLocation from the node table of the database given a particular
     * nodeId.
     *
     * @deprecated Replace with standard DAO calls instead of using JDBC
     * @param nodeId
     *            Node identifier
     *
     * @return nodeLocation Retrieved nodeLocation
     *
     * @throws SQLException
     *             if database error encountered
     */
    @Override
    public String getNodeLocation(long nodeId) throws SQLException {

        String nodeLocation = null;
        java.sql.Connection dbConn = null;
        try {
            Statement stmt = null;
            try {
                // Get datbase connection from the factory
                dbConn = DataSourceFactory.getInstance().getConnection();

                // Issue query and extract nodeLocation from result set
                stmt = dbConn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT location FROM node WHERE nodeid="
                                + String.valueOf(nodeId));
                if (rs.next()) {
                    nodeLocation = rs.getString("location");
                }
            } finally {
                // Close the statement
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable e) {
                        // do nothing
                    }
                }
            }
        } finally {

            // Close the database connection
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }

        return nodeLocation;
    }

	@Override
    public String getIfAlias(long nodeId, String ipaddr) throws SQLException {
		
		String ifAlias = null;
		java.sql.Connection dbConn = null;
		try {
	        Statement stmt = null;
	        try {
	            // Get database connection from the factory
	            dbConn = DataSourceFactory.getInstance().getConnection();
	            DbIpInterfaceEntry ipif = DbIpInterfaceEntry.get(dbConn, nodeId, InetAddressUtils.getInetAddress(ipaddr));
	            if (ipif != null) {
	                DbSnmpInterfaceEntry snmpif = DbSnmpInterfaceEntry.get(dbConn, nodeId, ipif.getIfIndex());
	                if (snmpif != null) ifAlias = snmpif.getAlias();
	            }
	        } finally {
	            // Close the statement
	            if (stmt != null) {
	                try {
	                    stmt.close();
	                } catch (Throwable e) {
	                    // do nothing
	                }
	            }
	        }
		} finally {
			
			// Close the database connection
			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
		
		return ifAlias;
	}

    @Override
    public String getAssetFieldValue(String parm, long nodeId) {
        String retParmVal = null;
        int end = parm.lastIndexOf(ASSET_END_SUFFIX);
        // The "asset[" start of this parameter is 6 characters long
        String assetField = parm.substring(6,end);
        java.sql.Connection dbConn = null;
        try {
             Statement stmt = null;
             try {
                    // Get datbase connection from the factory
                    dbConn = DataSourceFactory.getInstance().getConnection();

                    // Issue query and extract nodeLabel from result set
                    stmt = dbConn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT " + assetField + " FROM assets WHERE nodeid=" + String.valueOf(nodeId));
                         if (rs.next()) {
                             retParmVal = rs.getString(assetField);
                         }
                  } catch (SQLException sqlE) {
                                // do nothing
                    } finally {
                        // Close the statement
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable e) {
                                // do nothing
                            }
                        }
                    }
                  } finally {

                        // Close the database connection
                        if (dbConn != null) {
                                try {
                                        dbConn.close();
                                } catch (Throwable t) {
                                        // do nothing
                                }
                        }
                }
        return retParmVal;
    }

    @Override
    public String getHardwareFieldValue(String parm, long nodeId) {
        String retParmVal = null;
        int end = parm.lastIndexOf(HARDWARE_END_SUFFIX);
        // The "hardware[" start of this parameter is 6 characters long
        String[] parts = parm.substring(HARDWARE_BEGIN.length(),end).split(":");
        if (parts.length != 2) {
            return null;
        }
        String sql = null;
        boolean isCustomAttr = !parts[1].startsWith("entPhysical");
        if (isCustomAttr) {
            sql = "SELECT a.attribValue FROM hwEntityAttribute a, hwEntity h, hwEntityAttributeType t WHERE h.nodeId = " + String.valueOf(nodeId) + " AND a.hwEntityId = h.id AND a.hwAttribTypeId = t.id AND t.attribName = '" + parts[1] + "'";
        } else {
            sql = "SELECT " + parts[1] + " FROM hwEntity WHERE nodeId = " + String.valueOf(nodeId);
        }
        if (parts[0].matches("^\\d+$")) {
            // entPhysicalIndex
            if (isCustomAttr) {
                sql += " AND h.entPhysicalIndex = " + parts[0];
            } else {
                sql += " AND entPhysicalIndex = " + parts[0];
            }
        } else {
            if (parts[0].startsWith("~")) {
                // ~regexOverEntPhysicalName
                if (isCustomAttr) {
                    sql += " AND h.entPhysicalName ~ '" + parts[0].substring(1) + "'";
                } else {
                    sql += " AND entPhysicalName ~ '" + parts[0].substring(1) + "'";
                }
            } else {
                // entPhysicalName
                if (isCustomAttr) {
                    sql += " AND h.entPhysicalName = '" + parts[0] + "'";
                } else {
                    sql += " AND entPhysicalName = '" + parts[0] + "'";
                }
            }
        }
        java.sql.Connection dbConn = null;
        try {
            Statement stmt = null;
            try {
                // Get datbase connection from the factory
                dbConn = DataSourceFactory.getInstance().getConnection();
                // Issue query and extract nodeLabel from result set
                stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    retParmVal = rs.getString(1);
                }
            } catch (SQLException sqlE) {
                // do nothing
            } finally {
                // Close the statement
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable e) {
                        // do nothing
                    }
                }
            }
        } finally {
            // Close the database connection
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }
        return retParmVal;
    }

    /**
     * This method is used to convert the event host into a hostname id by
     * performing a lookup in the database. If the conversion is successful then
     * the corresponding hosname will be returned to the caller.
     * @param nodeId TODO
     * @param hostip
     *            The event host
     * 
     * @return The hostname
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error accessing the stored data or
     *                the SQL text is malformed.
     * 
     * @see EventdConstants#SQL_DB_HOSTIP_TO_HOSTNAME
     * 
     * @deprecated Replace with standard DAO calls instead of using JDBC
     */
    @Override
    public String getHostName(final int nodeId, final String hostip) throws SQLException {
        try {
            final String hostname = new JdbcTemplate(DataSourceFactory.getInstance()).queryForObject(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME, String.class, new Object[] { nodeId, hostip });
            return (hostname != null) ? hostname : hostip;
        } catch (final EmptyResultDataAccessException e) {
            return hostip;
        }
    }

	/**
	 * Retrieve foreignSource from the node table of the database given a particular
	 * nodeId.
	 *
	 * @deprecated Replace with standard DAO calls instead of using JDBC
	 * @param nodeId
	 *            Node identifier
	 *
	 * @return foreignSource Retrieved foreignSource
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
    @Override
    public String getForeignSource(long nodeId) throws SQLException {
        String foreignSource = null;
        java.sql.Connection dbConn = null;
        try {
            Statement stmt = null;
            try {
                // Get datbase connection from the factory
                dbConn = DataSourceFactory.getInstance().getConnection();

                // Issue query and extract nodeLabel from result set
                stmt = dbConn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT foreignsource FROM node WHERE nodeid="
                                + String.valueOf(nodeId));
                if (rs.next()) {
                    foreignSource = rs.getString("foreignsource");
                }
            } finally {
                // Close the statement
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable e) {
                        // do nothing
                    }
                }
            }
        } finally {

          // Close the database connection
          if (dbConn != null) {
            try {
              dbConn.close();
            } catch (Throwable t) {
              // do nothing
            }
          }
        }

        return foreignSource;
    }

    @Override
    public String getForeignId(long nodeId) throws SQLException {
        try (Connection dbConn = DataSourceFactory.getInstance().getConnection();
             PreparedStatement stmt = dbConn.prepareStatement("SELECT foreignId FROM node WHERE nodeid=?");
        ) {
            stmt.setString(1, String.valueOf(nodeId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("foreignId");
            }
        }
        return null;
    }
}

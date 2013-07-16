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

package org.opennms.netmgt.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.RowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class for methods to encode/decode ifLabel descriptions for
 * storing SNMP data in an RRD file.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 */
public class IfLabel extends Object {

	private static final Logger LOG = LoggerFactory.getLogger(IfLabel.class);
	

    /**
     * Return a map of useful SNMP information for the interface specified by
     * the nodeId and ifLabel. Essentially a "decoding" algorithm for the
     * ifLabel.
     *
     * @param nodeId
     *            Node id
     * @param ifLabel
     *            Interface label of format: <description>- <macAddr>
     * @return Map of SNMP info keyed by 'snmpInterface' table column names for
     *         the interface specified by nodeId and ifLabel args.
     * @throws SQLException
     *             if error occurs accessing the database.
     */
    public static Map<String, String> getInterfaceInfoFromIfLabel(int nodeId, String ifLabel) {
        if (ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final Map<String, String> info = new HashMap<String, String>();
        String desc = ifLabel;
        String mac = null;

        // first I have to strip off the MAC address from the end, if there is
        // one
        int dashIndex = ifLabel.lastIndexOf("-");

        if (dashIndex >= 0) {
            desc = ifLabel.substring(0, dashIndex);
            mac = ifLabel.substring(dashIndex + 1, ifLabel.length());
        }
        
       final String desc2 = desc;
       final String mac2 = mac;
 
        LOG.debug("getInterfaceInfoFromIfLabel: desc={} mac={}", desc, mac);

        String queryDesc = desc.replace('_', '%');

        String query = "" +
                "SELECT * " +
                "  FROM snmpinterface " +
                " WHERE nodeid = "+nodeId+
                "   AND (snmpifdescr ILIKE '"+queryDesc+"'" +
                "    OR snmpifname ilike '"+queryDesc+"')";
        LOG.debug("getInterfaceInfoFromLabel: query is: {}", query);
        
        Querier q = new Querier(Vault.getDataSource(), query, new RowProcessor() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // If the description portion of ifLabel matches an entry
                // in the snmpinterface table...

                /*
                 * When Cisco Express Forwarding (CEF) or some ATM encapsulations
                 * (AAL5) are used on Cisco routers, an additional entry might be 
                 * in the ifTable for these sub-interfaces, but there is no
                 * performance data available for collection.  This check excludes
                 * ifTable entries where ifDescr contains "-cef".  See bug #803.
                 */
                if (rs.getString("snmpifdescr") != null) {
                    if (Pattern.matches(".*-cef.*", rs.getString("snmpifdescr")))
                        return;
                }

                if ((AlphaNumeric.parseAndReplace(rs.getString("snmpifname"), '_').equals(desc2)) || (AlphaNumeric.parseAndReplace(rs.getString("snmpifdescr"), '_').equals(desc2))) {

                    // If the mac address portion of the ifLabel matches
                    // an entry in the snmpinterface table...
                    if (mac2 == null || mac2.equals(rs.getString("snmpphysaddr"))) {
                        LOG.debug("getInterfaceInfoFromIfLabel: found match...");
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            // Get extra information about the interface
                            info.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                        }
                    }
                }
            }
            
        });
        q.execute();
        LOG.debug("getInterfaceInfoFromLabel: Querier result count is: {}", q.getCount());
        
        // The map will remain empty if the information was not located in the
        // DB.
        return Collections.unmodifiableMap(info);
    }

    /**
     * Get the interface labels for each interface on a given node.
     *
     * @param nodeId a int.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    public static String[] getIfLabels(int nodeId) throws SQLException {
        
        String query = "" +
        		"SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr " +
        		"  FROM snmpinterface, ipinterface " +
        		" WHERE (ipinterface.ismanaged!='D') " +
        		"   AND ipinterface.nodeid=snmpinterface.nodeid " +
        		"   AND ifindex = snmpifindex " +
        		"   AND ipinterface.nodeid="+nodeId;
        
        final ArrayList<String> list = new ArrayList<String>();
        
        Querier q = new Querier(Vault.getDataSource(), query, new RowProcessor() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                list.add(getIfLabel(name, descr, physAddr));
            }
            
        });
        q.execute();
        String[] labels = list.toArray(new String[list.size()]);
        return labels;
    }

    /**
     * <p>getIfLabel</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getIfLabel(final int nodeId, final String ipAddr) {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String inetAddr = org.opennms.core.utils.InetAddressUtils.normalize(ipAddr);
        
        class LabelHolder {
            private String m_label;

            public void setLabel(String label) {
                m_label = label;
            }

            public String getLabel() {
                return m_label;
            }
        }
        
        final LabelHolder holder = new LabelHolder();

        String query = "" +
        		"SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr " +
        		"  FROM snmpinterface, ipinterface " +
        		" WHERE (ipinterface.ismanaged!='D') " +
        		"   AND ipinterface.nodeid=snmpinterface.nodeid " +
        		"   AND ifindex=snmpifindex " +
        		"   AND ipinterface.nodeid = "+nodeId+
        		"   AND ipinterface.ipaddr = '"+inetAddr+"'";
        
        Querier q = new Querier(Vault.getDataSource(), query, new RowProcessor() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                if (name != null || descr != null) {
                    holder.setLabel(getIfLabel(name, descr, physAddr));
                } else {
                    LOG.warn("Interface (nodeId/ipAddr={}/{}) has no ifName and no ifDescr...setting to label to 'no_ifLabel'.", nodeId, ipAddr);
                    holder.setLabel("no_ifLabel");
                }
            }
        });
        q.execute();
        
        return holder.getLabel();
    }

    /**
     * <p>getIfLabelfromIfIndex</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public static String getIfLabelfromIfIndex(final int nodeId, final String ipAddr, final int ifIndex) {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String inetAddr = org.opennms.core.utils.InetAddressUtils.normalize(ipAddr);

        if (ifIndex == -1) {
        	return getIfLabel(nodeId, inetAddr);
        }
        
        class LabelHolder {
            private String m_label;

            public void setLabel(String label) {
                m_label = label;
            }

            public String getLabel() {
                return m_label;
            }
        }
        
        final LabelHolder holder = new LabelHolder();
        
        String query = "" +
        		"SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr " +
        		"  FROM snmpinterface, ipinterface " +
        		" WHERE (ipinterface.ismanaged!='D') " +
        		"   AND ipinterface.nodeid=snmpinterface.nodeid " +
        		"   AND ifindex=snmpifindex " +
        		"   AND ipinterface.nodeid= "+nodeId+
        		"   AND ipinterface.ipaddr= '"+inetAddr+"'"+
        		"   AND ipinterface.ifindex= "+ifIndex;
        
        
        Querier q = new Querier(Vault.getDataSource(), query, new RowProcessor() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                if (name != null || descr != null) {
                    holder.setLabel(getIfLabel(name, descr, physAddr));
                } else {
                    LOG.warn("Interface (nodeId/ipAddr={}/{}) has no ifName and no ifDescr...setting to label to 'no_ifLabel'.", nodeId, ipAddr);
                    holder.setLabel("no_ifLabel");
                }
            }
            
        });
        q.execute();
        
        return holder.getLabel();
    }
 
    /**
     * Return the ifLabel as a string for the given node and ifIndex. Intended for
     * use with non-ip interfaces.
     *
     * @return String
     * @param nodeId a int.
     * @param ifIndex a int.
     */
    public static String getIfLabelfromSnmpIfIndex(final int nodeId, final int ifIndex) {
        
        class LabelHolder {
            private String m_label;

            public void setLabel(String label) {
                m_label = label;
            }

            public String getLabel() {
                return m_label;
            }
        }
        
        final LabelHolder holder = new LabelHolder();
        
        String query = "" +
                "SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr " +
                "  FROM snmpinterface " +
                "   WHERE nodeid= "+nodeId+
                "   AND snmpifindex= "+ifIndex;
        
        
        Querier q = new Querier(Vault.getDataSource(), query, new RowProcessor() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                if (name != null || descr != null) {
                    holder.setLabel(getIfLabel(name, descr, physAddr));
                } else {
                    LOG.warn("Interface (nodeId/ifIndex={}/{}) has no ifName and no ifDescr...setting to label to 'no_ifLabel'.", nodeId, ifIndex);
                    holder.setLabel("no_ifLabel");
                }
            }
            
        });
        q.execute();
        
        return holder.getLabel();
    }    

    /**
     * <p>getIfLabel</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param descr a {@link java.lang.String} object.
     * @param physAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getIfLabel(String name, String descr, String physAddr) {
        // If available ifName is used to generate the label
        // since it is guaranteed to be unique. Otherwise
        // ifDescr is used. In either case, all non
        // alpha numeric characters are converted to
        // underscores to ensure that the resulting string
        // will make a decent file name and that RRD
        // won't have any problems using it
        //
        String label = null;

        if (name != null) {
            label = AlphaNumeric.parseAndReplace(name, '_');
        } else if (descr != null) {
            label = AlphaNumeric.parseAndReplace(descr, '_');
        } else {
            throw new IllegalArgumentException("Both name and descr are null, but at least one cannot be.");
        }

        // In order to assure the uniqueness of the
        // RRD file names we now append the MAC/physical
        // address to the end of label if it is available.
        // 
        if (physAddr != null) {
            physAddr = AlphaNumeric.parseAndTrim(physAddr);
            if (physAddr.length() == 12) {
                label = label + "-" + physAddr;
            } else {
            	LOG.debug("initialize: physical address len is NOT 12, physAddr={}", physAddr);
            }
        }

        return label;
    }
}

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

package org.opennms.netmgt.rtc;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.rtc.datablock.RTCHashMap;
import org.opennms.netmgt.rtc.datablock.RTCNode;
import org.opennms.netmgt.rtc.datablock.RTCNodeKey;
import org.opennms.netmgt.rtc.utils.LegacyEuiLevelMapper;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Contains and maintains all the data for the RTC.
 *
 * The basic datablock is a 'RTCNode' that gets added to relevant
 * 'RTCCategory's. it also gets added to a map with different keys for easy
 * lookup
 *
 * The map('RTCHashMap') is keyed with 'RTCNodeKey's(a nodeid/ip/svc
 * combination), nodeid/ip combinations and nodeid and these keys either lookup
 * a single RTCNode or lists of 'RTCNode's
 *
 * Incoming events have a method in the DataManager to alter data - for e.g., a
 * 'nodeGainedService' event would result in the 'nodeGainedService()' method
 * being called by the DataUpdater(s).
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class DataManager implements AvailabilityService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataManager.class);

    @Autowired
	private FilterDao m_filterDao;

    @Autowired
	private RTCConfigFactory m_configFactory;

    @Autowired
	private TransactionTemplate m_transactionTemplate;

    @Autowired
	private JdbcTemplate m_jdbcTemplate;

	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

    /**
     * The category to XML mapper
     */
    private final LegacyEuiLevelMapper m_euiMapper;

	private class RTCNodeProcessor implements RowCallbackHandler {
		RTCNodeKey m_currentKey = null;

		Map<String,Set<Integer>> m_categoryNodeIdLists = new HashMap<String,Set<Integer>>();


		@Override
		public void processRow(ResultSet rs) throws SQLException {
			RTCNodeKey key = new RTCNodeKey(rs.getInt("nodeid"), InetAddressUtils.addr(rs.getString("ipaddr")), rs.getString("servicename"));
			processKey(key);
			processOutage(key, rs.getTimestamp("ifLostService"), rs.getTimestamp("ifRegainedService"));
		}

		private void processKey(RTCNodeKey key) {
			if (!matchesCurrent(key)) {
				m_currentKey = key;
				processIfService(key);
			}
		}

		private boolean matchesCurrent(RTCNodeKey key) {
			return (m_currentKey != null && m_currentKey.equals(key));
		}

		// This is called exactly once for each unique (node ID, IP address, service name) tuple
		public synchronized void processIfService(RTCNodeKey key) {
			for (RTCCategory cat : m_categories.values()) {
				if (catContainsIfService(cat, key)) {
					RTCNode rtcN = getRTCNode(key);
					addNodeToCategory(cat, rtcN);
				}
			}
		
		}

		private RTCNode getRTCNode(RTCNodeKey key) {
			RTCNode rtcN = m_map.getRTCNode(key);
			if (rtcN == null) {
				rtcN = new RTCNode(key, m_configFactory.getRollingWindow());
				addRTCNode(rtcN);
			}
			return rtcN;
		}

		private boolean catContainsIfService(RTCCategory cat, RTCNodeKey key) {
			return cat.containsService(key.getSvcName()) && catContainsNode(cat, (int)key.getNodeID());
		}
		
		private boolean catContainsNode(RTCCategory cat, Integer nodeID) {			
			Set<Integer> nodeIds = catGetNodeIds(cat);
			return nodeIds.contains(nodeID);
		}
		
		private Set<Integer> catGetNodeIds(RTCCategory cat) {
			Set<Integer> nodeIds = m_categoryNodeIdLists.get(cat.getLabel());
			// TODO: Put an expiration on this value so that it can reload when categories change
			if(nodeIds == null) {
				nodeIds = RTCUtils.getNodeIdsForCategory(m_filterDao, cat);
				m_categoryNodeIdLists.put(cat.getLabel(), nodeIds);
			}
			return nodeIds;
		}
		
		// This is processed for each outage, passing two null means there is not outage
		public void processOutage(RTCNodeKey key, Timestamp ifLostService, Timestamp ifRegainedService) {
			RTCNode rtcN = m_map.getRTCNode(key);
			// if we can't find the node it doesn't belong to any category
			if (rtcN == null) return;
			
			addOutageToRTCNode(rtcN, ifLostService, ifRegainedService);
		}
	}

    /**
     * The RTC categories
     */
    private Map<String, RTCCategory> m_categories;

    /**
     * map keyed using the RTCNodeKey or node ID or node ID/IP address
     */
    private RTCHashMap m_map;

	private static void addOutageToRTCNode(RTCNode rtcN, Timestamp lostTimeTS, Timestamp regainedTimeTS) {
		if (lostTimeTS == null) return;
		long lostTime = lostTimeTS.getTime();
		long regainedTime = -1;
		if (regainedTimeTS != null)
			regainedTime = regainedTimeTS.getTime();

		LOG.debug("lost time for nodeid/ip/svc: {}/{}/{}: {}/{}", rtcN.getNodeID(), rtcN.getIP(), rtcN.getSvcName(), lostTimeTS, lostTime);

		LOG.debug("regained time for nodeid/ip/svc: {}/{}/{}: {}/{}", rtcN.getNodeID(), rtcN.getIP(), rtcN.getSvcName(), regainedTimeTS, regainedTime);

		rtcN.addSvcTime(lostTime, regainedTime);
	}

	private void addRTCNode(RTCNode rtcN) {
		m_map.add(rtcN);
	}

	private static void addNodeToCategory(RTCCategory cat, RTCNode rtcN) {

		// add the category info to the node
        rtcN.addCategory(cat.getLabel());

		// Add node to category
		cat.addNode(rtcN);

		LOG.debug("rtcN : {}/{}/{} added to cat: {}", rtcN.getNodeID(), rtcN.getIP(), rtcN.getSvcName(), cat.getLabel());
	}

    /**
     * Populates nodes from the database. For each category in the categories
     * list, this reads the services and outage tables to get the initial data,
     * creates 'RTCNode' objects that are added to the map and and to the
     * appropriate category.
     * @param dbConn
     *            the database connection.
     * 
     * @throws SQLException
     *             if the database read fails due to an SQL error
     * @throws FilterParseException
     *             if filtering the data against the category rule fails due to
     *             the rule being incorrect
     * @throws RTCException
     *             if the database read or filtering the data against the
     *             category rule fails for some reason
     */
    private void populateNodesFromDB(String query, Object[] args) throws SQLException, FilterParseException, RTCException {

    	final String getOutagesInWindow = 
    			"select " + 
    			"       ifsvc.nodeid as nodeid, " + 
    			"       ifsvc.ipAddr as ipaddr, " + 
    			"       s.servicename as servicename, " + 
    			"       o.ifLostService as ifLostService, " + 
    			"       o.ifRegainedService as ifRegainedService " + 
    			"  from " + 
    			"       ifservices ifsvc " + 
    			"  join " + 
    			"       service s on (ifsvc.serviceid = s.serviceid) " + 
    			"left outer  join " + 
    			"       outages o on " +
    			"          (" + 
    			"            o.nodeid = ifsvc.nodeid " + 
    			"            and o.ipaddr = ifsvc.ipaddr " + 
    			"            and o.serviceid = ifsvc.serviceid " + 
    			"            and " +
    			"            (" + 
    			"               o.ifLostService > ? " + 
    			"               OR  o.ifRegainedService > ? " + 
    			"               OR  o.ifRegainedService is null " +
    			"            )" +
    			"          ) " +
    			" where ifsvc.status='A' " +
                (query == null ? "" : "and "+query) +
    			" order by " + 
    			"       ifsvc.nodeid, ifsvc.ipAddr, ifsvc.serviceid, o.ifLostService ";
    	
		long window = (new Date()).getTime() - (24L * 60L * 60L * 1000L);
		Timestamp windowTS = new Timestamp(window);

    	RowCallbackHandler rowHandler = new RTCNodeProcessor();

    	Object[] sqlArgs = createArgs(windowTS, windowTS, args);
    	
    	m_jdbcTemplate.query(getOutagesInWindow, sqlArgs, rowHandler);
    	
    }

	private static Object[] createArgs(Object arg1, Object arg2, Object[] remaining) {
		LinkedList<Object> args = new LinkedList<Object>();
		args.add(arg1);
		args.add(arg2);
		if (remaining != null)
			args.addAll(Arrays.asList(remaining));
		return args.toArray();
	}

    public DataManager() {
        // create category converter
        m_euiMapper = new LegacyEuiLevelMapper(this);
    };

    /**
     * Constructor. Parses categories from the categories.xml and populates them
     * with 'RTCNode' objects created from data read from the database (services
     * and outage tables)
     *
     * @exception SQLException
     *                if there is an error reading initial data from the
     *                database
     * @exception FilterParseException
     *                if a rule in the categories.xml was incorrect
     * @exception RTCException
     *                if the initialization/data reading does not go through
     * @throws org.xml.sax.SAXException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     * @throws org.opennms.netmgt.filter.api.FilterParseException if any.
     * @throws org.opennms.netmgt.rtc.RTCException if any.
     */
	@Override
	public void afterPropertiesSet() throws Exception {
    	// read the categories.xml to get all the categories
    	m_categories = RTCUtils.createCategoriesMap();

    	if (m_categories == null || m_categories.isEmpty()) {
    		throw new RTCException("No categories found in categories.xml");
    	}

    	LOG.debug("Number of categories read: {}", m_categories.size());

    	// create data holder
    	m_map = new RTCHashMap(30000);

    	m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

    		@Override
    		protected void doInTransactionWithoutResult(TransactionStatus arg0) {
    			// Populate the nodes initially from the database
    			try {
    				populateNodesFromDB(null, null);
    			} catch (FilterParseException e) {
    				throw new IllegalStateException("Cannot load RTC data from the database: " + e.getMessage(), e);
    			} catch (SQLException e) {
    				throw new IllegalStateException("Cannot load RTC data from the database: " + e.getMessage(), e);
    			} catch (RTCException e) {
    				throw new IllegalStateException("Cannot load RTC data from the database: " + e.getMessage(), e);
    			}
    		}
    	});
	}

    /**
     * Handles a node gained service event. Add a new entry to the map and the
     * categories on a 'serviceGained' event
     *
     * @param nodeid
     *            the node id
     * @param ip
     *            the IP address
     * @param svcName
     *            the service name
     */
    public synchronized void nodeGainedService(int nodeid, InetAddress ip, String svcName) {
        //
        // check the 'status' flag for the service
        //
        String svcStatus = m_monitoredServiceDao.get((int)nodeid, ip, svcName).getStatus();

        //
        // Include only service status 'A' and where service is not SNMP
        //
        if (!"A".equals(svcStatus)) {
            LOG.info("nodeGainedSvc: {}/{}/{} IGNORED because status is not active: {}", nodeid, ip, svcName, svcStatus);
        } else {
            LOG.debug("nodeGainedSvc: {}/{}/{}/{}", nodeid, ip, svcName, svcStatus);

            // I ran into problems with adding new services, so I just ripped
            // all that out and added
            // a call to the rescan method. -T

            // Hrm - since the rules can be based on things other than the
            // service name
            // we really need to rescan every time a new service is discovered.
            // For
            // example, if I have a category where the rule is "ipaddr =
            // 10.1.1.1 & isHTTP"
            // yet I only have ICMP in the service list, the node will not be
            // added when
            // HTTP is discovered, because it is not in the services list.
            // 
            // This is mainly useful when SNMP is discovered on a node.

            LOG.debug("rtcN : Rescanning services on : {}", ip);
            try {
                rtcNodeRescan(nodeid);
            } catch (FilterParseException ex) {
                LOG.warn("Failed to unmarshall database config", ex);
                throw new UndeclaredThrowableException(ex);
            } catch (SQLException ex) {
                LOG.warn("Failed to get database connection", ex);
                throw new UndeclaredThrowableException(ex);
            } catch (RTCException ex) {
                LOG.warn("Failed to get database connection", ex);
                throw new UndeclaredThrowableException(ex);
            }

        }

    }

    /**
     * Handles a node outage created event. Add a lost service entry to the right
     * node
     *
     * @param nodeid
     *            the node id
     * @param ip
     *            the IP address
     * @param svcName
     *            the service name
     * @param t
     *            the time at which service was lost
     */
    public synchronized void outageCreated(int nodeid, InetAddress ip, String svcName, long t) {
        RTCNodeKey key = new RTCNodeKey(nodeid, ip, svcName);
        RTCNode rtcN = m_map.getRTCNode(key);
        if (rtcN == null) {
            // oops! got a lost/regained service for a node that is not known?
            LOG.info("Received a outageCreated event for an unknown/irrelevant node: {}", key.toString());
            return;
        }

        // inform node
        rtcN.nodeLostService(t);

    }

    /**
     * Handles a node outage resolved event. Add a regained service entry to the right 
     * node.
     *
     * @param nodeid
     *            the node id
     * @param ip
     *            the IP address
     * @param svcName
     *            the service name
     * @param t
     *            the time at which service was regained
     */
    public synchronized void outageResolved(int nodeid, InetAddress ip, String svcName, long t) {
        RTCNodeKey key = new RTCNodeKey(nodeid, ip, svcName);
        RTCNode rtcN = m_map.getRTCNode(key);
        if (rtcN == null) {
            // oops! got a lost/regained service for a node that is not known?
            LOG.info("Received a outageResolved event for an unknown/irrelevant node: {}", key.toString());
            return;
        }

        // inform node
        rtcN.nodeRegainedService(t);
    }

    /**
     * Remove node from the map and the categories on a 'serviceDeleted' event.
     *
     * @param nodeid
     *            the nodeid on which service was deleted
     * @param ip
     *            the ip on which service was deleted
     * @param svcName
     *            the service that was deleted
     */
    public synchronized void serviceDeleted(int nodeid, InetAddress ip, String svcName) {
        // create lookup key
        RTCNodeKey key = new RTCNodeKey(nodeid, ip, svcName);

        // lookup the node
        RTCNode rtcN = m_map.getRTCNode(key);
        if (rtcN == null) {
            LOG.warn("Received a {} event for an unknown node: {}", EventConstants.SERVICE_DELETED_EVENT_UEI, key.toString());

            return;
        }

        //
        // Go through from all the categories this node belongs to
        // and delete the service
        //
        List<String> categories = rtcN.getCategories();
        ListIterator<String> catIter = categories.listIterator();
        while (catIter.hasNext()) {
            String catlabel = (String) catIter.next();

            RTCCategory cat = (RTCCategory) m_categories.get(catlabel);

            // get nodes in this category
            List<Integer> catNodes = cat.getNodes();

            // check if the category contains this node
            int nIndex = catNodes.indexOf(rtcN.getNodeID());
            if (nIndex != -1) {
                // remove from the category if it is the only service left.
                if (m_map.getServiceCount(nodeid, catlabel) == 1) {
                    catNodes.remove(nIndex);
                    LOG.info("Removing node from category: {}", catlabel);
                }

                // let the node know that this category is out
                catIter.remove();
            }
        }

        // finally remove from map
        
        m_map.delete(rtcN);

    }
    
    /**
     * <p>assetInfoChanged</p>
     *
     * @param nodeid a long.
     */
    public synchronized void assetInfoChanged(int nodeid) {
        try {
        	rtcNodeRescan(nodeid);
        } catch (FilterParseException ex) {
            LOG.warn("Failed to unmarshall database config", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (SQLException ex) {
            LOG.warn("Failed to get database connection", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (RTCException ex) {
            LOG.warn("Failed to get database connection", ex);
            throw new UndeclaredThrowableException(ex);
        }

    	
    }
    
    /**
     * <p>nodeCategoryMembershipChanged</p>
     *
     * @param nodeid a long.
     */
    public synchronized void nodeCategoryMembershipChanged(int nodeid) {
        try {
        	rtcNodeRescan(nodeid);
        } catch (FilterParseException ex) {
            LOG.warn("Failed to unmarshall database config", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (SQLException ex) {
            LOG.warn("Failed to get database connection", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (RTCException ex) {
            LOG.warn("Failed to get database connection", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * Update the categories for a node. This method will update the categories
     * for all interfaces on a node.
     *
     * @param nodeid
     *            the nodeid on which SNMP service was added
     * @throws java.sql.SQLException
     *             if the database read fails due to an SQL error
     * @throws org.opennms.netmgt.filter.api.FilterParseException
     *             if filtering the data against the category rule fails due to
     *             the rule being incorrect
     * @throws org.opennms.netmgt.rtc.RTCException
     *             if the database read or filtering the data against the
     *             category rule fails for some reason
     */
    public synchronized void rtcNodeRescan(int nodeid) throws SQLException, FilterParseException, RTCException {
    	
    	for (RTCCategory cat : m_categories.values()) {
			cat.deleteNode(nodeid);
		}
    	
    	m_map.deleteNode(nodeid);
    	
    	populateNodesFromDB("ifsvc.nodeid = ?", new Object[] { Long.valueOf(nodeid) });
    	
    }

    /**
     * Reparent an interface. This effectively means updating the nodelist of
     * the categories and the map
     *
     * Use the ip/oldnodeid combination to get all nodes that will be affected -
     * for each of these nodes, remove the old entry and add a new one with new
     * keys to the map
     *
     * <em>Note:</em> Each of these nodes could belong to more than one
     * category. However, category rule evaluation is done based ONLY on the IP -
     * therefore changing the nodeID on the node should update the categories
     * appropriately
     *
     * @param ip
     *            the interface to reparent
     * @param oldNodeId
     *            the node that the IP belonged to earlier
     * @param newNodeId
     *            the node that the IP now belongs to
     */
    public synchronized void interfaceReparented(InetAddress ip, int oldNodeId, int newNodeId) {
        // get all RTCNodes with the IP/old node ID
        for (RTCNode rtcN : m_map.getRTCNodes(oldNodeId, ip)) {

            // remove the node with the old node id from the map
            m_map.delete(rtcN);

            // change the node ID on the RTCNode
            rtcN.setNodeID(newNodeId);

            // now add the node with the new node ID
            m_map.add(rtcN);

            // remove old node ID from the categories it belonged to
            // and the new node ID
            for (String catlabel : rtcN.getCategories()) {
                RTCCategory rtcCat = m_categories.get(catlabel);
                rtcCat.deleteNode(oldNodeId);
                rtcCat.addNode(newNodeId);
            }

        }
    }

    /**
     * Get the value(uptime) for the category in the last 'rollingWindow'
     * starting at current time
     *
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the category in the last 'rollingWindow'
     *         starting at current time
     */
    public synchronized double getValue(RTCCategory category, long curTime, long rollingWindow) {
        return m_map.getValue(category.getLabel(), curTime, rollingWindow);
    }

    /**
     * Get the value(uptime) for the nodeid in the last 'rollingWindow' starting
     * at current time in the context of the passed category
     *
     * @param nodeid
     *            the node for which value is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the node in the last 'rollingWindow'
     *         starting at current time in the context of the passed category
     */
    public synchronized double getValue(int nodeid, RTCCategory category, long curTime, long rollingWindow) {
        return m_map.getValue(nodeid, category.getLabel(), curTime, rollingWindow);
    }

    /**
     * Get the service count for the nodeid in the context of the passed
     * category
     *
     * @param nodeid
     *            the node for which service count is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service count for the nodeid in the context of the passed
     *         category
     */
    public synchronized int getServiceCount(int nodeid, RTCCategory category) {
        return m_map.getServiceCount(nodeid, category.getLabel());
    }

    /**
     * Get the service down count for the nodeid in the context of the passed
     * category
     *
     * @param nodeid
     *            the node for which service down count is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service down count for the nodeid in the context of the
     *         passed category
     */
    public synchronized int getServiceDownCount(int nodeid, RTCCategory category) {
        return m_map.getServiceDownCount(nodeid, category.getLabel());
    }

    /**
     * <p>getCategories</p>
     *
     * @return the categories
     */
    @Override
    public synchronized Map<String, RTCCategory> getCategories() {
        return m_categories;
    }

    public Collection<Integer> getNodes(RTCCategory category) {
        return category.getNodes();
    }

    @Override
    public EuiLevel getEuiLevel(RTCCategory category) {
        return m_euiMapper.convertToEuiLevelXML(category);
    }
}

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

package org.opennms.netmgt.poller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The source for all path outage business objects (nodes, critical path IPs,
 * critical path service names). Encapsulates all lookup functionality for
 * these business objects in one place.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class PathOutageManagerDaoImpl implements PathOutageManager{
	@Autowired
	private NodeDao nodeDao;
	
	@Autowired
	private PathOutageDao pathOutageDao;
	
	@Autowired
	private MonitoredServiceDao monitoredServiceDao;
	
	@Autowired
	private OutageDao outageDao;
	
	@Autowired
	private IpInterfaceDao ipInterfaceDao;
	
    /** Constant <code>NO_CRITICAL_PATH="Not Configured"</code> */
    public static final String NO_CRITICAL_PATH = "Not Configured";
    
    

    /**
     * <p>
     * Retrieve all the critical paths
     * from the database
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public List<String[]> getAllCriticalPaths() throws SQLException {
        final List<String[]> paths = new ArrayList<String[]>();

        List<OnmsPathOutage> outs = pathOutageDao.findAll();
        	
        for (OnmsPathOutage out : outs) {
        	String[] path = new String[2];
            path[0] = InetAddressUtils.str(out.getCriticalPathIp());
            path[1] = out.getCriticalPathServiceName();
            paths.add(path);
        }
        	        
        return paths;
       
    }

    /**
     * <p>
     * Retrieve critical path by nodeid
     * from the database
     *
     * @param nodeID a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public String getPrettyCriticalPath(int nodeID) {
        String result = NO_CRITICAL_PATH;

       	OnmsPathOutage out = pathOutageDao.get(nodeID);
       	result = InetAddressUtils.str(out.getCriticalPathIp()) + " " + out.getCriticalPathServiceName();

        return result;
    }

    public String[] getCriticalPath(int nodeId) {
    	//"SELECT criticalpathip, criticalpathservicename FROM pathoutage WHERE nodeid=?";
    	
    	OnmsPathOutage out = pathOutageDao.get(nodeId);
    	
        final String[] cpath = new String[2];
        cpath[0] = InetAddressUtils.str(out.getCriticalPathIp());
        cpath[1] = out.getCriticalPathServiceName();
        if (cpath[0] != null && cpath[1] != "") {
        	cpath[0] = InetAddressUtils.str(out.getCriticalPathIp());
        	cpath[1] = out.getCriticalPathServiceName();
        }
        else if (cpath[0] == null || cpath[0].equals("")) {
            cpath[0] = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
            cpath[1] = "ICMP";
        }
        else if (cpath[1] == null || cpath[1].equals("")) {
            cpath[1] = "ICMP";
        }
        return cpath;
    }

    /**
     * <p>
     * Retrieve all the nodes in a critical path
     * from the database
     *
     * @param criticalPathIp
     *            IP address of the critical path
     * @param criticalPathServiceName
     *            service name for the critical path
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    public List<String> getNodesInPath(String criticalPathIp, String criticalPathServiceName) {
        final List<String> pathNodes = new ArrayList<String>();
        
        List<OnmsPathOutage> outs = pathOutageDao.findAll();
        for (OnmsPathOutage out: outs) {
        	String node = out.getCriticalPathServiceName();
        	if (node.equals(criticalPathServiceName)) {
        		pathNodes.add(String.valueOf(out.getNode().getId()));
        	}
        }
        
        return pathNodes;
    }

    /**
     * This method is responsible for determining the
     * node label of a node, and the up/down status
     * and status color
     *
     * @param nodeIDStr a {@link java.lang.String} object.
     * @param conn a {@link java.sql.Connection} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    public String[] getLabelAndStatus(String nodeIDStr, Connection conn) {
    	int countManagedSvcs = 0;
        int countOutages = 0;
        String[] result = new String[3];
        result[1] = "Cleared";
        result[2] = "Unmanaged";

        int nodeID = WebSecurityUtils.safeParseInt(nodeIDStr);

        OnmsNode node = nodeDao.get(nodeID);
        result[0] = node.getLabel();
            
        List<OnmsMonitoredService> serve = monitoredServiceDao.findAll();
           
            
        for (OnmsMonitoredService cur: serve) {
        	if ("A".equals(cur.getStatus()) && nodeID == cur.getNodeId()) {
          		countManagedSvcs++;
          	}
        }
            
        if(countManagedSvcs > 0) {
            	
           	List<OnmsOutage> out = outageDao.findAll();
            	
           	for (OnmsOutage outs: out) {
           		if (outs.getServiceRegainedEvent() == null) {
           			countOutages++;
           		}
           	}
            	
            if(countManagedSvcs == countOutages) {
            	result[1] = "Critical";
                result[2] = "All Services Down";
            } else if(countOutages == 0) {
                result[1] = "Normal";
                result[2] = "All Services Up";
            } else {
                result[1] = "Minor";
                result[2] = "Some Services Down";
            }
        }
        
        return result;
        
    }

    /**
     * This method is responsible for determining the
     * data related to the critical path:
     * node label, nodeId, the number of nodes
     * dependent on this path, and the managed state
     * of the path
     *
     * @param criticalPathIp a {@link java.lang.String} object.
     * @param criticalPathServiceName a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    public String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) {
        String[] result = new String[4];
        int count = 0;
        OnmsNode node = new OnmsNode();

        //private static final String GET_NODELABEL_BY_IP = "SELECT nodelabel FROM node WHERE nodeid IN (SELECT nodeid FROM ipinterface WHERE ipaddr=? AND ismanaged!='D')";
        
        final org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsNode.class)
        .setAliases(Arrays.asList(new Alias[] {
            new Alias("ipInterfaces","ipInterfaces", JoinType.LEFT_JOIN)
        }))
        .addRestriction(new EqRestriction("ipInterfaces.ipAddress", InetAddressUtils.addr(criticalPathIp)))
        //TODO: Replace D with a constant
        .addRestriction(new NeRestriction("ipInterfaces.isManaged", "D"));

        List<OnmsNode> nList = nodeDao.findMatching(criteria);
        if(nList.size() < 1) {
        	return result;
        }
        else if(nList.size() == 1) {
        	node = nList.get(0);
        	result[0] = node.getLabel();
        }
        else if(nList.size() > 1) {
        	result[0] = "(" + nList.size() + " nodes have this IP)";
        }
        
        result[1] = node.getNodeId();
        result[2] = Integer.toString(nList.size());
        
        //"SELECT count(*) FROM outages WHERE ipaddr=? AND ifregainedservice IS NULL AND serviceid=(SELECT serviceid FROM service WHERE servicename=?)";
                   
        final org.opennms.core.criteria.Criteria crit = new org.opennms.core.criteria.Criteria(OnmsOutage.class)
        .setAliases(Arrays.asList(new Alias[] {
            new Alias("monitoredService","monitoredService", JoinType.LEFT_JOIN),
            new Alias("monitoredService.serviceType","serviceType", JoinType.LEFT_JOIN),
            new Alias("monitoredService.ipInterface","ipInterface", JoinType.LEFT_JOIN)
        }))
        .addRestriction(new NullRestriction("ifRegainedService"))
        .addRestriction(new EqRestriction("serviceType.name", criticalPathServiceName))
        .addRestriction(new EqRestriction("ipInterface.ipAddress",InetAddressUtils.addr(criticalPathIp)));
        	
        List<OnmsOutage> oList = outageDao.findMatching(crit);
        
        count = oList.size();

        if(count > 0) {
        	final org.opennms.core.criteria.Criteria tres = new org.opennms.core.criteria.Criteria(OnmsMonitoredService.class)
        	.setAliases(Arrays.asList(new Alias[] {
        			new Alias("serviceType","monitoredService.serviceType", JoinType.LEFT_JOIN)
                	}))
                	.addRestriction(new EqRestriction("status", StatusType.ACTIVE))
                	.addRestriction(new EqRestriction("serviceType.name", criticalPathServiceName))
                	.addRestriction(new EqRestriction("ipAddress",InetAddressUtils.addr(criticalPathIp)));
            	
        	List<OnmsMonitoredService> oms = monitoredServiceDao.findMatching(tres);

        	count = oms.size();
        	if(count > 0) {
        		result[3] = "Critical";
        	} else {
        		result[3] = "Normal";
        	}
        	for (OnmsMonitoredService open: oms) {
        		result[3] = open.getStatus();
        	}
        } else {
        	result[3] = "Cleared";
        }
        
        return result;
    }
    @Override
    public Set<Integer> getDependencyNodesByCriticalPath(String criticalPathip) {
       	Set<Integer> depNodes = new TreeSet<Integer>();
    	    	
    	final org.opennms.core.criteria.Criteria crit = new org.opennms.core.criteria.Criteria(OnmsPathOutage.class)
    	.addRestriction(new EqRestriction("criticalPathIp", criticalPathip))
        .setOrders(Arrays.asList(new Order[] {
        		Order.asc("nodeId")
        }));
    	
    	List<OnmsPathOutage> l = pathOutageDao.findMatching(crit);
       	for (OnmsPathOutage cur : l) {
       		List<OnmsIpInterface> iface = ipInterfaceDao.findByNodeId(cur.getNodeId());
       		for (OnmsIpInterface one : iface) {
       			if (one.getIsManaged() != "D") {
       				depNodes.add(cur.getNodeId());
       			}
       		}
    	}
    	
    	return depNodes;
    }
    
    @Override
    public Set<Integer> getDependencyNodesByNodeId(int nodeId) {
    	Set<Integer> depNodes = new TreeSet<Integer>();
    	
    	final org.opennms.core.criteria.Criteria crit = new org.opennms.core.criteria.Criteria(OnmsPathOutage.class) 
    	.addRestriction(new EqRestriction("nodeId", nodeId));
    	
    	List<OnmsPathOutage> l = pathOutageDao.findMatching(crit);
    	for (OnmsPathOutage cur: l) {
    		List<OnmsIpInterface> iface = ipInterfaceDao.findByNodeId(cur.getNodeId());
    		for (OnmsIpInterface one: iface) {
    			if (one.getIpAddress().equals(cur.getCriticalPathIp())) {
    				depNodes.add(cur.getNodeId());
    			}
    		}
    	}    	
    	
    	return depNodes;
    }
}

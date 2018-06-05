/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.element;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.svclayer.model.AggregateStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * The source for all network element business objects (nodes, interfaces,
 * services). Encapsulates all lookup functionality for the network element
 * business objects in one place.
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Transactional(readOnly=true)
public class NetworkElementFactory implements InitializingBean, NetworkElementFactoryInterface {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private MonitoredServiceDao m_monSvcDao;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Autowired
    private MonitoringSystemDao m_monitoringSystemDao;
    
	@Autowired
	private PlatformTransactionManager m_transactionManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    public static NetworkElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static NetworkElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(NetworkElementFactoryInterface.class);
    }

    private static final Comparator<Interface> INTERFACE_COMPARATOR = new InterfaceComparator();

    public static class InterfaceComparator implements Comparator<Interface> {
        @Override
        public int compare(Interface o1, Interface o2) {

            // Sort by IP first if the IPs are non-0.0.0.0
            if (!"0.0.0.0".equals(o1.getIpAddress()) && !"0.0.0.0".equals(o2.getIpAddress())) {
                return new InetAddressComparator().compare(InetAddressUtils.addr(o1.getIpAddress()), InetAddressUtils.addr(o2.getIpAddress()));
            } else {
                // Sort IPs that are non-0.0.0.0 so they are first
                if (!"0.0.0.0".equals(o1.getIpAddress())) {
                    return -1;
                } else if (!"0.0.0.0".equals(o2.getIpAddress())) {
                    return 1;
                }
            }
            return 0;
        }
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodeLabel(int)
	 */
    @Override
    public String getNodeLabel(int nodeId) {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.eq("id", nodeId);
        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        
        if(nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            return node.getLabel();
        } else {
            return null;
        }
    }

    @Override
    public String getNodeLocation(int nodeId) {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.eq("id", nodeId);
        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());

        if(nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            if (node.getLocation() != null) {
                return node.getLocation().getLocationName();
            }
        }

        return null;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getIpPrimaryAddress(int)
	 */
    @Override
    public String getIpPrimaryAddress(int nodeId) {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsIpInterface.class);
        cb.and(new EqRestriction("node.id", nodeId), new EqRestriction("isSnmpPrimary", PrimaryType.PRIMARY));
        
        final List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(cb.toCriteria());
        
        if(ifaces.size() > 0) {
            final OnmsIpInterface iface = ifaces.get(0);
            return InetAddressUtils.str(iface.getIpAddress());
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNode(int)
	 */
    @Override
    public OnmsNode getNode(int nodeId) {
        return m_nodeDao.get(nodeId);
    }

    /* (non-Javadoc)
     * @see org.opennms.web.element.NetworkElementFactoryInterface#getNode(string)
     */
    @Override
    public OnmsNode getNode(String  lookupCriteria) {
        return m_nodeDao.get(lookupCriteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllNodes()
	 */
    @Override
    public List<OnmsNode> getAllNodes() {
        OnmsCriteria criteria =  new OnmsCriteria(OnmsNode.class);
        criteria.add(Restrictions.or(Restrictions.isNull("type"), Restrictions.ne("type", "D")));
        criteria.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(criteria);
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesLike(java.lang.String)
	 */
    @Override
    public List<OnmsNode> getNodesLike(String nodeLabel) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("assetRecord", "assetRecord");
        criteria.add(Restrictions.and(Restrictions.ilike("label", nodeLabel, MatchMode.ANYWHERE), Restrictions.or(Restrictions.isNull("type"), Restrictions.ne("type", "D"))));
        criteria.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(criteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithIpLike(java.lang.String)
	 */
    @Override
    public List<OnmsNode> getNodesWithIpLike(String iplike) {
        if(iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        OnmsCriteria nodeCrit = new OnmsCriteria(OnmsNode.class, "node");
        nodeCrit.createCriteria("ipInterfaces", "iface")
            .add(OnmsRestrictions.ipLike(iplike))
            .add(Restrictions.ne("isManaged", "D"));
        nodeCrit.add(Restrictions.ne("type", "D"));
        nodeCrit.addOrder(Order.asc("label"));
        nodeCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        
        return m_nodeDao.findMatching(nodeCrit);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithService(int)
	 */
    @Override
    public List<OnmsNode> getNodesWithService(int serviceId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("assetRecord", "assetRecord");
        criteria.createAlias("ipInterfaces", "iface");
        criteria.createAlias("iface.monitoredServices", "svc");
        criteria.createAlias("svc.serviceType", "svcType").add(Restrictions.eq("svcType.id", serviceId));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        return m_nodeDao.findMatching(criteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithPhysAddr(java.lang.String)
	 */
    @Override
    public List<OnmsNode> getNodesWithPhysAddr(final String macAddr) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("assetRecord", "assetRecord");
        criteria.createAlias("snmpInterfaces", "snmpIfaces", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.ne("type", "D"));
        criteria.add(Restrictions.ilike("snmpIfaces.physAddr", macAddr.replaceAll("[:-]*", ""), MatchMode.ANYWHERE));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(criteria);
    }

    /**
     * @deprecated OnmsNode.arpInterface went away, so we only search snmpInterfaces
     * @param macAddr
     * @return a list of @{OnmsNode}s
     */
    @Override
    public List<OnmsNode> getNodesWithPhysAddrAtInterface(String macAddr) {
        return getNodesWithPhysAddr(macAddr);
    }

    /**
     * @deprecated OnmsNode.arpInterface went away, so we only search snmpInterfaces
     * @param macAddr
     * @return a list of @{OnmsNode}s
     */
    @Override
    public List<OnmsNode> getNodesWithPhysAddrFromSnmpInterface(String macAddr) {
        return getNodesWithPhysAddr(macAddr);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithIfAlias(java.lang.String)
	 */
    @Override
    public List<OnmsNode> getNodesWithIfAlias(String ifAlias) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("snmpInterfaces", "snmpIface");
        criteria.add(Restrictions.ne("type", "D"));
        criteria.add(Restrictions.ilike("snmpIface.ifAlias", ifAlias, MatchMode.ANYWHERE));
        criteria.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(criteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getHostname(java.lang.String)
	 */
    @Override
    public String getHostname(String ipAddress) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.add(Restrictions.eq("ipAddress", InetAddressUtils.addr(ipAddress)));
        criteria.add(Restrictions.isNotNull("ipHostName"));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List<OnmsIpInterface> ipIfaces = m_ipInterfaceDao.findMatching(criteria);
        
        if(ipIfaces.size() > 0) {
            OnmsIpInterface iface = ipIfaces.get(0);
            return iface.getIpHostName();
        }
        
        return null;
    }

    @Override
    public Integer getIfIndex(int ipinterfaceid) {
        return getIfIndex(m_ipInterfaceDao.get(ipinterfaceid));
    }
    
    @Override
    public Integer getIfIndex(int nodeID, String ipaddr) {
        return getIfIndex(m_ipInterfaceDao.get(m_nodeDao.get(nodeID), ipaddr));
    }

    private Integer getIfIndex(OnmsIpInterface ipinterface) {
        if (ipinterface != null && ipinterface.getIfIndex() != null)
            return ipinterface.getIfIndex();
        return -1;        
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getInterface(int)
	 */
    @Override
    public Interface getInterface(int ipInterfaceId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.add(Restrictions.eq("id", ipInterfaceId));
        criteria.setFetchMode("snmpInterface", FetchMode.JOIN);
        
        List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(criteria);
        
        if(ifaces.size() > 0) {
            return new Interface(ifaces.get(0));
        }
        
        return null;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getInterface(int, java.lang.String)
	 */
    @Override
    public Interface getInterface(int nodeId, String ipAddress) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ipAddress", InetAddressUtils.addr(ipAddress)));
        criteria.setFetchMode("snmpInterface", FetchMode.JOIN);
        
        List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(criteria);
        return ifaces.size() > 0 ? new Interface(ifaces.get(0)) : null;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getInterface(int, java.lang.String, int)
	 */
    @Override
    public Interface getInterface(int nodeId, String ipAddress, int ifIndex) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.createAlias("snmpInterface", "snmpIface");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ipAddress", InetAddressUtils.addr(ipAddress)));
        criteria.add(Restrictions.eq("snmpIface.ifIndex", ifIndex));

        List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findMatching(criteria);
        
        return ifaces.size() > 0 ? new Interface(ifaces.get(0)) : null;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getSnmpInterface(int, int)
	 */
    @Override
    public Interface getSnmpInterface(int nodeId, int ifIndex) {
        OnmsCriteria criteria  = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ifIndex", ifIndex));
        
        List<OnmsSnmpInterface> snmpIfaces = m_snmpInterfaceDao.findMatching(criteria);
        if(snmpIfaces.size() > 0) {
            return new Interface(snmpIfaces.get(0));
        }
        return null;
    }
    

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getInterfacesWithIpAddress(java.lang.String)
	 */
    @Override
    public Interface[] getInterfacesWithIpAddress(String ipAddress) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("ipAddress", InetAddressUtils.addr(ipAddress)));
        
        return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
    }


    

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getInterfacesWithIfAlias(int, java.lang.String)
	 */
    @Override
    public Interface[] getInterfacesWithIfAlias(int nodeId, String ifAlias) {
        
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.createAlias("snmpInterface", "snmpIface");
        criteria.createAlias("node.assetRecord", "assetRecord");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.ilike("snmpIface.ifAlias", ifAlias, MatchMode.ANYWHERE));
        criteria.add(Restrictions.ne("isManaged", "D"));
        
        return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllInterfacesOnNode(int)
	 */
    @Override
    public Interface[] getAllInterfacesOnNode(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.createAlias("snmpInterface", "snmpIface");
        criteria.createAlias("node.assetRecord", "assetRecord");
        criteria.add(Restrictions.eq("node.id", nodeId));
        
        return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllSnmpInterfacesOnNode(int)
	 */
    @Override
    public Interface[] getAllSnmpInterfacesOnNode(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.addOrder(Order.asc("ifIndex"));
        
        return onmsSnmpInterfaces2InterfaceArray(m_snmpInterfaceDao.findMatching(criteria));
    }

    private Interface[] onmsSnmpInterfaces2InterfaceArray(
            List<OnmsSnmpInterface> snmpIfaces) {
        List<Interface> intfs = new LinkedList<>();
        
        for(OnmsSnmpInterface snmpIface : snmpIfaces) {
            intfs.add(new Interface(snmpIface));
        }
        
        return intfs.toArray(new Interface[intfs.size()]);
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getActiveInterfacesOnNode(int)
	 */
    @Override
    public Interface[] getActiveInterfacesOnNode(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.ne("isManaged", "D"));
        
        return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
    }

    /*
     * Returns all interfaces, including their SNMP information
     */
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllInterfaces()
	 */
    @Override
    public Interface[] getAllInterfaces() {
        return getAllInterfaces(true);
    }

    /*
     * Returns all interfaces, but only includes SNMP data if includeSNMP is true
     * This may be useful for pages that don't need SNMP data and don't want to execute
     * a sub-query per interface!
     *
     * @param includeSNMP a boolean.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     */
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllInterfaces(boolean)
	 */
    @Override
    public Interface[] getAllInterfaces(boolean includeSnmp) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        if(!includeSnmp) {
            return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
        }else {
            return getInterfaceArrayWithSnmpData(m_ipInterfaceDao.findMatching(criteria));
        }
    }


    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllManagedIpInterfaces(boolean)
	 */
    @Override
    public Interface[] getAllManagedIpInterfaces(boolean includeSNMP) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.ne("isManaged", "D"));
        criteria.add(Restrictions.ne("ipAddress", InetAddressUtils.addr("0.0.0.0")));
        criteria.add(Restrictions.isNotNull("ipAddress"));
        criteria.addOrder(Order.asc("ipHostName"));
        criteria.addOrder(Order.asc("node.id"));
        criteria.addOrder(Order.asc("ipAddress"));
        
        if(!includeSNMP) {
            return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
        }else {
            return getInterfaceArrayWithSnmpData(m_ipInterfaceDao.findMatching(criteria));
        }
    }

    @Override
    public Interface[] getAllManagedIpInterfacesLike(String ipHost){
        OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.ne("isManaged", "D"));
        //criteria.add(Restrictions.ne("ipAddress", InetAddressUtils.addr("0.0.0.0")));
        criteria.add(Restrictions.or(Restrictions.ilike("ipHostName", ipHost, MatchMode.ANYWHERE), Restrictions.ilike("ipAddress", ipHost, MatchMode.ANYWHERE)));
        //criteria.add(Restrictions.isNotNull("ipAddress"));
        criteria.addOrder(Order.asc("ipHostName"));
        criteria.addOrder(Order.asc("node.id"));
        criteria.addOrder(Order.asc("ipAddress"));

        return getInterfaceArray(m_ipInterfaceDao.findMatching(criteria));
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getService(int, java.lang.String, int)
	 */
    @Override
    public Service getService(int nodeId, String ipAddress, int serviceId) {
        try {
            OnmsMonitoredService monSvc = m_monSvcDao.get(nodeId, InetAddress.getByName(ipAddress), serviceId);
            return monSvc == null ? null : new Service(monSvc);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid ip address '" + ipAddress + "'", e);
        }
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getService(int)
	 */
    @Override
    public Service getService(int ifServiceId) {
        OnmsMonitoredService monSvc = m_monSvcDao.get(ifServiceId);
        return monSvc == null ? null : new Service(monSvc);
    }
    

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllServices()
	 */
    @Override
    public Service[] getAllServices() {
        return getServiceArray(m_monSvcDao.findAll());
    }

    

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServicesOnInterface(int, java.lang.String)
	 */
    @Override
    public Service[] getServicesOnInterface(int nodeId, String ipAddress) {
        return getServicesOnInterface(nodeId, ipAddress, false);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServicesOnInterface(int, java.lang.String, boolean)
	 */
    @Override
    public Service[] getServicesOnInterface(int nodeId, String ipAddress, boolean includeDeletions) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class);
        criteria.createAlias("ipInterface", "ipInterface");
        criteria.createAlias("ipInterface.node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ipInterface.ipAddress", InetAddressUtils.addr(ipAddress)));
        
        if(!includeDeletions) {
            criteria.add(Restrictions.ne("status", "D"));
        }
        
        return getServiceArray(m_monSvcDao.findMatching(criteria));
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServicesOnNode(int)
	 */
    @Override
    public Service[] getServicesOnNode(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class);
        criteria.createAlias("ipInterface", "ipInterface");
        criteria.createAlias("ipInterface.snmpInterface", "snmpIface", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("ipInterface.node", "node");
        criteria.createAlias("serviceType", "serviceType");
        criteria.add(Restrictions.eq("node.id", nodeId));
        
        return getServiceArray(m_monSvcDao.findMatching(criteria));
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServicesOnNode(int, int)
	 */
    @Override
    public Service[] getServicesOnNode(int nodeId, int serviceId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class);
        criteria.createAlias("ipInterface", "ipInterface");
        criteria.createAlias("ipInterface.node", "node");
        criteria.createAlias("ipInterface.snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType");
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("serviceType.id", serviceId));
        
        return getServiceArray(m_monSvcDao.findMatching(criteria));
    }

    private static Service[] getServiceArray(List<OnmsMonitoredService> monSvcs) {
        List<Service> svcs = new LinkedList<>();
        for(OnmsMonitoredService monSvc : monSvcs) {
            Service service = new Service(monSvc);
            
            svcs.add(service);
        }
        
        
        return svcs.toArray(new Service[svcs.size()]);
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServiceNameFromId(int)
	 */
    @Override
    public String getServiceNameFromId(int serviceId) {
        OnmsServiceType type = m_serviceTypeDao.get(serviceId);
        return type == null ? null : type.getName();
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServiceIdFromName(java.lang.String)
	 */
    @Override
    public int getServiceIdFromName(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OnmsServiceType type = m_serviceTypeDao.findByName(serviceName);
        return type == null ? -1 : type.getId();
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServiceIdToNameMap()
	 */
    @Override
    public Map<Integer, String> getServiceIdToNameMap(){
        Map<Integer,String> serviceMap = new HashMap<Integer,String>();
        for (OnmsServiceType type : m_serviceTypeDao.findAll()) {
            serviceMap.put(type.getId(), type.getName());
        }
        return serviceMap;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getServiceNameToIdMap()
	 */
    @Override
    public Map<String, Integer> getServiceNameToIdMap(){
        Map<String,Integer> serviceMap = new HashMap<String,Integer>();
        for (OnmsServiceType type : m_serviceTypeDao.findAll()) {
            serviceMap.put(type.getName(), type.getId());
        }
        return serviceMap;
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesLikeAndIpLike(java.lang.String, java.lang.String, int)
	 */
    @Override
    public List<OnmsNode> getNodesLikeAndIpLike(String nodeLabel, String iplike, int serviceId) {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        OnmsCriteria nodeCrit = new OnmsCriteria(OnmsNode.class);
        nodeCrit.createAlias("assetRecord", "assetRecord");
        nodeCrit.add(Restrictions.ilike("label", nodeLabel));
        nodeCrit.createCriteria("ipInterfaces")
            .add(OnmsRestrictions.ipLike(iplike))
            .createAlias("monitoredServices", "monSvcs")
            .createAlias("monSvcs.serviceType", "serviceType")
            .add(Restrictions.eq("serviceType.id", serviceId));
        nodeCrit.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(nodeCrit);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesLike(java.lang.String, int)
	 */
    @Override
    public List<OnmsNode> getNodesLike(String nodeLabel, int serviceId) {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("assetRecord", "assetRecord");
        criteria.createAlias("ipInterfaces", "iface");
        criteria.createAlias("iface.monitoredServices", "monSvcs");
        criteria.createAlias("monSvcs.serviceType", "serviceType");
        criteria.add(Restrictions.ilike("label", nodeLabel, MatchMode.ANYWHERE));
        criteria.add(Restrictions.eq("serviceType.id", serviceId));
        criteria.add(Restrictions.ne("type", "D"));
        criteria.addOrder(Order.asc("label"));
        
        return m_nodeDao.findMatching(criteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithIpLike(java.lang.String, int)
	 */
    @Override
    public List<OnmsNode> getNodesWithIpLike(String iplike, int serviceId) {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OnmsCriteria nodeCrit = new OnmsCriteria(OnmsNode.class);
        nodeCrit.createAlias("assetRecord", "assetRecord");
        nodeCrit.createCriteria("ipInterfaces", "iface")
            .createAlias("monitoredServices", "monSvcs")
            .createAlias("monSvcs.serviceType", "serviceType")
            .add(OnmsRestrictions.ipLike(iplike))
            .add(Restrictions.eq("serviceType.id", serviceId));
        nodeCrit.add(Restrictions.ne("type", "D"));
        nodeCrit.addOrder(Order.asc("label"));
        nodeCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        
        return m_nodeDao.findMatching(nodeCrit);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getAllNodes(int)
	 */
    @Override
    public List<OnmsNode> getAllNodes(int serviceId) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("ipInterfaces", "ipInterfaces");
        criteria.createAlias("ipInterfaces.monitoredServices", "monSvcs");
        criteria.add(Restrictions.ne("type", "D"));
        criteria.add(Restrictions.eq("monSvcs.serviceType.id", serviceId));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        
        return m_nodeDao.findMatching(criteria);
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesFromPhysaddr(java.lang.String)
	 */
    @Override
    public List<OnmsNode> getNodesFromPhysaddr(final String physAddr) {
        if (physAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("snmpInterfaces", "iface")
            .ilike("iface.physAddr", physAddr.replaceAll("[:-]", ""))
            .ne("type", NodeType.DELETED)
            .distinct();
        return m_nodeDao.findMatching(builder.toCriteria());
    }

    /* (non-Javadoc)
	 * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodeIdsWithIpLike(java.lang.String)
	 */
    @Override
    public List<Integer> getNodeIdsWithIpLike(String iplike){
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        OnmsCriteria nodeCrit = new OnmsCriteria(OnmsNode.class);
        nodeCrit.createCriteria("ipInterfaces", "iface").add(OnmsRestrictions.ipLike(iplike));
        nodeCrit.add(Restrictions.ne("type", "D"));
        nodeCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List<Integer> nodeIds = new ArrayList<>();
        List<OnmsNode> nodes = m_nodeDao.findMatching(nodeCrit);
        for(OnmsNode node : nodes) {
            nodeIds.add(node.getId());
        }
        
        return nodeIds;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithCategories(java.lang.String[], boolean)
     */
    @Override
    public List<OnmsNode> getNodesWithCategories(String[] categories, boolean onlyNodesWithDownAggregateStatus) {
        List<OnmsNode> ourNodes = getNodesInCategories(categories);
        
        if(onlyNodesWithDownAggregateStatus) {
            AggregateStatus as = new AggregateStatus(new HashSet<OnmsNode>(ourNodes));
            ourNodes = as.getDownNodes();
        }
        return ourNodes;
    }

    
    private List<OnmsNode> getNodesInCategories(String[] categoryStrings){
        List<OnmsCategory> categories = new ArrayList<>();
        for(String categoryString : categoryStrings) {
            OnmsCategory category = m_categoryDao.findByName(categoryString);
            if(category != null) {
                categories.add(category);
            }else {
                throw new IllegalArgumentException("The Category " + categoryString + " does not exist");
            }
        }
        
        return m_nodeDao.findAllByCategoryList(categories);
    }

    /* (non-Javadoc)
     * @see org.opennms.web.element.NetworkElementFactoryInterface#getNodesWithCategories(java.lang.String[], java.lang.String[], boolean)
     */
    @Override
    public List<OnmsNode> getNodesWithCategories(String[] categories1, String[] categories2, boolean onlyNodesWithDownAggregateStatus) {
        ArrayList<OnmsCategory> c1 = new ArrayList<OnmsCategory>(categories1.length);
        for (String category : categories1) {
                c1.add(m_categoryDao.findByName(category));
        }
        ArrayList<OnmsCategory> c2 = new ArrayList<OnmsCategory>(categories2.length);
        for (String category : categories2) {
                c2.add(m_categoryDao.findByName(category));
        }
        
        List<OnmsNode> ourNodes1 = getNodesInCategories(categories1);
        List<OnmsNode> ourNodes2 = getNodesInCategories(categories2);
        
        Set<Integer> n2id = new HashSet<Integer>(ourNodes2.size());
        for (OnmsNode n2 : ourNodes2) {
            n2id.add(n2.getId()); 
        }

        List<OnmsNode> ourNodes = new ArrayList<>();
        for (OnmsNode n1 : ourNodes1) {
            if (n2id.contains(n1.getId())) {
                ourNodes.add(n1);
            }
        }
        
        if (onlyNodesWithDownAggregateStatus) {
            AggregateStatus as = new AggregateStatus(ourNodes);
            ourNodes = as.getDownNodes();
        }

        return ourNodes;
    }
        
    private Interface[] getInterfaceArray(List<OnmsIpInterface> ipIfaces) {
        List<Interface> intfs = new LinkedList<>();
        for(OnmsIpInterface iface : ipIfaces) {
            intfs.add(new Interface(iface));
        }
        
        Collections.sort(intfs, INTERFACE_COMPARATOR);
        return intfs.toArray(new Interface[intfs.size()]);
    }
    
    private Interface[] getInterfaceArrayWithSnmpData(List<OnmsIpInterface> ipIfaces) {
        List<Interface> intfs = new LinkedList<>();
        for(OnmsIpInterface iface : ipIfaces) {
            Interface intf = new Interface(iface);
            if(iface.getSnmpInterface() != null) {
                OnmsSnmpInterface snmpIface = iface.getSnmpInterface();
                intf.createSnmpInterface(snmpIface);
            }
            intfs.add(intf);
        }
        
        Collections.sort(intfs, INTERFACE_COMPARATOR);
        return intfs.toArray(new Interface[intfs.size()]);
    }

    public List<OnmsMonitoringLocation> getMonitoringLocations() {
        return m_monitoringLocationDao.findAll().stream().sorted((a,b) -> a.getLocationName().compareTo(b.getLocationName())).collect(Collectors.toList());
    }

    public List<OnmsMonitoringSystem> getMonitoringSystems() {
        return m_monitoringSystemDao.findAll().stream().sorted((a,b) -> a.getId().compareTo(b.getId())).collect(Collectors.toList());
    }
}

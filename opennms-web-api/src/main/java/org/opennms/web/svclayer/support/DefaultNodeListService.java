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

package org.opennms.web.svclayer.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SiteStatusViewConfigDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.svclayer.NodeListService;
import org.opennms.web.svclayer.model.AggregateStatus;
import org.opennms.web.svclayer.model.NodeListCommand;
import org.opennms.web.svclayer.model.NodeListModel;
import org.opennms.web.svclayer.model.NodeListModel.NodeModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * <p>DefaultNodeListService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:ayres@opennms.org">Bill Ayres</a>
 */
public class DefaultNodeListService implements NodeListService, InitializingBean {
    private static final Comparator<OnmsIpInterface> IP_INTERFACE_COMPARATOR = new IpInterfaceComparator();
    private static final Comparator<OnmsSnmpInterface> SNMP_INTERFACE_COMPARATOR = new SnmpInterfaceComparator();
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SiteStatusViewConfigDao m_siteStatusViewConfigDao;

    /** {@inheritDoc} */
    @Override
    public NodeListModel createNodeList(NodeListCommand command) {
        return createNodeList(command, true);
    }
    
    /** {@inheritDoc} */
    public NodeListModel createNodeList(NodeListCommand command, boolean sanitizeLabels) {
        Collection<OnmsNode> onmsNodes = null;
        
        /*
         * All search queries can be done solely with
         * criteria, so we build a common criteria object with common
         * restrictions and sort options.  Each specific search query
         * adds its own criteria restrictions (if any).
         * 
         * A set of booleans is maintained for aliases that might be
         * added in multiple places to ensure we don't add the same alias
         * multiple times.
         */
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class, "node");
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.ne("node.type", "D"));

        // Add additional criteria based on the command object
        addCriteriaForCommand(criteria, command);
            
        criteria.addOrder(Order.asc("node.label"));
        onmsNodes = m_nodeDao.findMatching(criteria);

        if (command.getNodesWithDownAggregateStatus()) {
            AggregateStatus as = new AggregateStatus(onmsNodes);
            onmsNodes = as.getDownNodes();
        }
        
        if (sanitizeLabels) {
            for (OnmsNode node : onmsNodes) {
                node.setLabel(WebSecurityUtils.sanitizeString(node.getLabel()));
            }
        }
        
        return createModelForNodes(command, onmsNodes);
    }

    private void addCriteriaForCommand(OnmsCriteria criteria, NodeListCommand command) {
        if (command.hasNodename()) {
            addCriteriaForNodename(criteria, command.getNodename());
        } else if (command.hasNodeId()) {
            addCriteriaForNodeId(criteria, command.getNodeId());
        } else if (command.hasIplike()) {
            addCriteriaForIpLike(criteria, command.getIplike());
        } else if (command.hasService()) {
            addCriteriaForService(criteria, command.getService());
        } else if (command.hasMaclike()) {
            addCriteriaForMaclike(criteria, command.getMaclike());
        } else if (command.hasMib2Parm() &&command.hasMib2ParmValue() && command.hasMib2ParmMatchType()) {
            addCriteriaForMib2Parm(criteria, command.getMib2Parm(), command.getMib2ParmValue(), command.getMib2ParmMatchType());
        } else if (command.hasSnmpParm() &&command.hasSnmpParmValue() && command.hasSnmpParmMatchType()) {
            addCriteriaForSnmpParm(criteria, command.getSnmpParm(), command.getSnmpParmValue(), command.getSnmpParmMatchType());
        } else if (command.hasCategory1() && command.hasCategory2()) {
            addCriteriaForCategories(criteria, command.getCategory1(), command.getCategory2());
        } else if (command.hasCategory1()) {
            addCriteriaForCategories(criteria, command.getCategory1());
        } else if (command.hasStatusViewName() && command.hasStatusSite() && command.hasStatusRowLabel()) {
            addCriteriaForSiteStatusView(criteria, command.getStatusViewName(), command.getStatusSite(), command.getStatusRowLabel());
        } else if(command.hasForeignSource()) {
            addCriteriaForForeignSource(criteria, command.getForeignSource());
        } else if(command.hasMonitoringLocation()) {
            addCriteriaForMonitoringLocation(criteria, command.getMonitoringLocation());
        } else {
            // Do nothing.... don't add any restrictions other than the default ones
        }

        if (command.getNodesWithOutages()) {
            addCriteriaForCurrentOutages(criteria);
        }
    }

    private void addCriteriaForMonitoringLocation(OnmsCriteria criteria, String monitoringLocation) {
        criteria.add(Restrictions.eq("node.location.locationName", monitoringLocation));
    }

    private static void addCriteriaForMib2Parm(OnmsCriteria criteria, String mib2Parm, String mib2ParmValue, String mib2ParmMatchType) {
        // All of the MIB-II system attributes are in the node table
        if(mib2ParmMatchType.equals("contains")) {
            criteria.add(Restrictions.ilike("node.".concat(mib2Parm), mib2ParmValue, MatchMode.ANYWHERE));
        } else if(mib2ParmMatchType.equals("equals")) {
            criteria.add(Restrictions.eq("node.".concat(mib2Parm), mib2ParmValue));
        }
    }

    private static void addCriteriaForSnmpParm(OnmsCriteria criteria,
            String snmpParm, String snmpParmValue, String snmpParmMatchType) {
        criteria.createAlias("node.ipInterfaces", "ipInterface");
        criteria.add(Restrictions.ne("ipInterface.isManaged", "D"));

        criteria.createAlias("node.snmpInterfaces", "snmpInterface");
        criteria.add(Restrictions.ne("snmpInterface.collect", "D"));
        if(snmpParmMatchType.equals("contains")) {
            criteria.add(Restrictions.ilike("snmpInterface.".concat(snmpParm), snmpParmValue, MatchMode.ANYWHERE));
        } else if(snmpParmMatchType.equals("equals")) {
            snmpParmValue = snmpParmValue.toLowerCase();
            criteria.add(Restrictions.sqlRestriction("{alias}.nodeid in (select nodeid from snmpinterface where snmpcollect != 'D' and lower(snmp" + snmpParm + ") = '" + snmpParmValue + "')"));
        }
    }

    private static void addCriteriaForCurrentOutages(OnmsCriteria criteria) {
        /*
         * This doesn't work properly if ipInterfaces and/or
         * monitoredServices have other restrictions.  If we are
         * matching on service ID = 7, but service ID = 1 has an
         * outage, the node won't show up.
         */
        /*
        criteria.createAlias("ipInterfaces", "ipInterface");
        criteria.createAlias("ipInterfaces.monitoredServices", "monitoredService");
        criteria.createAlias("ipInterfaces.monitoredServices.currentOutages", "currentOutages");
        criteria.add(Restrictions.isNull("currentOutages.ifRegainedService"));
        criteria.add(Restrictions.or(Restrictions.isNull("currentOutages.suppressTime"), Restrictions.lt("currentOutages.suppressTime", new Date())));
        */
        
        // This SQL restriction does work fine, however 
        criteria.add(Restrictions.sqlRestriction("{alias}.nodeId in (select ip.nodeId from outages o, ifservices if, ipinterface ip where if.id = o.ifserviceid and ip.id = if.ipinterfaceid and o.ifregainedservice is null and o.suppresstime is null or o.suppresstime < now())"));
    }

    private static void addCriteriaForNodename(OnmsCriteria criteria, String nodeName) {
        criteria.add(Restrictions.ilike("node.label", nodeName, MatchMode.ANYWHERE));
    }
    
    private static void addCriteriaForNodeId(OnmsCriteria criteria, String nodeIdString) {
        int nodeId = parseNodeId(nodeIdString);
        criteria.add(Restrictions.idEq(nodeId));
    }

    private static int parseNodeId(String nodeIdString) {
        try {
            return Integer.parseInt(nodeIdString);
        } catch (NumberFormatException nfe) {
            LoggerFactory.getLogger(DefaultNodeListService.class).warn("{} is not a valid node id, falling back to -1.", nodeIdString);
            return -1;
        }
    }
    
    private static void addCriteriaForForeignSource(OnmsCriteria criteria, String foreignSource) {
        criteria.add(Restrictions.ilike("node.foreignSource", foreignSource, MatchMode.ANYWHERE));
    }

    private static void addCriteriaForIpLike(OnmsCriteria criteria, String iplike) {
        OnmsCriteria ipInterface = criteria.createCriteria("node.ipInterfaces", "ipInterface");
        ipInterface.add(Restrictions.ne("isManaged", "D"));
        
        ipInterface.add(OnmsRestrictions.ipLike(iplike));
    }
    

    private static void addCriteriaForService(OnmsCriteria criteria, int serviceId) {
        criteria.createAlias("node.ipInterfaces", "ipInterface");
        criteria.add(Restrictions.ne("ipInterface.isManaged", "D"));

        criteria.createAlias("node.ipInterfaces.monitoredServices", "monitoredService");
        criteria.createAlias("node.ipInterfaces.monitoredServices.serviceType", "serviceType");
        criteria.add(Restrictions.eq("serviceType.id", serviceId));
        criteria.add(Restrictions.ne("monitoredService.status", "D"));
    }

    private static void addCriteriaForMaclike(final OnmsCriteria criteria, final String macLike) {
        final String macLikeStripped = macLike.replaceAll("[:-]", "");
        
        criteria.createAlias("node.snmpInterfaces", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        final Disjunction physAddrDisjunction = Restrictions.disjunction();
        physAddrDisjunction.add(Restrictions.ilike("snmpInterface.physAddr", macLikeStripped, MatchMode.ANYWHERE));
        criteria.add(physAddrDisjunction);
  
        // This is an alternative to the above code if we need to use the out-of-the-box DetachedCriteria which doesn't let us specify the join type 
        /*
        String propertyName = "nodeId";
        String value = "%" + macLikeStripped + "%";
        
        Disjunction physAddrDisjuction = Restrictions.disjunction();
        physAddrDisjuction.add(Restrictions.sqlRestriction("{alias}." + propertyName + " IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE ? )", value, new StringType()));
        criteria.add(physAddrDisjuction);
        */
    }


    // This doesn't work because we can only join a specific table once with criteria
    /*
    public void addCriteriaForCategories(OnmsCriteria criteria, String[] ... categories) {
        Assert.notNull(categories, "categories argument must not be null");
        Assert.isTrue(categories.length >= 1, "categories must have at least one set of categories");

        for (String[] categoryStrings : categories) {
            for (String categoryString : categoryStrings) {
                OnmsCategory category = m_categoryDao.findByName(categoryString);
                if (category == null) {
                    throw new IllegalArgumentException("Could not find category for name '" + categoryString + "'");
                }
            }
        }
        
        int categoryCount = 0;
        for (String[] categoryStrings : categories) {
            OnmsCriteria categoriesCriteria = criteria.createCriteria("categories", "category" + categoryCount++);
            categoriesCriteria.add(Restrictions.in("name", categoryStrings));
        }
    }
    */
    
    private void addCriteriaForCategories(OnmsCriteria criteria, String[]... categories) {
        Assert.notNull(criteria, "criteria argument must not be null");
        
        for (Criterion criterion : m_categoryDao.getCriterionForCategorySetsUnion(categories)) {
            criteria.add(criterion);
        }
    }
        
    private void addCriteriaForSiteStatusView(OnmsCriteria criteria, String statusViewName, String statusSite, String rowLabel) {
        View view = m_siteStatusViewConfigDao.getView(statusViewName);
        RowDef rowDef = getRowDef(view, rowLabel);
        Set<String> categoryNames = getCategoryNamesForRowDef(rowDef);
        
        addCriteriaForCategories(criteria, categoryNames.toArray(new String[categoryNames.size()]));
        
        String sql = "{alias}.nodeId in (select nodeId from assets where " + view.getColumnName() + " = ?)";
        criteria.add(Restrictions.sqlRestriction(sql, statusSite, new StringType()));
    }
    

    private static RowDef getRowDef(final View view, final String rowLabel) {
        for (final RowDef rowDef : view.getRows()) {
            if (rowDef.getLabel().equals(rowLabel)) {
                return rowDef;
            }
        }
        
        throw new DataRetrievalFailureException("Unable to locate row: "+rowLabel+" for status view: "+view.getName());
    }
    
    private static Set<String> getCategoryNamesForRowDef(RowDef rowDef) {
        Set<String> categories = new LinkedHashSet<>();
        
        List<Category> cats = rowDef.getCategories();
        for (Category cat : cats) {
            categories.add(cat.getName());
        }
        return categories;
    }

    private static NodeListModel createModelForNodes(NodeListCommand command, Collection<OnmsNode> onmsNodes) {
        int interfaceCount = 0;
        List<NodeModel> displayNodes = new LinkedList<>();
        for (OnmsNode node : onmsNodes) {
            List<OnmsIpInterface> displayInterfaces = new LinkedList<>();
            List<OnmsSnmpInterface> displaySnmpInterfaces = new LinkedList<>();
            if (command.getListInterfaces()) {
                if (command.hasSnmpParm() && command.getSnmpParmMatchType().equals("contains")) {
                    String parmValueMatchString = (".*" + command.getSnmpParmValue().toLowerCase().replaceAll("([\\W])", "\\\\$0").replaceAll("\\\\%", ".*").replaceAll("_", ".") + ".*");
                    if (command.getSnmpParm().equals("ifAlias")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) && snmpIntf.getIfAlias() != null && snmpIntf.getIfAlias().toLowerCase().matches(parmValueMatchString)) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifName")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) &&snmpIntf.getIfName() != null && snmpIntf.getIfName().toLowerCase().matches(parmValueMatchString)) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifDescr")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) &&snmpIntf.getIfDescr() != null && snmpIntf.getIfDescr().toLowerCase().matches(parmValueMatchString)) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    }
                } else if (command.hasSnmpParm() && command.getSnmpParmMatchType().equals("equals")) {
                    if (command.getSnmpParm().equals("ifAlias")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) && snmpIntf.getIfAlias() != null && snmpIntf.getIfAlias().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifName")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) &&snmpIntf.getIfName() != null && snmpIntf.getIfName().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifDescr")) {
                        for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                            if (snmpIntf != null && !"D".equals(snmpIntf.getCollect()) &&snmpIntf.getIfDescr() != null && snmpIntf.getIfDescr().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displaySnmpInterfaces.add(snmpIntf);
                            }
                        }
                    }
                } else if (command.hasMaclike()) {
                	String macLikeStripped = command.getMaclike().toLowerCase().replaceAll("[:-]", "");
                	for (OnmsSnmpInterface snmpIntf : node.getSnmpInterfaces()) {
                	    if (snmpIntf.getPhysAddr() != null && !"D".equals(snmpIntf.getCollect()) && snmpIntf.getPhysAddr().toLowerCase().contains(macLikeStripped)) {
                	        displaySnmpInterfaces.add(snmpIntf);
                	    }
                	}
                } else {
                    for (OnmsIpInterface intf : node.getIpInterfaces()) {
                        if (!"D".equals(intf.getIsManaged()) && !"0.0.0.0".equals(InetAddressUtils.str(intf.getIpAddress()))) {
                        	displayInterfaces.add(intf);
                        }
                    }
                }
            }
            
            Collections.sort(displayInterfaces, IP_INTERFACE_COMPARATOR);
            Collections.sort(displaySnmpInterfaces, SNMP_INTERFACE_COMPARATOR);

            displayNodes.add(new NodeListModel.NodeModel(node, displayInterfaces, displaySnmpInterfaces));
            interfaceCount += displayInterfaces.size();
            interfaceCount += displaySnmpInterfaces.size();
        }

        return new NodeListModel(displayNodes, interfaceCount);
    }
    
    

    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getSiteStatusViewConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.SiteStatusViewConfigDao} object.
     */
    public SiteStatusViewConfigDao getSiteStatusViewConfigDao() {
        return m_siteStatusViewConfigDao;
    }

    /**
     * <p>setSiteStatusViewConfigDao</p>
     *
     * @param siteStatusViewConfigDao a {@link org.opennms.netmgt.dao.api.SiteStatusViewConfigDao} object.
     */
    public void setSiteStatusViewConfigDao(SiteStatusViewConfigDao siteStatusViewConfigDao) {
        m_siteStatusViewConfigDao = siteStatusViewConfigDao;
    }


    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_nodeDao != null, "nodeDao property cannot be null");
        Assert.state(m_categoryDao != null, "categoryDao property cannot be null");
        Assert.state(m_siteStatusViewConfigDao != null, "siteStatusViewConfigDao property cannot be null");
    }
    
    public static class IpInterfaceComparator implements Comparator<OnmsIpInterface>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1538654897829381114L;

        @Override
        public int compare(final OnmsIpInterface o1, final OnmsIpInterface o2) {
            int diff;

            // Sort by IP first if the IPs are non-0.0.0.0
            final String o1ip = InetAddressUtils.str(o1.getIpAddress());
			final String o2ip = InetAddressUtils.str(o2.getIpAddress());
			if (!"0.0.0.0".equals(o1ip) && !"0.0.0.0".equals(o2ip)) {
                return new ByteArrayComparator().compare(InetAddressUtils.toIpAddrBytes(o1ip), InetAddressUtils.toIpAddrBytes(o2ip));
            } else {
                // Sort IPs that are non-0.0.0.0 so they are first
                if (!"0.0.0.0".equals(o1ip)) {
                    return -1;
                } else if (!"0.0.0.0".equals(o2ip)) {
                    return 1;
                }
            }
            
            // If we don't have an SNMP interface for both, compare by ID
            if (o1.getSnmpInterface() == null || o2.getSnmpInterface() == null) {
                // List interfaces without SNMP interface first
                if (o1.getSnmpInterface() != null) {
                    return -1;
                } else {
                    return o1.getId().compareTo(o2.getId());
                }
            }

            // Sort by ifName
            if (o1.getSnmpInterface().getIfName() == null || o2.getSnmpInterface().getIfName() == null) {
                if (o1.getSnmpInterface().getIfName() != null) {
                    return -1;
                } else if (o2.getSnmpInterface().getIfName() != null) {
                    return 1;
                }
            } else if ((diff = o1.getSnmpInterface().getIfName().compareTo(o2.getSnmpInterface().getIfName())) != 0) {
                return diff;
            }
            
            // Sort by ifDescr
            if (o1.getSnmpInterface().getIfDescr() == null || o2.getSnmpInterface().getIfDescr() == null) {
                if (o1.getSnmpInterface().getIfDescr() != null) {
                    return -1;
                } else if (o2.getSnmpInterface().getIfDescr() != null) {
                    return 1;
                }
            } else if ((diff = o1.getSnmpInterface().getIfDescr().compareTo(o2.getSnmpInterface().getIfDescr())) != 0) {
                return diff;
            }
            
            // Sort by ifIndex
            if ((diff = o1.getSnmpInterface().getIfIndex().compareTo(o2.getSnmpInterface().getIfIndex())) != 0) {
                return diff;
            }

            // Fallback to id
            return o1.getId().compareTo(o2.getId());
        }
    }
    /**
     * This class implements a comparator for OnmsSnmpInterfaces. (For comparing non-ip interfaces).
     *
     */
    public static class SnmpInterfaceComparator implements Comparator<OnmsSnmpInterface>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 3751865611949289845L;

        @Override
        public int compare(OnmsSnmpInterface o1, OnmsSnmpInterface o2) {
            int diff;
            
            // Sort by ifName
            if (o1.getIfName() == null || o2.getIfName() == null) {
                if (o1.getIfName() != null) {
                    return -1;
                } else if (o2.getIfName() != null) {
                    return 1;
                }
            } else if ((diff = o1.getIfName().compareTo(o2.getIfName())) != 0) {
                return diff;
            }
            
            // Sort by ifDescr
            if (o1.getIfDescr() == null || o2.getIfDescr() == null) {
                if (o1.getIfDescr() != null) {
                    return -1;
                } else if (o2.getIfDescr() != null) {
                    return 1;
                }
            } else if ((diff = o1.getIfDescr().compareTo(o2.getIfDescr())) != 0) {
                return diff;
            }
            
            // Sort by ifIndex
            if ((diff = o1.getIfIndex().compareTo(o2.getIfIndex())) != 0) {
                return diff;
            }

            // Fallback to id
            return o1.getId().compareTo(o2.getId());
        }
    }
}

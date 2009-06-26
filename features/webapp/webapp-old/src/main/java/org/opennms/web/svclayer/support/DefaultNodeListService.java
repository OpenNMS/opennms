/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 12, 2007
 *
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
import org.opennms.core.utils.IPSorter;
import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.Rows;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsArpInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.opennms.web.command.NodeListCommand;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.NodeListService;
import org.opennms.web.svclayer.dao.SiteStatusViewConfigDao;
import org.opennms.web.svclayer.support.NodeListModel.NodeModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:ayres@opennms.org">Bill Ayres</a>
 */
public class DefaultNodeListService implements NodeListService, InitializingBean {
    private static final Comparator<OnmsIpInterface> IP_INTERFACE_COMPARATOR = new IpInterfaceComparator();
    private static final Comparator<OnmsArpInterface> ARP_INTERFACE_COMPARATOR = new ArpInterfaceComparator();
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SiteStatusViewConfigDao m_siteStatusViewConfigDao;

    public NodeListModel createNodeList(NodeListCommand command) {
        Collection<OnmsNode> onmsNodes = null;
        
        /*
         * All search queries can be done solely with
         * criteria, so we build a common criteria object with common
         * restrictions and sort options.  Each specific search query
         * adds its own crtieria restrictions (if any).
         * 
         * A set of booleans is maintained for aliases that might be
         * added in muliple places to ensure we don't add the same alias
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
        
        return createModelForNodes(command, onmsNodes);
    }

    private void addCriteriaForCommand(OnmsCriteria criteria, NodeListCommand command) {
        if (command.hasNodename()) {
            addCriteriaForNodename(criteria, command.getNodename());
        } else if (command.hasIplike()) {
            addCriteriaForIpLike(criteria, command.getIplike());
        } else if (command.hasService()) {
            addCriteriaForService(criteria, command.getService());
        } else if (command.hasMaclike()) {
            addCriteriaForMaclike(criteria, command.getMaclike());
        } else if (command.hasSnmpParm() &&command.hasSnmpParmValue() && command.hasSnmpParmMatchType()) {
            addCriteriaForSnmpParm(criteria, command.getSnmpParm(), command.getSnmpParmValue(), command.getSnmpParmMatchType());
        } else if (command.hasCategory1() && command.hasCategory2()) {
            addCriteriaForCategories(criteria, command.getCategory1(), command.getCategory2());
        } else if (command.hasCategory1()) {
            addCriteriaForCategories(criteria, command.getCategory1());
        } else if (command.hasStatusViewName() && command.hasStatusSite() && command.hasStatusRowLabel()) {
            addCriteriaForSiteStatusView(criteria, command.getStatusViewName(), command.getStatusSite(), command.getStatusRowLabel());
        } else {
            // Do nothing.... don't add any restrictions other than the default ones
        }

        if (command.getNodesWithOutages()) {
            addCriteriaForCurrentOutages(criteria);
        }
    }

    private void addCriteriaForSnmpParm(OnmsCriteria criteria,
            String snmpParm, String snmpParmValue, String snmpParmMatchType) {
        criteria.createAlias("node.ipInterfaces", "ipInterface");
        criteria.add(Restrictions.ne("ipInterface.isManaged", "D"));

        criteria.createAlias("node.ipInterfaces.snmpInterface", "snmpInterface");
        if(snmpParmMatchType.equals("contains")) {
            criteria.add(Restrictions.ilike("snmpInterface.".concat(snmpParm), snmpParmValue, MatchMode.ANYWHERE));
        } else if(snmpParmMatchType.equals("equals")) {
            snmpParmValue = snmpParmValue.toLowerCase();
            criteria.add(Restrictions.sqlRestriction("{alias}.nodeid in (select nodeid from snmpinterface where lower(snmp" + snmpParm + ") = '" + snmpParmValue + "')"));
        }
    }

    private void addCriteriaForCurrentOutages(OnmsCriteria criteria) {
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
        criteria.add(Restrictions.sqlRestriction("{alias}.nodeId in (select o.nodeId from outages o where o.ifregainedservice is null and o.suppresstime is null or o.suppresstime < now())"));
    }

    private void addCriteriaForNodename(OnmsCriteria criteria, String nodeName) {
        criteria.add(Restrictions.ilike("node.label", nodeName, MatchMode.ANYWHERE));
    }

    private void addCriteriaForIpLike(OnmsCriteria criteria, String iplike) {
        OnmsCriteria ipInterface = criteria.createCriteria("node.ipInterfaces", "ipInterface");
        ipInterface.add(Restrictions.ne("isManaged", "D"));
        
        ipInterface.add(OnmsRestrictions.ipLike(iplike));
    }

    private void addCriteriaForService(OnmsCriteria criteria, int serviceId) {
        criteria.createAlias("node.ipInterfaces", "ipInterface");
        criteria.add(Restrictions.ne("ipInterface.isManaged", "D"));

        criteria.createAlias("node.ipInterfaces.monitoredServices", "monitoredService");
        criteria.createAlias("node.ipInterfaces.monitoredServices.serviceType", "serviceType");
        criteria.add(Restrictions.eq("serviceType.id", serviceId));
        criteria.add(Restrictions.ne("monitoredService.status", "D"));
    }

    private void addCriteriaForMaclike(OnmsCriteria criteria, String macLike) {
        String macLikeStripped = macLike.replaceAll("[:-]", "");
        
        criteria.createAlias("node.snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.arpInterfaces", "arpInterface", CriteriaSpecification.LEFT_JOIN);
        Disjunction physAddrDisjunction = Restrictions.disjunction();
        physAddrDisjunction.add(Restrictions.ilike("snmpInterface.physAddr", macLikeStripped, MatchMode.ANYWHERE));
        physAddrDisjunction.add(Restrictions.ilike("arpInterface.physAddr", macLikeStripped, MatchMode.ANYWHERE));
        criteria.add(physAddrDisjunction);
  
        // This is an alternative to the above code if we need to use the out-of-the-box DetachedCriteria which doesn't let us specify the join type 
        /*
        String propertyName = "nodeId";
        String value = "%" + macLikeStripped + "%";
        
        Disjunction physAddrDisjuction = Restrictions.disjunction();
        physAddrDisjuction.add(Restrictions.sqlRestriction("{alias}." + propertyName + " IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE ? )", value, new StringType()));
        physAddrDisjuction.add(Restrictions.sqlRestriction("{alias}." + propertyName + " IN (SELECT nodeid FROM atinterface WHERE atphysaddr LIKE ? )", value, new StringType()));
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
    

    private RowDef getRowDef(View view, String rowLabel) {
        Rows rows = view.getRows();
        Collection<RowDef> rowDefs = rows.getRowDefCollection();
        for (RowDef rowDef : rowDefs) {
            if (rowDef.getLabel().equals(rowLabel)) {
                return rowDef;
            }
        }
        
        throw new DataRetrievalFailureException("Unable to locate row: "+rowLabel+" for status view: "+view.getName());
    }
    
    private Set<String> getCategoryNamesForRowDef(RowDef rowDef) {
        Set<String> categories = new LinkedHashSet<String>();
        
        List<Category> cats = rowDef.getCategoryCollection();
        for (Category cat : cats) {
            categories.add(cat.getName());
        }
        return categories;
    }

    private NodeListModel createModelForNodes(NodeListCommand command, Collection<OnmsNode> onmsNodes) {
        int interfaceCount = 0;
        List<NodeModel> displayNodes = new LinkedList<NodeModel>();
        for (OnmsNode node : onmsNodes) {
            List<OnmsIpInterface> displayInterfaces = new LinkedList<OnmsIpInterface>();
            List<OnmsArpInterface> displayArpInterfaces = new LinkedList<OnmsArpInterface>();
            if (command.getListInterfaces()) {
                if (command.hasSnmpParm() && command.getSnmpParmMatchType().equals("contains")) {
                    String parmValueMatchString = (".*" + command.getSnmpParmValue().toLowerCase().replaceAll("([\\W])", "\\\\$0").replaceAll("\\\\%", ".*").replaceAll("_", ".") + ".*");
                    if (command.getSnmpParm().equals("ifAlias")) { 
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null && intf.getSnmpInterface().getIfAlias() != null && intf.getSnmpInterface().getIfAlias().toLowerCase().matches(parmValueMatchString)) {
                                displayInterfaces.add(intf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifName")) {
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null &&intf.getSnmpInterface().getIfName() != null && intf.getSnmpInterface().getIfName().toLowerCase().matches(parmValueMatchString)) {
                                displayInterfaces.add(intf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifDescr")) {
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null &&intf.getSnmpInterface().getIfDescr() != null && intf.getSnmpInterface().getIfDescr().toLowerCase().matches(parmValueMatchString)) {
                                displayInterfaces.add(intf);
                            }
                        }
                    }
                } else if (command.hasSnmpParm() && command.getSnmpParmMatchType().equals("equals")) {
                    if (command.getSnmpParm().equals("ifAlias")) {
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null && intf.getSnmpInterface().getIfAlias() != null && intf.getSnmpInterface().getIfAlias().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displayInterfaces.add(intf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifName")) {
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null &&intf.getSnmpInterface().getIfName() != null && intf.getSnmpInterface().getIfName().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displayInterfaces.add(intf);
                            }
                        }
                    } else if (command.getSnmpParm().equals("ifDescr")) {
                        for (OnmsIpInterface intf : node.getIpInterfaces()) {
                            if (intf.getSnmpInterface() != null &&intf.getSnmpInterface().getIfDescr() != null && intf.getSnmpInterface().getIfDescr().equalsIgnoreCase(command.getSnmpParmValue())) {
                                displayInterfaces.add(intf);
                            }
                        }
                    }
                } else if (command.hasMaclike()) {
                	String macLikeStripped = command.getMaclike().toLowerCase().replaceAll("[:-]", "");
                	for (OnmsIpInterface intf : node.getIpInterfaces()) {
                		if (intf.getSnmpInterface() != null &&intf.getSnmpInterface().getPhysAddr() != null && intf.getSnmpInterface().getPhysAddr().toLowerCase().contains(macLikeStripped)) {
                			displayInterfaces.add(intf);
                		}
                	}
                	for (OnmsArpInterface aint : node.getArpInterfaces()) {
                		if (aint.getPhysAddr() != null && aint.getPhysAddr().toLowerCase().contains(macLikeStripped)) {
                			OnmsIpInterface intf = node.getIpInterfaceByIpAddress(aint.getIpAddress());
                			if ((intf == null || intf.getSnmpInterface() == null || intf.getSnmpInterface().getPhysAddr() == null || !intf.getSnmpInterface().getPhysAddr().equalsIgnoreCase(aint.getPhysAddr()))) {
                				displayArpInterfaces.add(aint);
                			}
                		}
                	}
                } else {
                    for (OnmsIpInterface intf : node.getIpInterfaces()) {
                        if (!"D".equals(intf.getIsManaged()) && !"0.0.0.0".equals(intf.getIpAddress())) {
                            displayInterfaces.add(intf);
                        }
                    }
                }
            }
            
            Collections.sort(displayInterfaces, IP_INTERFACE_COMPARATOR);
            Collections.sort(displayArpInterfaces, ARP_INTERFACE_COMPARATOR);           

            displayNodes.add(new NodeListModel.NodeModel(node, displayInterfaces, displayArpInterfaces));
            interfaceCount += displayInterfaces.size();
            interfaceCount += displayArpInterfaces.size();
        }

        return new NodeListModel(displayNodes, interfaceCount);
    }
    
    

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public SiteStatusViewConfigDao getSiteStatusViewConfigDao() {
        return m_siteStatusViewConfigDao;
    }

    public void setSiteStatusViewConfigDao(SiteStatusViewConfigDao siteStatusViewConfigDao) {
        m_siteStatusViewConfigDao = siteStatusViewConfigDao;
    }


    public void afterPropertiesSet() throws Exception {
        Assert.state(m_nodeDao != null, "nodeDao property cannot be null");
        Assert.state(m_categoryDao != null, "categoryDao property cannot be null");
        Assert.state(m_siteStatusViewConfigDao != null, "siteStatusViewConfigDao property cannot be null");
    }
    
    public static class IpInterfaceComparator implements Comparator<OnmsIpInterface>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(OnmsIpInterface o1, OnmsIpInterface o2) {
            int diff;

            // Sort by IP first if the IPs are non-0.0.0.0
            if (!"0.0.0.0".equals(o1.getIpAddress()) && !"0.0.0.0".equals(o2.getIpAddress())) {
                if (IPSorter.convertToLong(o1.getIpAddress()) > IPSorter.convertToLong(o2.getIpAddress())) {
                    return 1;
                } else if (IPSorter.convertToLong(o1.getIpAddress()) < IPSorter.convertToLong(o2.getIpAddress())) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                // Sort IPs that are non-0.0.0.0 so they are first
                if (!"0.0.0.0".equals(o1.getIpAddress())) {
                    return -1;
                } else if (!"0.0.0.0".equals(o2.getIpAddress())) {
                    return 1;
                }
            }
            
            // If we don't have an SNMP interface for both, compare by ID
            if (o1.getSnmpInterface() == null || o2.getSnmpInterface() == null) {
                // List interfaces without snmp interface first
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

    public static class ArpInterfaceComparator implements Comparator<OnmsArpInterface>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(OnmsArpInterface o1, OnmsArpInterface o2) {
            int diff;

            // Sort by IP first if the IPs are non-0.0.0.0
            if (!"0.0.0.0".equals(o1.getIpAddress()) && !"0.0.0.0".equals(o2.getIpAddress())) {
                if (IPSorter.convertToLong(o1.getIpAddress()) > IPSorter.convertToLong(o2.getIpAddress())) {
                    return 1;
                } else if (IPSorter.convertToLong(o1.getIpAddress()) < IPSorter.convertToLong(o2.getIpAddress())) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                // Sort IPs that are non-0.0.0.0 so they are first
                if (!"0.0.0.0".equals(o1.getIpAddress())) {
                    return -1;
                } else if (!"0.0.0.0".equals(o2.getIpAddress())) {
                    return 1;
                }
            }
            
            // Sort by mac address
            if (o1.getPhysAddr() == null || o2.getPhysAddr() == null) {
                if (o1.getPhysAddr() != null) {
                    return -1;
                } else if (o2.getPhysAddr() != null) {
                    return 1;
                }
            } else if ((diff = o1.getPhysAddr().compareTo(o2.getPhysAddr())) != 0) {
                return diff;
            }
            
            // Fallback to id
            return o1.getId().compareTo(o2.getId());
        }
        
    }
}

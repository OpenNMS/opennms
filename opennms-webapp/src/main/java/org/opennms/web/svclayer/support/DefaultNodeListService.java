package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.opennms.core.utils.IPSorter;
import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.Rows;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsArpInterface;
import org.opennms.netmgt.model.OnmsCategory;
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
import org.springframework.util.StringUtils;

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
        } else if (command.hasIfAlias()) {
            addCriteriaForIfalias(criteria, command.getIfAlias());
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
    }

    private void addCriteriaForIfalias(OnmsCriteria criteria, String ifAlias) {
        criteria.createAlias("node.ipInterfaces", "ipInterface");
        criteria.add(Restrictions.ne("ipInterface.isManaged", "D"));

        criteria.createAlias("node.ipInterfaces.snmpInterface", "snmpInterface");
        criteria.add(Restrictions.ilike("snmpInterface.ifAlias", ifAlias, MatchMode.ANYWHERE));
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
        Assert.notNull(categories, "categories argument must not be null");
        Assert.isTrue(categories.length >= 1, "categories must have at least one set of categories");

        // Build a list a list of category IDs to use when building the restrictions
        List<List<Integer>> categoryIdsList = new ArrayList<List<Integer>>(categories.length);
        for (String[] categoryStrings : categories) {
            List<Integer> categoryIds = new ArrayList<Integer>(categoryStrings.length);
            for (String categoryString : categoryStrings) {
                OnmsCategory category = m_categoryDao.findByName(categoryString);
                if (category == null) {
                    throw new IllegalArgumentException("Could not find category for name '" + categoryString + "'");
                }
                categoryIds.add(category.getId());
            }
            categoryIdsList.add(categoryIds);
        }
        
        for (List<Integer> categoryIds : categoryIdsList) {
            Type[] types = new Type[categoryIds.size()];
            String[] questionMarks = new String[categoryIds.size()];
            Type theOneAndOnlyType = new IntegerType();
            
            for (int i = 0; i < categoryIds.size(); i++) {
                types[i] = theOneAndOnlyType;
                questionMarks[i] = "?";
            }
            String sql = "{alias}.nodeId in (select distinct cn.nodeId from category_node cn where cn.categoryId in (" + StringUtils.arrayToCommaDelimitedString(questionMarks) + "))";
            criteria.add(Restrictions.sqlRestriction(sql, categoryIds.toArray(new Integer[categoryIds.size()]), types));
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
    

    @SuppressWarnings("unchecked")
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
    

    @SuppressWarnings("unchecked")
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
        int arpInterfaceCount = 0;
        List<NodeModel> displayNodes = new LinkedList<NodeModel>();
        for (OnmsNode node : onmsNodes) {
            List<OnmsIpInterface> displayInterfaces = new LinkedList<OnmsIpInterface>();
            List<OnmsArpInterface> displayArpInterfaces = new LinkedList<OnmsArpInterface>();
            if (command.getListInterfaces()) {
                if (command.hasIfAlias()) {
                    String ifAliasMatchString = (".*" + command.getIfAlias().toLowerCase().replaceAll("([\\W])", "\\\\$0").replaceAll("_", ".") + ".*"); 
                    for (OnmsIpInterface intf : node.getIpInterfaces()) {
                        if (intf.getSnmpInterface() != null && intf.getSnmpInterface().getIfAlias() != null && intf.getSnmpInterface().getIfAlias().toLowerCase().matches(ifAliasMatchString)) {
                            displayInterfaces.add(intf);
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
    
    public static class IpInterfaceComparator implements Comparator<OnmsIpInterface> {
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

    public static class ArpInterfaceComparator implements Comparator<OnmsArpInterface> {
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

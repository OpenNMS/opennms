//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 10: Cleanup imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.dao.hibernate;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.type.Type;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.StringUtils;

/**
 * <p>NodeDaoHibernate class.</p>
 *
 * @author Ted Kazmark
 * @author David Hustace
 * @version $Id: $
 */
public class NodeDaoHibernate extends AbstractDaoHibernate<OnmsNode, Integer>
        implements NodeDao {

    /**
     * <p>Constructor for NodeDaoHibernate.</p>
     */
    public NodeDaoHibernate() {
        super(OnmsNode.class);
    }

    /** {@inheritDoc} */
    public OnmsNode get(String lookupCriteria) {
        if (lookupCriteria.contains(":")) {
            String[] criteria = lookupCriteria.split(":");
            return findByForeignId(criteria[0], criteria[1]);
        }
        return get(Integer.parseInt(lookupCriteria));
    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findNodes(final OnmsDistPoller distPoller) {
        return find("from OnmsNode where distPoller = ?", distPoller);
    }

    /** {@inheritDoc} */
    public OnmsNode getHierarchy(Integer id) {
        OnmsNode node = findUnique(
                          "select distinct n from OnmsNode as n "
                                  + "left join fetch n.assetRecord "
                                  + "where n.id = ?", id);
        
        initialize(node.getIpInterfaces());
        for (OnmsIpInterface i : node.getIpInterfaces()) {
            initialize(i.getMonitoredServices());
        }
        
        initialize(node.getSnmpInterfaces());
        for (OnmsSnmpInterface i : node.getSnmpInterfaces()) {
            initialize(i.getIpInterfaces());
        }
        
        return node;

    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findByLabel(String label) {
        return find("from OnmsNode as n where n.label = ?", label);
    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findAllByVarCharAssetColumn(
            String columnName, String columnValue) {
        return find("from OnmsNode as n where n.assetRecord." + columnName
                + " = ?", columnValue);
    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(
            String columnName, String columnValue,
            Collection<OnmsCategory> categories) {
    	
        return find("select distinct n from OnmsNode as n "
        		+ "join n.categories as c "
                + "left join fetch n.assetRecord "
                + "left join fetch n.ipInterfaces as iface "
                + "left join fetch iface.monitoredServices as monSvc "
                + "left join fetch monSvc.serviceType "
                + "left join fetch monSvc.currentOutages "
                + "where n.assetRecord." + columnName + " = ? "
                + "and c.name in ("+categoryListToNameList(categories)+")", columnValue);
    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findByCategory(OnmsCategory category) {
        return find("select distinct n from OnmsNode as n "
                    + "join n.categories c "
                    + "left join fetch n.assetRecord "
                    + "left join fetch n.ipInterfaces as iface "
                    + "left join fetch iface.monitoredServices as monSvc "
                    + "left join fetch monSvc.serviceType "
                    + "left join fetch monSvc.currentOutages "
                    + "where c.name = ?",
                    category.getName());
    }

	private String categoryListToNameList(Collection<OnmsCategory> categories) {
		List<String> categoryNames = new ArrayList<String>();
    	for (OnmsCategory category : categories) {
			categoryNames.add(category.getName());
		}
		return StringUtils.collectionToDelimitedString(categoryNames, ", ", "'", "'");
	}
        
        

    /** {@inheritDoc} */
    public Collection<OnmsNode> findAllByCategoryList(
            Collection<OnmsCategory> categories) {
        return find("select distinct n from OnmsNode as n "
                + "join n.categories c " 
                + "left join fetch n.assetRecord "
                + "left join fetch n.ipInterfaces as iface "
                + "left join fetch n.snmpInterfaces as snmpIface"
                + "left join fetch iface.monitoredServices as monSvc "
                + "left join fetch monSvc.serviceType "
                + "left join fetch monSvc.currentOutages "
                + "where c.name in ("+categoryListToNameList(categories)+")"
                + "and n.type != 'D'");
    }

    /** {@inheritDoc} */
    public Collection<OnmsNode> findAllByCategoryLists( final Collection<OnmsCategory> rowCategories, final Collection<OnmsCategory> columnCategories) {
    	
        return getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsNode>>() {

            public Collection<OnmsNode> doInHibernate(Session session) throws HibernateException, SQLException {
                
                return (Collection<OnmsNode>)session.createQuery("select distinct n from OnmsNode as n "
                + "join n.categories c1 "
                + "join n.categories c2 "
                + "left join fetch n.assetRecord "
                + "left join fetch n.ipInterfaces as iface "
                + "left join fetch n.snmpInterfaces as snmpIface"
                + "left join fetch iface.monitoredServices as monSvc "
                + "left join fetch monSvc.serviceType "
                + "left join fetch monSvc.currentOutages "
                + "where c1 in (:rowCategories) "
                + "and c2 in (:colCategories) "
                + "and n.type != 'D'")
                .setParameterList("rowCategories", rowCategories)
                .setParameterList("colCategories", columnCategories)
                .list();
                

            }

        });
        
    }
    
    public static class SimpleSurveillanceStatus implements SurveillanceStatus {
        
        private int m_serviceOutages;
        private int m_nodesWithOutages;
        private int m_nodeCount;
        
        public SimpleSurveillanceStatus(Number serviceOutages, Number nodesWithOutages, Number nodeCount) {
            System.err.println(String.format("Args: %s (%s), %s (%s), %s (%s)", 
                    serviceOutages, serviceOutages == null ? null : serviceOutages.getClass(),
                    nodesWithOutages, nodesWithOutages == null ? null : nodesWithOutages.getClass(),
                    nodeCount, nodeCount == null ? null : nodeCount.getClass()
                    ));
                    
            m_serviceOutages = serviceOutages == null ? 0 : serviceOutages.intValue();
            m_nodesWithOutages = nodesWithOutages == null ? 0 : nodesWithOutages.intValue();
            m_nodeCount = nodeCount == null ? 0 : nodeCount.intValue();
        }

        public Integer getDownEntityCount() {
            return m_nodesWithOutages;
        }

        public Integer getTotalEntityCount() {
            return m_nodeCount;
        }

        public String getStatus() {
            switch (m_serviceOutages) {
            case 0:  return "Normal";
            case 1:  return "Warning";
            default: return "Critical";
            }
        }
        
    }
    public SurveillanceStatus findSurveillanceStatusByCategoryLists(final Collection<OnmsCategory> rowCategories, final Collection<OnmsCategory> columnCategories) {
        return getHibernateTemplate().execute(new HibernateCallback<SurveillanceStatus>() {

            public SurveillanceStatus doInHibernate(Session session) throws HibernateException, SQLException {
                
                /*
                 * select 
                 *      count(distinct case when currentout7_.outageId is null then null else monitoreds6_.id end), 
                 *      count(distinct case when currentout7_.outageId is null then null else onmsnode0_.nodeId end), 
                 *      count(distinct onmsnode0_.nodeId) 
                 * from 
                 *      node onmsnode0_ 
                 * left outer join 
                 *      pathOutage onmsnode0_1_ on onmsnode0_.nodeId=onmsnode0_1_.nodeId 
                 * inner join 
                 *      category_node categories1_ on onmsnode0_.nodeId=categories1_.nodeId 
                 * inner join 
                 *      categories onmscatego2_ on categories1_.categoryId=onmscatego2_.categoryid 
                 * inner join 
                 *      category_node categories3_ on onmsnode0_.nodeId=categories3_.nodeId 
                 * inner join 
                 *      categories onmscatego4_ on categories3_.categoryId=onmscatego4_.categoryid 
                 * left outer join 
                 *      ipInterface ipinterfac5_ on onmsnode0_.nodeId=ipinterfac5_.nodeId 
                 * left outer join 
                 *      ifServices monitoreds6_ on ipinterfac5_.id=monitoreds6_.ipInterfaceId 
                 * left outer join 
                 *      outages currentout7_ on monitoreds6_.id=currentout7_.ifserviceId and ( currentout7_.ifRegainedService is null) 
                 * where 
                 *      (onmscatego2_.categoryid in (14)) 
                 * and 
                 *      (onmscatego4_.categoryid in (36)) 
                 * and 
                 *      monitoreds6_.status='A' 
                 * and 
                 *      qonmsnode0_.nodeType<>'D';
                 */
                Constructor<?> constructor = SimpleSurveillanceStatus.class.getConstructors()[0];
                
                session.flush();

                return (SimpleSurveillanceStatus)session.createSQLQuery("select" +
                		" count(distinct case when outages.outageid is null then null else monSvc.id end)," +
                		" count(distinct case when outages.outageid is null then null else node.nodeid end)," +
                		" count(distinct node.nodeid)" +
                		" from node" +
                		" join category_node cn1 using (nodeid)" +
                		" join category_node cn2 using (nodeid)" +
                		" left outer join ipinterface ip using (nodeid)" +
                		" left outer join ifservices monsvc on (monsvc.ipinterfaceid = ip.id)" +
                		" left outer join outages on (outages.ifserviceid = monsvc.id and outages.ifregainedservice is null)" +
                        " where nodeType <> 'D'" +
                        " and monSvc.status = 'A'" +
                		" and cn1.categoryid in (:rowCategories)" +
                		" and cn2.categoryid in (:columnCategories)"
                		)
                		.setParameterList("rowCategories", rowCategories)
                		.setParameterList("columnCategories", columnCategories)
                        .setResultTransformer(new AliasToBeanConstructorResultTransformer(constructor))
                        .uniqueResult();
                        

//                return (SurveillanceStatus)session.createQuery(
//                        "select new "+ SimpleSurveillanceStatus.class.getName()
//                        + "(sum(case when currOut is null then 0 else 1 end), count(distinct case when currOut is null then 0 else n.id end), count(distinct n)) "
//                        + "from OnmsNode as n " 
//                        + "join n.categories c1 "
//                        + "join n.categories c2 "
//                        + "left join n.ipInterfaces as iface "
//                        + "left join iface.monitoredServices as monSvc "
//                        + "left join monSvc.currentOutages as currOut "
//                        + "where c1 in (:rowCategories) "
//                        + "and c2 in (:colCategories) "
//                        + "and monSvc.status = 'A' "
//                        + "and n.type != 'D' "
//                        )
//                        .setParameterList("rowCategories", rowCategories)
//                        .setParameterList("colCategories", columnCategories)
//                        .uniqueResult();


            }

        });

    }


    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource) {
        List<Object[]> pairs = getHibernateTemplate().find("select n.id, n.foreignId from OnmsNode n where n.foreignSource = ?", foreignSource);
        Map<String, Integer> foreignIdMap = new HashMap<String, Integer>();
        for (Object[] pair : pairs) {
            foreignIdMap.put((String)pair[1], (Integer)pair[0]);
        }
        return foreignIdMap;
    }

    /** {@inheritDoc} */
    public List<OnmsNode> findByForeignSource(String foreignSource) {
        return find("from OnmsNode n where n.foreignSource = ?", foreignSource);
    }

    /** {@inheritDoc} */
    public OnmsNode findByForeignId(String foreignSource, String foreignId) {
        return findUnique("from OnmsNode n where n.foreignSource = ? and n.foreignId = ?", foreignSource, foreignId);
    }
    
    /** {@inheritDoc} */
    public List<OnmsNode> findByForeignSourceAndIpAddress(String foreignSource, String ipAddress) {
        if (foreignSource == null) {
            return find("select distinct n from OnmsNode n join n.ipInterfaces as ip where n.foreignSource is NULL and ip.ipAddress = ?", ipAddress);
        } else {
            return find("select distinct n from OnmsNode n join n.ipInterfaces as ip where n.foreignSource = ? and ip.ipAddress = ?", foreignSource, ipAddress);
        }
    }

    /** {@inheritDoc} */
    public int getNodeCountForForeignSource(String foreignSource) {
        return queryInt("select count(*) from OnmsNode as n where n.foreignSource = ?", foreignSource);
    }
    
    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsNode> findAll() {
        return find("from OnmsNode order by label");
    }
    
    /**
     * <p>findAllProvisionedNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsNode> findAllProvisionedNodes() {
        return find("from OnmsNode n where n.foreignSource is not null");
    }
    
    /** {@inheritDoc} */
    public List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId, Date scanStamp) {
        return findObjects(OnmsIpInterface.class, "from OnmsIpInterface iface where iface.node.id = ? and (iface.ipLastCapsdPoll is null or iface.ipLastCapsdPoll < ?)", nodeId, scanStamp);
    }

    /** {@inheritDoc} */
    public void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp) {
        getHibernateTemplate().bulkUpdate("delete from OnmsIpInterface iface where iface.node.id = ? and (iface.ipLastCapsdPoll is null or iface.ipLastCapsdPoll < ?)", new Object[] { nodeId, scanStamp });
        getHibernateTemplate().bulkUpdate("delete from OnmsSnmpInterface iface where iface.node.id = ? and (iface.lastCapsdPoll is null or iface.lastCapsdPoll < ?)", new Object[] { nodeId, scanStamp });
    }

    /** {@inheritDoc} */
    public void updateNodeScanStamp(Integer nodeId, Date scanStamp) {
        OnmsNode n = get(nodeId);
        n.setLastCapsdPoll(scanStamp);
        update(n);
    }

    /**
     * <p>getNodeIds</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Integer> getNodeIds() {
        return findObjects(Integer.class, "select distinct n.id from OnmsNode as n where n.type != 'D'");
    }


    
    


}

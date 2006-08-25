//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceMapper;
import org.opennms.netmgt.dao.jdbc.monsvc.MonitoredServiceMapper;
import org.opennms.netmgt.dao.jdbc.monsvc.MonitoredServiceMapperWithLazyRelatives;
import org.opennms.netmgt.dao.jdbc.node.FindAll;
import org.opennms.netmgt.dao.jdbc.node.FindByAssetNumber;
import org.opennms.netmgt.dao.jdbc.node.FindByDpName;
import org.opennms.netmgt.dao.jdbc.node.FindByNodeId;
import org.opennms.netmgt.dao.jdbc.node.FindByNodeLabel;
import org.opennms.netmgt.dao.jdbc.node.FindByVarCharAssetColumn;
import org.opennms.netmgt.dao.jdbc.node.FindByVarCharAssetColumnAndCategoryList;
import org.opennms.netmgt.dao.jdbc.node.LazyNode;
import org.opennms.netmgt.dao.jdbc.node.NodeDelete;
import org.opennms.netmgt.dao.jdbc.node.NodeMapper;
import org.opennms.netmgt.dao.jdbc.node.NodeMapperWithLazyRelatives;
import org.opennms.netmgt.dao.jdbc.node.NodeSave;
import org.opennms.netmgt.dao.jdbc.node.NodeUpdate;
import org.opennms.netmgt.dao.jdbc.outage.OutageMapper;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

public class NodeDaoJdbc extends AbstractDaoJdbc implements NodeDao {
    
    public NodeDaoJdbc() {
        super();
    }
    
    public NodeDaoJdbc(DataSource ds) {
        super(ds);
    }
    
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from node");
    }
    
    public void delete(OnmsNode node) {
        if (node.getId() == null)
            throw new IllegalArgumentException("cannot delete a node with nodeid = null");
        
        getNodeDeleter().doDelete(node);
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public Set findNodes(OnmsDistPoller dp) {
        return new FindByDpName(getDataSource()).findSet(dp.getName());
    }

    public void flush() {
    }

    public OnmsNode get(int id) {
        return get(new Integer(id));
    }

    public OnmsNode get(Integer id) {
        if (Cache.retrieve(OnmsNode.class, id) == null)
            return new FindByNodeId(getDataSource()).findUnique(id);
        else
            return (OnmsNode)Cache.retrieve(OnmsNode.class, id);
    }

    @SuppressWarnings("unchecked")
	public OnmsNode getHierarchy(Integer id) {
    	
    	final String hierarchyQuery = "SELECT " +
    	"node.nodeid as nodeid, " +
    	"node.dpName as dpName, " +
    	"node.nodeCreateTime as nodeCreateTime, " +
    	"node.nodeParentID as nodeParentID, " +
    	"node.nodeType as nodeType, " +
    	"node.nodeSysOid as nodeSysOid, " +
    	"node.nodeSysName as nodeSysName, " +
    	"node.nodeSysDescription as nodeSysDescription, " +
    	"node.nodeSysLocation as nodeSysLocation, " +
    	"node.nodeSysContact as nodeSysContact, " +
    	"node.nodeLabel as nodeLabel, " +
    	"node.nodeLabelSource as nodeLabelSource, " +
    	"node.nodeNetBiosName as nodeNetBiosName, " +
    	"node.nodeDomainName as nodeDomainName, " +
    	"node.operatingSystem as operatingSystem, " +
    	"node.lastCapsdPoll as lastCapsdPoll, " +
		"ipInterface.nodeID as ipInterface_nodeID, " +
		"ipInterface.ipAddr as ipInterface_ipAddr, " +
		"ipInterface.ifIndex as ipInterface_ifIndex, " +
		"ipInterface.ipHostname as ipInterface_ipHostname, " +
		"ipInterface.isManaged as ipInterface_isManaged, " +
		"ipInterface.ipStatus as ipInterface_ipStatus, " +
		"ipInterface.ipLastCapsdPoll as ipInterface_ipLastCapsdPoll, " +
		"ipInterface.isSnmpPrimary as ipInterface_isSnmpPrimary, " +
		"ifservices.nodeid as ifservices_nodeid, " +
		"ifservices.ipAddr as ifservices_ipAddr, " +
		"ifservices.ifIndex as ifservices_ifIndex, " +
		"ifservices.serviceId as ifservices_serviceId, " +
		"ifservices.lastGood as ifservices_lastGood, " +
		"ifservices.lastFail as ifservices_lastFail, " +
		"ifservices.qualifier as ifservices_qualifier, " +
		"ifservices.status as ifservices_status, " +
		"ifservices.source as ifservices_source, " +
		"ifservices.notify as ifservices_notify, " +
		"outages.outageID as outages_outageID, " +
		"outages.svcLostEventID as outages_svcLostEventID, " +
		"outages.svcRegainedEventID as outages_svcRegainedEventID, " +
		"outages.nodeID as outages_nodeID, " +
		"outages.ipAddr as outages_ipAddr, " +
		"outages.serviceID as outages_serviceID, " +
		"ifservices.ifIndex as outages_ifIndex, " +
		"outages.ifLostService as outages_ifLostService, " +
		"outages.ifRegainedService as outages_ifRegainedService, " +
		"outages.suppressTime as outages_suppressTime, " +
		"outages.suppressedBy as outages_suppressedBy " +
    	"FROM node " +
    	"LEFT JOIN ipInterface ON (node.nodeId = ipInterface.nodeId) " +
    	"LEFT JOIN ifservices ON (ipInterface.nodeId = ifservices.nodeId AND ipInterface.ipAddr = ifservices.ipAddr) " +
    	"LEFT JOIN outages ON (ifServices.nodeId = outages.nodeId AND ifServices.ipAddr = outages.ipAddr AND ifServices.serviceID = outages.serviceId AND outages.ifRegainedService is null) " +
    	"WHERE node.nodeId = ?" +
    	"";
    	
    	RowMapper rowMapper = new RowMapper() {
    		// we use the lazy mapper to that the other relationships that we are using
    		// are filling in in a lazy way
    		NodeMapper nodeMapper = new NodeMapperWithLazyRelatives(getDataSource()) {

				@Override
				protected void setIpInterfaces(OnmsNode node) {
					// we override this to do nothing and just fill in the set with the rest of the query
				}
    			
    		};
    		IpInterfaceMapper ifMapper = new IpInterfaceMapper();
    		MonitoredServiceMapper monSvcMapper = new MonitoredServiceMapper();
    		OutageMapper outageMapper = new OutageMapper();

			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				OnmsNode node = nodeMapper.mapNode(rs, rowNum);
				OnmsIpInterface iface = ifMapper.mapInterface(rs, rowNum);
				if (iface != null) {
					node.getIpInterfaces().add(iface);
					OnmsMonitoredService monSvc = (OnmsMonitoredService)monSvcMapper.mapRow(rs, rowNum);
					if (monSvc != null) {
						iface.getMonitoredServices().add(monSvc);
						OnmsOutage outage = (OnmsOutage)outageMapper.mapRow(rs, rowNum);
						if (outage != null) {
							monSvc.getCurrentOutages().add(outage);
						}
					}
				}
				return node;
			}
    		
    	};
    	
    	Set<OnmsNode> nodes = new HashSet<OnmsNode>(getJdbcTemplate().query(hierarchyQuery, new Object[] { id }, rowMapper));
    	if (nodes.isEmpty())
    		return null;
    	else if (nodes.size() == 1)
    		return (OnmsNode)nodes.iterator().next();
    	else
    		throw new IncorrectResultSizeDataAccessException("Unexpected number of nodes returned.", 1, nodes.size());
    }

    public OnmsNode load(int id) {
        return load(new Integer(id));
    }

    public OnmsNode load(Integer id) {
        OnmsNode node = get(id);
        if (node == null)
            throw new IllegalArgumentException("unable to load node with id "+id);
        
        return node;
    }

    public void save(OnmsNode node) {
        if (node.getId() != null)
            throw new IllegalArgumentException("Cannot save a node that already has a nodeid");
        
        node.setId(allocateId());
        getNodeSaver().doInsert(node);
        cascadeSaveAssociations(node);
    }

    public void saveOrUpdate(OnmsNode node) {
        if (node.getId() == null)
            save(node);
        else
            update(node);
    }

    public void update(OnmsNode node) {
        if (node.getId() == null)
            throw new IllegalArgumentException("Cannot update a node without a nodeid");
        
        if (isDirty(node))
        		getNodeUpdater().doUpdate(node);
        cascadeUpdateAssociations(node);
    }

    private boolean isDirty(OnmsNode node) {
    		if (node instanceof LazyNode) {
    			LazyNode lazyNode = (LazyNode) node;
    			return lazyNode.isDirty();
    		}
    		return true;
    }

	private Integer allocateId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('nodeNxtId')"));
    }

    private void cascadeSaveAssociations(OnmsNode node) {
        saveCategoryMapping(node);
        getAssetRecordDao().save(node.getAssetRecord());
        getIpInterfaceDao().saveIfsForNode(node);
        getSnmpInterfaceDao().saveIfsForNode(node);
    }

    private void saveCategoryMapping(OnmsNode node) {
        for (Iterator it = node.getCategories().iterator(); it.hasNext();) {
            OnmsCategory category = (OnmsCategory) it.next();
            getJdbcTemplate().update("insert into category_node (nodeid, categoryId) values (?,?)", new Object[] { node.getId(), category.getId()});
        }
    }

    private void cascadeUpdateAssociations(OnmsNode node) {
        udpateCategoryMapping(node);
        getAssetRecordDao().update(node.getAssetRecord());
        getIpInterfaceDao().saveOrUpdateIfsForNode(node);
        getSnmpInterfaceDao().saveOrUpdateIfsForNode(node);
    }

    private void udpateCategoryMapping(OnmsNode node) {
    	if (!isDirty(node.getCategories())) return;
        getJdbcTemplate().update("delete from category_node where nodeid = ?", new Object[] { node.getId() });
        saveCategoryMapping(node);
    }

    private AssetRecordDaoJdbc getAssetRecordDao() {
        return (new AssetRecordDaoJdbc(getDataSource()));
    }

    private IpInterfaceDaoJdbc getIpInterfaceDao() {
        return new IpInterfaceDaoJdbc(getDataSource());
    }
    
    private SnmpInterfaceDaoJdbc getSnmpInterfaceDao() {
        return new SnmpInterfaceDaoJdbc(getDataSource());
    }

    private NodeDelete getNodeDeleter() {
        return new NodeDelete(getDataSource());
    }

    private NodeSave getNodeSaver() {
        return new NodeSave(getDataSource());
    }

    private NodeUpdate getNodeUpdater() {
        return new NodeUpdate(getDataSource());
    }

	public OnmsNode findByAssetNumber(String assetNumber) {
		return new FindByAssetNumber(getDataSource()).findUnique(assetNumber);
	}

	public Collection findByLabel(String label) {
		return new FindByNodeLabel(getDataSource()).execute(label);
	}

    /**
     * This method searches for nodes matching a column in the assets table. 
     * Note: this implmentation requires that the column type be of type VARCHAR.
     * 
     * @param columnName is VARCHAR column in assets table used in where clause
     * @param columnValue is the value used for matching <code>columnName</code>
     * @return Collection of nodes.
     */
    public Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue) {
        log().debug("findAllByVarCharAssetColumn: beginning find.");
        List<OnmsNode> nodes = new FindByVarCharAssetColumn(getDataSource(), columnName).execute(columnValue);
        log().debug("findAllByVarCharAssetColumn: find complete. Nodes found: "+nodes.size());
        return nodes;
    }

    /**
     * This method searches for nodes matching a column in the assets table and also having been
     * assigned to a category in the cateories table.  Note: this implmentation requires that
     * the column type be of type VARCHAR.
     * 
     * @param columnName is VARCHAR column in assets table used in where clause
     * @param columnValue is the value used for matching <code>columnName</code>
     * @param categoryNames is a collection of names from categories
     *  table assigned to the node via category_node table
     * @return Collection of nodes.
     */
    public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue, Collection<String> categoryNames) {
        log().debug("findAllByVarCharAssetColumnCategoryList: beginning find.");
        List<OnmsNode> nodes = new FindByVarCharAssetColumnAndCategoryList(getDataSource(), columnName, categoryNames).execute(columnValue);
        log().debug("findAllByVarCharAssetColumnCateoryList: find complete. Nodes found: "+nodes.size());
        return nodes;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}

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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.jdbc.node.FindAll;
import org.opennms.netmgt.dao.jdbc.node.FindByAssetNumber;
import org.opennms.netmgt.dao.jdbc.node.FindByDpName;
import org.opennms.netmgt.dao.jdbc.node.FindByNodeId;
import org.opennms.netmgt.dao.jdbc.node.FindByNodeLabel;
import org.opennms.netmgt.dao.jdbc.node.FindByVarCharAssetColumn;
import org.opennms.netmgt.dao.jdbc.node.FindByVarCharAssetColumnAndCategoryList;
import org.opennms.netmgt.dao.jdbc.node.LazyNode;
import org.opennms.netmgt.dao.jdbc.node.NodeDelete;
import org.opennms.netmgt.dao.jdbc.node.NodeSave;
import org.opennms.netmgt.dao.jdbc.node.NodeUpdate;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;

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

    public OnmsNode getHierarchy(Integer id) {
        return get(id);
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

    public Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue) {
        return new FindByVarCharAssetColumn(getDataSource(), columnName).execute(columnValue);
    }

    public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue, Collection<String> categoryNames) {
        return new FindByVarCharAssetColumnAndCategoryList(getDataSource(), columnName, categoryNames).execute(columnValue);
    }

}

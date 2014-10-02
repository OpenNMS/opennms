/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.SurveillanceStatus;

public class TestNodeDao implements NodeDao {


	@Override
	public OnmsNode get(Integer id) {
		OnmsNode node = new OnmsNode();
		node.setId(777);
		node.setLabel("p-brane");
		return node;
	}
	
	@Override
	public String getLabelForId(Integer id) {
		if (id.intValue() == 777) {
			return "p-brane";
		}
		return null;
	}
	
	@Override
	public OnmsNode load(Integer id) {
		OnmsNode node = new OnmsNode();
		node.setId(777);
		node.setLabel("p-brane");
		return node;
	}
	
	@Override
	public List<OnmsNode> findAll() {
		
		List<OnmsNode> nodeList = new LinkedList<OnmsNode>();
		
		OnmsNode node = new OnmsNode();
		node.setId(777);
		node.setLabel("p-brane");
		
		nodeList.add(node);
		return nodeList;
	}

	@Override
	public void lock() {
	}

	@Override
	public void initialize(Object obj) {
	}

	@Override
	public void flush() {
	}

	@Override
	public void clear() {
	}

	@Override
	public int countAll() {
		return 1;
	}

	@Override
	public void delete(OnmsNode entity) {
	}

	@Override
	public void delete(Integer key) {
	}

	@Override
	public List<OnmsNode> findMatching(Criteria criteria) {
		return null;
	}

	@Override
	public List<OnmsNode> findMatching(OnmsCriteria criteria) {
		return null;
	}

	@Override
	public int countMatching(Criteria onmsCrit) {
		return 0;
	}

	@Override
	public int countMatching(OnmsCriteria onmsCrit) {
		return 0;
	}

	@Override
	public void save(OnmsNode entity) {
	}

	@Override
	public void saveOrUpdate(OnmsNode entity) {
	}

	@Override
	public void update(OnmsNode entity) {
	}

	@Override
	public OnmsNode get(String lookupCriteria) {
		return null;
	}
	
	@Override
	public List<OnmsNode> findByLabel(String label) {
		return null;
	}

	@Override
	public List<OnmsNode> findNodes(OnmsDistPoller dp) {
		return null;
	}

	@Override
	public OnmsNode getHierarchy(Integer id) {
		return null;
	}

	@Override
	public Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource) {
		return null;
	}

	@Override
	public List<OnmsNode> findAllByVarCharAssetColumn(String columnName,
			String columnValue) {
		return null;
	}

	@Override
	public List<OnmsNode> findAllByVarCharAssetColumnCategoryList(
			String columnName, String columnValue,
			Collection<OnmsCategory> categories) {
		return null;
	}

	@Override
	public List<OnmsNode> findByCategory(OnmsCategory category) {
		return null;
	}

	@Override
	public List<OnmsNode> findAllByCategoryList(
			Collection<OnmsCategory> categories) {
		return null;
	}

	@Override
	public List<OnmsNode> findAllByCategoryLists(
			Collection<OnmsCategory> rowCatNames,
			Collection<OnmsCategory> colCatNames) {
		return null;
	}

	@Override
	public List<OnmsNode> findByForeignSource(String foreignSource) {
		return null;
	}

	@Override
	public OnmsNode findByForeignId(String foreignSource, String foreignId) {
		return null;
	}

	@Override
	public int getNodeCountForForeignSource(String groupName) {
		return 0;
	}

	@Override
	public List<OnmsNode> findAllProvisionedNodes() {
		return null;
	}

	@Override
	public List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId,
			Date scanStamp) {
		return null;
	}

	@Override
	public void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp) {
	}

	@Override
	public void updateNodeScanStamp(Integer nodeId, Date scanStamp) {
	}

	@Override
	public Collection<Integer> getNodeIds() {
		return null;
	}

	@Override
	public List<OnmsNode> findByForeignSourceAndIpAddress(
			String foreignSource, String ipAddress) {
		return null;
	}

	@Override
	public SurveillanceStatus findSurveillanceStatusByCategoryLists(
			Collection<OnmsCategory> rowCategories,
			Collection<OnmsCategory> columnCategories) {
		return null;
	}

	@Override
	public Integer getNextNodeId(Integer nodeId) {
		return null;
	}

	@Override
	public Integer getPreviousNodeId(Integer nodeId) {
		return null;
	}
	
}
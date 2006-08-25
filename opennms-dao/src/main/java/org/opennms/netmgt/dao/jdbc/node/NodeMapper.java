package org.opennms.netmgt.dao.jdbc.node;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.jdbc.core.RowMapper;

public class NodeMapper implements RowMapper {
	
	public NodeMapper() {
	}
	
	public OnmsNode mapNode(ResultSet rs, int rowNum) throws SQLException {
		return (OnmsNode)mapRow(rs, rowNum);
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		final Integer id = (Integer) rs.getObject("nodeid");
		
		LazyNode node = (LazyNode)Cache.obtain(OnmsNode.class, id);
		node.setLoaded(true);
		
		String dpName = rs.getString("dpName");
		OnmsDistPoller distPoller = (OnmsDistPoller)Cache.obtain(OnmsDistPoller.class, dpName);
		
		node.setDistPoller(distPoller);
		
		OnmsAssetRecord asset = (OnmsAssetRecord)Cache.obtain(OnmsAssetRecord.class, id);
		node.setAssetRecord(asset);
		
		Integer parentId = (Integer)rs.getObject("nodeParentID");
		if (parentId == null) {
			node.setParent(null);
		} else {
			OnmsNode parent = (OnmsNode)Cache.obtain(OnmsNode.class, parentId);
			node.setParent(parent);
		}
		
		node.setCreateTime(rs.getTime("nodeCreateTime"));
		node.setType(rs.getString("nodeType"));
		node.setSysObjectId(rs.getString("nodeSysOid"));
		node.setSysName(rs.getString("nodeSysName"));
		node.setSysDescription((rs.getString("nodeSysDescription")));
		node.setSysLocation(rs.getString("nodeSysLocation"));
		node.setSysContact(rs.getString("nodeSysContact"));
		node.setLabel(rs.getString("nodeLabel"));
		node.setLabelSource(rs.getString("nodeLabelSource"));
		node.setNetBiosName(rs.getString("nodeNetBiosName"));
		node.setNetBiosDomain(rs.getString("nodeDomainName"));;
		node.setOperatingSystem(rs.getString("operatingSystem"));
		node.setLastCapsdPoll(rs.getTime("lastCapsdPoll"));
		
		setIpInterfaces(node);
		
		setCategories(node);
		
		setSnmpInterfaces(node);
		
		node.setDirty(false);
		return node;
	}

	protected void setSnmpInterfaces(OnmsNode node) {
	}

	protected void setCategories(OnmsNode node) {
	}

	protected void setIpInterfaces(OnmsNode node) {
	}

}

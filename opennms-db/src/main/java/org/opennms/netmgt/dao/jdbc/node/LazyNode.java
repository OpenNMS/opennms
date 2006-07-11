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
package org.opennms.netmgt.dao.jdbc.node;

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;

public class LazyNode extends OnmsNode {
	
	private static final long serialVersionUID = 1L;

	private DataSource m_dataSource;
	private boolean m_dirty;
	private boolean m_loaded = false;
	
	public LazyNode(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public OnmsAssetRecord getAssetRecord() {
		load();
		return super.getAssetRecord();
	}

	public Set getCategories() {
        load();
        return super.getCategories();
    }

	public Date getCreateTime() {
		load();
		return super.getCreateTime();
	}

	public OnmsDistPoller getDistPoller() {
		load();
		return super.getDistPoller();
	}

	public Set getIpInterfaces() {
		load();
		return super.getIpInterfaces();
	}

	public String getLabel() {
		load();
		return super.getLabel();
	}

	public String getLabelSource() {
		load();
		return super.getLabelSource();
	}

	public Date getLastCapsdPoll() {
		load();
		return super.getLastCapsdPoll();
	}

	public String getNetBiosDomain() {
		load();
		return super.getNetBiosDomain();
	}

	public String getNetBiosName() {
		load();
		return super.getNetBiosName();
	}

	public String getOperatingSystem() {
		load();
		return super.getOperatingSystem();
	}

	public OnmsNode getParent() {
		load();
		return super.getParent();
	}

	public Set getSnmpInterfaces() {
		load();
		return super.getSnmpInterfaces();
	}

	public String getSysContact() {
		load();
		return super.getSysContact();
	}

	public String getSysDescription() {
		load();
		return super.getSysDescription();
	}

	public String getSysLocation() {
		load();
		return super.getSysLocation();
	}

	public String getSysName() {
		load();
		return super.getSysName();
	}

	public String getSysObjectId() {
		load();
		return super.getSysObjectId();
	}

	public String getType() {
		load();
		return super.getType();
	}

	public boolean isDirty() {
		return m_dirty;
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setAssetRecord(OnmsAssetRecord asset) {
		load();
		setDirty(true);
		super.setAssetRecord(asset);
	}

	public void setCategories(Set categories) {
        load();
        setDirty(true);
        super.setCategories(categories);
    }

	public void setCreateTime(Date nodecreatetime) {
		load();
		setDirty(true);
		super.setCreateTime(nodecreatetime);
	}

	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setDistPoller(OnmsDistPoller distpoller) {
		load();
		setDirty(true);
		super.setDistPoller(distpoller);
	}

	public void setIpInterfaces(Set ipinterfaces) {
		load();
		setDirty(true);
		super.setIpInterfaces(ipinterfaces);
	}

	public void setLabel(String nodelabel) {
		load();
		setDirty(true);
		super.setLabel(nodelabel);
	}

	public void setLabelSource(String nodelabelsource) {
		load();
		setDirty(true);
		super.setLabelSource(nodelabelsource);
	}

	public void setLastCapsdPoll(Date lastcapsdpoll) {
		load();
		setDirty(true);
		super.setLastCapsdPoll(lastcapsdpoll);
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

	public void setNetBiosDomain(String nodedomainname) {
		load();
		setDirty(true);
		super.setNetBiosDomain(nodedomainname);
	}

	public void setNetBiosName(String nodenetbiosname) {
		load();
		setDirty(true);
		super.setNetBiosName(nodenetbiosname);
	}

	public void setOperatingSystem(String operatingsystem) {
		load();
		setDirty(true);
		super.setOperatingSystem(operatingsystem);
	}

	public void setParent(OnmsNode parent) {
		load();
		setDirty(true);
		super.setParent(parent);
	}

	public void setSnmpInterfaces(Set snmpinterfaces) {
		load();
		setDirty(true);
		super.setSnmpInterfaces(snmpinterfaces);
	}

	public void setSysContact(String nodesyscontact) {
		load();
		setDirty(true);
		super.setSysContact(nodesyscontact);
	}

	public void setSysDescription(String nodesysdescription) {
		load();
		setDirty(true);
		super.setSysDescription(nodesysdescription);
	}

	public void setSysLocation(String nodesyslocation) {
		load();
		setDirty(true);
		super.setSysLocation(nodesyslocation);
	}

	public void setSysName(String nodesysname) {
		load();
		setDirty(true);
		super.setSysName(nodesysname);
	}

	public void setSysObjectId(String nodesysoid) {
		load();
		setDirty(true);
		super.setSysObjectId(nodesysoid);
	}
	
	public void setType(String nodetype) {
		load();
		setDirty(true);
		super.setType(nodetype);
	}

    public String toString() {
		load();
		setDirty(true);
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindByNodeId(m_dataSource).findUnique(getId());
		}
	}

	

}

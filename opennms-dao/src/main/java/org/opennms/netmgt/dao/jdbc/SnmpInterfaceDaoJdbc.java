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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.jdbc.snmpif.FindAll;
import org.opennms.netmgt.dao.jdbc.snmpif.FindById;
import org.opennms.netmgt.dao.jdbc.snmpif.FindByNode;
import org.opennms.netmgt.dao.jdbc.snmpif.LazySnmpInterface;
import org.opennms.netmgt.dao.jdbc.snmpif.SnmpInterfaceId;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceDaoJdbc extends AbstractDaoJdbc implements
		SnmpInterfaceDao {

	public SnmpInterfaceDaoJdbc() {
		super();
	}

	public SnmpInterfaceDaoJdbc(DataSource ds) {
		super(ds);
	}

	public int countAll() {
		return getJdbcTemplate().queryForInt(
				"select count(*) from snmpInterface");
	}

	public void delete(OnmsSnmpInterface iface) {
		Object[] parms = new Object[] { iface.getNode().getId(),
				iface.getIpAddress(), iface.getIfIndex() };
		getJdbcTemplate()
				.update(
						"delete from snmpInterface where nodeid = ? and ipaddr = ? and snmpifindex = ?",
						parms);
	}

	public void deleteInterfacesForNode(OnmsNode node) {
		getJdbcTemplate().update("delete from snmpinterface where nodeid = ?",
				new Object[] { node.getId() });
	}

	public Collection findAll() {
		return new FindAll(getDataSource()).findSet();
	}

	public Set findByNode(OnmsNode node) {
		return new FindByNode(getDataSource()).findSet(node.getId());
	}

	public void flush() {
	}

	public OnmsSnmpInterface get(Integer dbNodeId, String dbIpAddr,
			Integer dbIfIndex) {
		return get(new SnmpInterfaceId(dbNodeId, dbIpAddr, dbIfIndex));
	}

	public OnmsSnmpInterface get(SnmpInterfaceId id) {
		OnmsSnmpInterface iface = (OnmsSnmpInterface) Cache.obtain(
				OnmsSnmpInterface.class, id);
		if (iface != null)
			return iface;

		return FindById.get(getDataSource(), id).find(id);
	}

	public OnmsSnmpInterface get(Long id) {
		throw new RuntimeException(
				"cannot lookup interface by a single int id yet!");
	}

	public OnmsSnmpInterface load(Long id) {
		throw new RuntimeException(
				"cannot lookup interface by a single int id yet!");
	}

	public void save(OnmsSnmpInterface snmpIface) {
		if (exists(snmpIface))
			throw new IllegalArgumentException(
					"cannot save snmpinterfce that already exist in the db");

		doSave(snmpIface);
	}

	public void saveIfsForNode(OnmsNode node) {
		for (Iterator it = node.getSnmpInterfaces().iterator(); it.hasNext();) {
			OnmsSnmpInterface iface = (OnmsSnmpInterface) it.next();
			doSave(iface);
		}
	}

	public void saveOrUpdate(OnmsSnmpInterface svc) {
		if (exists(svc)) {
			doUpdate(svc);
		} else {
			doSave(svc);
		}
	}

	public void saveOrUpdateIfsForNode(OnmsNode node) {
		Set snmpInterfaces = node.getSnmpInterfaces();
		if (!isDirty(snmpInterfaces)) return;
		if (snmpInterfaces instanceof JdbcSet) {
			updateSetMembers((JdbcSet) snmpInterfaces);
		} else {
			removeAndAddSet(node);
		}
	}

	public void update(OnmsSnmpInterface svc) {

		if (!exists(svc))
			throw new IllegalArgumentException(
					"cannot updates svcs that are already in the db");

		doUpdate(svc);

	}

	private void cascadeSaveAssociations(OnmsSnmpInterface iface) {
	}

	private void cascadeUpdateAssociations(OnmsSnmpInterface iface) {
	}

	private void doSave(OnmsSnmpInterface iface) {
		getJdbcTemplate()
				.update(
						"insert into snmpinterface"
								+ " (snmpipadentnetmask, snmpphysaddr, snmpifdescr"
								+ ", snmpiftype, snmpifname, snmpifspeed "
								+ ", snmpifadminstatus, snmpifoperstatus, snmpifalias"
								+ ", nodeid, ipaddr, snmpifindex) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						new Object[] { iface.getNetMask(), iface.getPhysAddr(),
								iface.getIfDescr(), iface.getIfType(),
								iface.getIfName(), iface.getIfSpeed(),
								iface.getIfAdminStatus(),
								iface.getIfOperStatus(), iface.getIfAlias(),
								iface.getNode().getId(), iface.getIpAddress(),
								iface.getIfIndex(), });
		cascadeSaveAssociations(iface);
	}

	private void doUpdate(OnmsSnmpInterface iface) {

		// THIS SUCKS! Muck around with the statment to account for partially
		// null keys. BYUCK!

		if (isDirty(iface)) {
			String updateStmt = ""
					+ "update snmpinterface"
					+ "   set   snmpipadentnetmask = ?"
					+ "       , snmpphysaddr = ?"
					+ "       , snmpifdescr = ?"
					+ "       , snmpiftype = ?"
					+ "       , snmpifname = ?"
					+ "       , snmpifspeed"
					+ "       , snmpifadminstatus"
					+ "       , snmpifoperstatus"
					+ "       , snmpifalias"
					+ " where nodeid = ?"
					+ "   and ipaddr = ? and "
					+ (iface.getIfIndex() == null ? "snmpifindex is null"
							: "ifIndex = ?");

			// now to construct the correct array

			// all but the ifIndex
			Object[] parms = new Object[] { iface.getNetMask(),
					iface.getPhysAddr(), iface.getIfDescr(), iface.getIfType(),
					iface.getIfName(), iface.getIfSpeed(),
					iface.getIfAdminStatus(), iface.getIfOperStatus(),
					iface.getIfAlias(), iface.getNode().getId(),
					iface.getIpAddress(), };

			if (iface.getIfIndex() != null) {
				List parmList = new ArrayList(Arrays.asList(parms));
				parmList.add(iface.getIfIndex());
				parms = parmList.toArray();
			}

			getJdbcTemplate().update(updateStmt, parms);
		}

		cascadeUpdateAssociations(iface);
	}

	private boolean isDirty(OnmsSnmpInterface iface) {
		if (iface instanceof LazySnmpInterface) {
			LazySnmpInterface lazyIface = (LazySnmpInterface) iface;
			return lazyIface.isDirty();
		}
		return true;
	}

	private boolean exists(OnmsSnmpInterface iface) {

		// SAME CRAP HERE
		String query = "select count(*) from snmpinterface where nodeid = ? and ipaddr = ? and "
				+ (iface.getIfIndex() == null ? "snmpifindex is null"
						: "snmpifindex = ?");

		Object[] parms = new Object[] { iface.getNode().getId(),
				iface.getIpAddress(), };

		if (iface.getIfIndex() != null) {
			List parmList = new ArrayList(Arrays.asList(parms));
			parmList.add(iface.getIfIndex());
			parms = parmList.toArray();
		}
		int count = getJdbcTemplate().queryForInt(query, parms);
		return count > 0;
	}

	private void removeAndAddSet(OnmsNode node) {
		deleteInterfacesForNode(node);
		saveIfsForNode(node);
	}

	private void updateSetMembers(JdbcSet set) {
		for (Iterator it = set.getRemoved().iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIf = (OnmsSnmpInterface) it.next();
			delete(snmpIf);
		}

		for (Iterator it = set.getRemaining().iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIf = (OnmsSnmpInterface) it.next();
			doUpdate(snmpIf);
		}

		for (Iterator it = set.getAdded().iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIf = (OnmsSnmpInterface) it.next();
			doSave(snmpIf);

		}

		set.reset();
	}

}

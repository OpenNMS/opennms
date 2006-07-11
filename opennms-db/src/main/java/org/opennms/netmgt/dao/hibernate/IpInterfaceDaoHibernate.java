//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
/**
 * @author david
 *
 */
public class IpInterfaceDaoHibernate extends AbstractDaoHibernate  implements IpInterfaceDao {

    public OnmsIpInterface load(Long id) {
        return (OnmsIpInterface)getHibernateTemplate().load(OnmsIpInterface.class, id);
    }

    public OnmsIpInterface get(Long id) {
        return (OnmsIpInterface)getHibernateTemplate().get(OnmsIpInterface.class, id);
    }

	public OnmsIpInterface get(OnmsNode node, String ipAddress) {
		// TODO: Implement this!
		throw new RuntimeException("Not implemented!");
	}

    public void save(OnmsIpInterface iface) {
        getHibernateTemplate().save(iface);
    }

    public void update(OnmsIpInterface iface) {
        getHibernateTemplate().update(iface);
    }

    public Collection findAll() {
        return getHibernateTemplate().loadAll(OnmsIpInterface.class);
    }

    public Collection findByIpAddress(String ipAddress) {
        return getHibernateTemplate().find("from OnmsIpInterface ipIf where ipIf.ipAddress = ?", ipAddress);
    }

    public int countAll() {
        return ((Integer)findUnique("select count(*) from OnmsIpInterface")).intValue();
    }

	public Collection findByServiceType(String svcName) {
		// TODO: Implement this!
		throw new RuntimeException("Not implemented!");
	}

}

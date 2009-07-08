/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.util.Assert;

public class SnmpInterfaceDaoHibernate extends
		AbstractDaoHibernate<OnmsSnmpInterface, Integer> implements
		SnmpInterfaceDao {

	public SnmpInterfaceDaoHibernate() {
		super(OnmsSnmpInterface.class);
	}
	


    public OnmsSnmpInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifIndex) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select distinct snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ? and snmpIf.ifIndex = ?", 
                          nodeId, 
                          ifIndex);
        
    }



    public OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex) {
        Assert.notNull(foreignSource, "foreignSource may not be null");
        Assert.notNull(foreignId, "foreignId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select distinct snmpIf from OnmsSnmpInterface as snmpIf join snmpIf.node as node where node.foreignSource = ? and node.foreignId = ? and snmpIf.ifIndex = ?", 
                          foreignSource, 
                          foreignId, 
                          ifIndex);
    }

	

}

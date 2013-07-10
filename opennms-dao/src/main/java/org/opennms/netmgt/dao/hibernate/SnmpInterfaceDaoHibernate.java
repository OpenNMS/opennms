/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.util.Assert;

public class SnmpInterfaceDaoHibernate extends
		AbstractDaoHibernate<OnmsSnmpInterface, Integer> implements
		SnmpInterfaceDao {

	/**
	 * <p>Constructor for SnmpInterfaceDaoHibernate.</p>
	 */
	public SnmpInterfaceDaoHibernate() {
		super(OnmsSnmpInterface.class);
	}
	


    /** {@inheritDoc} */
        @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifIndex) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select distinct snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ? and snmpIf.ifIndex = ?", 
                          nodeId, 
                          ifIndex);
        
    }



    /** {@inheritDoc} */
        @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex) {
        Assert.notNull(foreignSource, "foreignSource may not be null");
        Assert.notNull(foreignId, "foreignId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select distinct snmpIf from OnmsSnmpInterface as snmpIf join snmpIf.node as node where node.foreignSource = ? and node.foreignId = ? and node.type = 'A' and snmpIf.ifIndex = ?", 
                          foreignSource, 
                          foreignId, 
                          ifIndex);
    }

	

}

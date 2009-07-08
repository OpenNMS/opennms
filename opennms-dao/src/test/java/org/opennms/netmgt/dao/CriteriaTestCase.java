/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.dao;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;

public class CriteriaTestCase extends AbstractTransactionalDaoTestCase {

    public void testSimple() {
        OnmsCriteria crit = new OnmsCriteria(OnmsNode.class);
        crit.add(Restrictions.eq("label", "node1"));
        
        Collection<OnmsNode> matching = getNodeDao().findMatching(crit);
        
        assertEquals("Expect a single node with label node1", 1, matching.size());
        
        OnmsNode node = matching.iterator().next();
        assertEquals("node1", node.getLabel());
        assertEquals(3, node.getIpInterfaces().size());
    }
    
    public void testComplicated() {
        OnmsCriteria crit = 
            new OnmsCriteria(OnmsNode.class)
            .createAlias("ipInterfaces", "iface")
            .add(Restrictions.eq("iface.ipAddress", "192.168.2.1"));
        
        Collection<OnmsNode> matching = getNodeDao().findMatching(crit);
        
        assertEquals("Expect a single node with an interface 192.168.2.1", 1, matching.size());
        
        OnmsNode node = matching.iterator().next();
        assertEquals("node2", node.getLabel());
        assertEquals(3, node.getIpInterfaces().size());
            
    }
}

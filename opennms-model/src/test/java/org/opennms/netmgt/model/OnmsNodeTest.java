/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic unit tests for OnmsNode Class
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class OnmsNodeTest {

    /*
     * Test the equals method of the PrimaryType class and therefore the getPrimaryInterface method
     * of the node.
     */
    @Test
    public void testGetPrimaryInterface() {
        
        OnmsNode node = new OnmsNode();
        OnmsIpInterface iface = new OnmsIpInterface();
        PrimaryType primary1 = PrimaryType.PRIMARY;
        iface.setIsSnmpPrimary(primary1);
        node.addIpInterface(iface);
        
        OnmsIpInterface iface2 = new OnmsIpInterface();
        PrimaryType not_eligible1 = PrimaryType.NOT_ELIGIBLE;
        iface2.setIsSnmpPrimary(not_eligible1);
        node.addIpInterface(iface2);
        
        
        Object o = node.getPrimaryInterface();
        
        Assert.assertSame(o, iface);
        
    }

}

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: June 19, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.model;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;

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

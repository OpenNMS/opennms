/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import junit.framework.Assert;

import org.junit.Test;

public class OnmsHwEntityTest {

    @Test
    public void testEquals() {
        OnmsHwEntity r1 = createEntity("Chassis", "Processor", "Module", "CPU", "Memory");
        OnmsHwEntity r2 = createEntity("Chassis", "I/O", "Module");
        OnmsHwEntity r3 = createEntity("Chassis", "Processor", "Module", "CPU", "Memory");
        OnmsHwEntity r4 = createEntity("Chassis", "Processor", "Module", "CPU");
        Assert.assertTrue(r1.equals(r3));
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r1.equals(r4));
        Assert.assertFalse(r1.equals(null));
    }

    private OnmsHwEntity createEntity(String rootName, String childName1, String childName2, String ... attributes) {
        OnmsHwEntity r1 = new OnmsHwEntity();
        r1.setId(getRandomId());
        r1.setEntPhysicalIndex(1);
        r1.setEntPhysicalName(rootName);

        OnmsHwEntity c1 = new OnmsHwEntity();
        c1.setId(getRandomId());
        c1.setEntPhysicalIndex(2);
        c1.setEntPhysicalName(childName1);

        OnmsHwEntity c2 = new OnmsHwEntity();
        c2.setId(getRandomId());
        c2.setEntPhysicalIndex(3);
        c2.setEntPhysicalName(childName2);

        for (int i = 0; i< attributes.length; i++) {
            HwEntityAttributeType a = new HwEntityAttributeType(".1.1.1." + i, attributes[i], "integer");
            a.setId(getRandomId());
            c1.addAttribute(a, Integer.toString(i + 10));
        }

        r1.addChildEntity(c1);
        r1.addChildEntity(c2);

        OnmsNode n = new OnmsNode();
        n.setId(1);
        n.setLabel("n1");
        r1.setNode(n);

        return r1;
    }

    public int getRandomId() {
        return 10 + (int)(Math.random()*100); 
    }
}

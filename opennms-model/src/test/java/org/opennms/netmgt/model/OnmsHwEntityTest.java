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
import org.opennms.core.xml.JaxbUtils;

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

    @Test
    public void testTypes() {
        HwEntityAttributeType cpu = new HwEntityAttributeType(".1.1.1.1", "cpu", "integer");
        HwEntityAttributeType mem = new HwEntityAttributeType(".1.1.1.2", "mem", "integer");

        OnmsHwEntity e = new OnmsHwEntity();
        e.setId(1);
        e.setEntPhysicalIndex(1);
        e.setEntPhysicalName("Chassis");
        e.addAttribute(cpu, "2");
        e.addAttribute(mem, "128");

        OnmsHwEntity c = new OnmsHwEntity();
        c.setId(2);
        c.setEntPhysicalIndex(2);
        c.setEntPhysicalName("Module");
        c.addAttribute(cpu, "1");
        c.addAttribute(mem, "64");
        e.addChildEntity(c);

        OnmsNode n = new OnmsNode();
        n.setId(1);
        n.setLabel("n1");
        e.setNode(n);

        String xml = JaxbUtils.marshal(e);

        OnmsHwEntity h = JaxbUtils.unmarshal(OnmsHwEntity.class, xml);
        h.fixRelationships();

        Assert.assertNotNull(h);
        Assert.assertEquals(1,  h.getChildren().size());
        checkAttributes(h);
        checkAttributes(h.getChildren().get(0));

        Assert.assertEquals(e, h);
    }

    private void checkAttributes(OnmsHwEntity h) {
        for (OnmsHwEntityAttribute a : h.getHwEntityAttributes()) {
            Assert.assertNotNull(a.getHwEntity());
            Assert.assertNotNull(a.getType());
        }
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

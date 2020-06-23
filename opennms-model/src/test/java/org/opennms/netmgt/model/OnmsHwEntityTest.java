/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import org.opennms.core.xml.JaxbUtils;

/**
 * The Class OnmsHwEntityTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class OnmsHwEntityTest {

    /**
     * Test equals.
     */
    @Test
    public void testEquals() {
        OnmsHwEntity r1 = createEntity("Chassis", new String[] {"Processor", "Module"}, new String[] {"CPU", "Memory"});
        OnmsHwEntity r2 = createEntity("Chassis", new String[] {"Processor", "Module"}, new String[] {"CPU", "Memory"});
        OnmsHwEntity r3 = createEntity("Chassis", new String[] {"Processor", "Module"},  new String[] {"CPU"});
        OnmsHwEntity r4 = createEntity("Chassis", new String[] {"I/O", "Module"}, new String[] {});
        Assert.assertFalse(r1.equals(null));
        Assert.assertFalse(r1.equals(r3));
        Assert.assertFalse(r1.equals(r4));
        Assert.assertTrue(r1.equals(r2));
    }

    /**
     * Test types.
     */
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
        checkAttributes(h.getChildren().iterator().next());

        Assert.assertEquals(e, h);
    }

    @Test
    public void testRemove() throws Exception {
        OnmsHwEntity e = new OnmsHwEntity();
        e.setId(1);
        e.setEntPhysicalIndex(1);
        e.setEntPhysicalName("Chassis");

        OnmsHwEntity c1 = new OnmsHwEntity();
        c1.setId(2);
        c1.setEntPhysicalIndex(2);
        c1.setEntPhysicalName("Module 1");
        e.addChildEntity(c1);

        OnmsHwEntity c2 = new OnmsHwEntity();
        c2.setId(3);
        c2.setEntPhysicalIndex(3);
        c2.setEntPhysicalName("Module 2");
        e.addChildEntity(c2);

        OnmsNode n = new OnmsNode();
        n.setId(1);
        n.setLabel("n1");
        e.setNode(n);
        
        e.removeChild(c2);
        Assert.assertEquals(1, e.getChildren().size());
    }

    /**
     * Check attributes.
     *
     * @param h the entity
     */
    private void checkAttributes(OnmsHwEntity h) {
        for (OnmsHwEntityAttribute a : h.getHwEntityAttributes()) {
            Assert.assertNotNull(a.getHwEntity());
            Assert.assertNotNull(a.getType());
        }
    }

    /**
     * Creates the entity.
     *
     * @param rootName the root name
     * @param childName1 the child name1
     * @param childName2 the child name2
     * @param attributes the attributes
     * @return the entity
     */
    private OnmsHwEntity createEntity(String rootName, String[] children, String[] attributes) {
        OnmsHwEntity r = new OnmsHwEntity();
        r.setId(getRandomId());
        r.setEntPhysicalIndex(1);
        r.setEntPhysicalName(rootName);

        int index = 2;
        for (String child : children) {
            OnmsHwEntity c = new OnmsHwEntity();
            c.setId(getRandomId());
            c.setEntPhysicalIndex(index++);
            c.setEntPhysicalName(child);
            r.addChildEntity(c);
        }

        for (int i = 0; i< attributes.length; i++) {
            HwEntityAttributeType a = new HwEntityAttributeType(".1.1.1." + i, attributes[i], "integer");
            a.setId(getRandomId());
            r.addAttribute(a, Integer.toString(i + 10));
        }

        OnmsNode n = new OnmsNode();
        n.setId(1);
        n.setLabel("n1");
        r.setNode(n);

        System.out.println(r);

        return r;
    }

    /**
     * Gets the random id.
     *
     * @return the random id
     */
    public int getRandomId() {
        return 10 + (int)(Math.random()*100); 
    }
}

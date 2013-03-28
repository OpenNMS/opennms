/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
import junit.framework.TestCase;

import org.junit.Test;

public class OnmsSeverityTest extends TestCase {

    @Test public void testGetId() {
        int id = OnmsSeverity.CLEARED.getId();
        OnmsSeverity sev = OnmsSeverity.CLEARED;
        Assert.assertEquals(id, sev.getId());
    }

    @Test public void testGetLabel() {
        String label = OnmsSeverity.CLEARED.getLabel();
        OnmsSeverity sev = OnmsSeverity.CLEARED;
        Assert.assertEquals(label, sev.getLabel());
    }

    @Test public void testIsLessThan() {
        OnmsSeverity major = OnmsSeverity.MAJOR;
        OnmsSeverity minor = OnmsSeverity.MINOR;
        Assert.assertTrue(minor.isLessThan(major));
    }

    @Test public void testIsLessThanOrEqual() {
        OnmsSeverity major = OnmsSeverity.MAJOR;
        OnmsSeverity minor = OnmsSeverity.MINOR;
        Assert.assertTrue(minor.isLessThanOrEqual(major));
    }

    @Test public void testIsGreaterThan() {
        OnmsSeverity major = OnmsSeverity.MAJOR;
        OnmsSeverity minor = OnmsSeverity.MINOR;
        Assert.assertTrue(major.isGreaterThan(minor));
    }

    @Test public void testIsGreaterThanOrEqual() {
        OnmsSeverity major = OnmsSeverity.MAJOR;
        OnmsSeverity minor = OnmsSeverity.MINOR;
        Assert.assertTrue(major.isGreaterThanOrEqual(minor));
    }

    @Test public void testEscalate() {
        OnmsSeverity major = OnmsSeverity.MAJOR;
        OnmsSeverity minor = OnmsSeverity.MINOR;
        minor = OnmsSeverity.escalate(minor);
        Assert.assertEquals(minor, major);
        minor = OnmsSeverity.escalate(minor);
        Assert.assertEquals(minor, OnmsSeverity.CRITICAL);
        OnmsSeverity same = OnmsSeverity.escalate(minor);
        Assert.assertSame(same, minor);
    }

}

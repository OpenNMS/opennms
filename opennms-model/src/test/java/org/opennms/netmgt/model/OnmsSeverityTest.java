/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import org.junit.Assert;
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

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
package org.opennms.netmgt.provision.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link NodeScan}.
 */
public class NodeScanTest {

    /**
     * A scheduled NodeScan instance is reused across repeated runs
     * (scheduleWithFixedDelay). The scan stamp drives obsolete-interface deletion, so
     * it must be refreshed for each run rather than left frozen at construction time;
     * otherwise a reused instance keeps comparing against a stale stamp and never
     * reaps interfaces that have gone away. See NMS-112 regression.
     */
    @Test
    public void refreshesScanStampOnEachRun() throws Exception {
        final NodeScan scan = new NodeScan(1, "fs", "fid", null, null, null, null, null, null, null, null);
        Assert.assertNotNull(scan.getScanStamp());

        // Simulate the stamp left behind by a prior run on the reused instance.
        final Date stale = new Date(scan.getScanStamp().getTime() - TimeUnit.HOURS.toMillis(1));
        final Field stampField = NodeScan.class.getDeclaredField("m_scanStamp");
        stampField.setAccessible(true);
        stampField.set(scan, stale);
        Assert.assertEquals(stale, scan.getScanStamp());

        final Method reset = NodeScan.class.getDeclaredMethod("reset");
        reset.setAccessible(true);
        reset.invoke(scan);

        Assert.assertTrue("reset() must advance the scan stamp past a stale prior-run value",
                scan.getScanStamp().after(stale));
    }
}

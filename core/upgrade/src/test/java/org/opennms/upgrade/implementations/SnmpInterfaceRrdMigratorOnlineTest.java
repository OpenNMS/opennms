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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.rrd.model.Row;
import org.opennms.netmgt.rrd.util.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.core.io.FileSystemResource;

/**
 * The Test Class for SnmpInterfaceRrdMigratorOnline.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class SnmpInterfaceRrdMigratorOnlineTest {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        FileUtils.copyDirectory(new File("src/test/resources/rrd"), new File("target/home/rrd"));
        FileUtils.copyDirectory(new File("src/test/resources/jetty-webapps/opennms/WEB-INF"), new File("target/home/jetty-webapps/opennms/WEB-INF/"));
        System.setProperty("opennms.home", "target/home");
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        dao.setConfigResource(new FileSystemResource(new File("target/home/etc/datacollection-config.xml")));
        dao.afterPropertiesSet();
        DataCollectionConfigFactory.setInstance(dao);
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    /**
     * Test upgrade.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgrade() throws Exception {
        // Check Current JRBs
        RRDv1 rrd = RrdConvertUtils.dumpJrb(new File("target/home/rrd/1/eth0/ifInOctets.jrb"));
        Row r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381500900l);
        Assert.assertNotNull(r);
        Assert.assertEquals(new Double(-6.0), r.getValues().get(0));
        r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381512300l);
        Assert.assertNull(r);

        rrd = RrdConvertUtils.dumpJrb(new File("target/home/rrd/1/eth0-005056c00008/ifInOctets.jrb"));
        r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381500900l);
        Assert.assertNotNull(r);
        Assert.assertTrue(r.getValues().get(0).isNaN());
        r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381512300l);
        Assert.assertNotNull(r);
        Assert.assertEquals(new Double(12.0), r.getValues().get(0));

        // Perform Migration
        SnmpInterfaceRrdMigratorOnline obj = new SnmpInterfaceRrdMigratorOnline() {
            protected List<SnmpInterfaceUpgrade> getInterfacesToMerge() throws OnmsUpgradeException {
                List<SnmpInterfaceUpgrade> interfaces = new ArrayList<>();
                interfaces.add(new SnmpInterfaceUpgrade(1, null, null, "eth0", "eth0", "005056c00008", false));
                return interfaces;
            }
        };
        obj.preExecute();
        obj.execute();
        obj.postExecute();

        // Verify if the backups have been removed
        Assert.assertFalse(new File("target/home/rrd/1/eth0.zip").exists());
        Assert.assertFalse(new File("target/home/rrd/1/eth0-005056c00008.zip").exists());

        // Check Merged JRB
        rrd = RrdConvertUtils.dumpJrb(new File("target/home/rrd/1/eth0-005056c00008/ifInOctets.jrb"));
        r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381500900l);
        Assert.assertNotNull(r);
        Assert.assertEquals(new Double(-6.0), r.getValues().get(0));
        r = rrd.findRowByTimestamp(rrd.getRras().get(0), 1381512300l);
        Assert.assertNotNull(r);
        Assert.assertEquals(new Double(12.0), r.getValues().get(0));
    }

}

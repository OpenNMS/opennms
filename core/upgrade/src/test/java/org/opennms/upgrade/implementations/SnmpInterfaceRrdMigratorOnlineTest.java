/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
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
                List<SnmpInterfaceUpgrade> interfaces = new ArrayList<SnmpInterfaceUpgrade>();
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

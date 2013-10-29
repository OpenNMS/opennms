/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.upgrade.implementations;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Test Class for JmxRrdMigratorOffline.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class JmxRrdMigratorOfflineTest {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        File homeDir = new File("target/home");
        File cfgDir = new File(homeDir, "etc");
        Assert.assertTrue(cfgDir.mkdirs());
        File rrdDir = new File(homeDir, "rrd");
        Assert.assertTrue(rrdDir.mkdirs());
        FileUtils.copyFile(new File("src/test/resources/version.properties"), new File(homeDir, "jetty-webapps/opennms/WEB-INF/version.properties"));
        FileUtils.copyFile(new File("src/test/resources/opennms-storeByGroup.properties"), new File(cfgDir, "opennms.properties"));
        FileUtils.copyFile(new File("src/test/resources/rrd-configuration.properties"), new File(cfgDir, "rrd-configuration.properties"));
        FileUtils.copyFile(new File("src/test/resources/opennms-server.xml"), new File(cfgDir, "opennms-server.xml"));
        FileUtils.copyFile(new File("src/test/resources/collectd-configuration.xml"), new File(cfgDir, "collectd-configuration.xml"));
        FileUtils.copyFile(new File("src/test/resources/jmx-datacollection-config.xml"), new File(cfgDir, "jmx-datacollection-config.xml"));
        FileUtils.copyFile(new File("src/test/resources/jvm-graph.properties"), new File(cfgDir, "snmp-graph.properties.d/jvm-graph.properties"));
        FileUtils.copyFile(new File("src/test/resources/snmp-graph.properties"), new File(cfgDir, "snmp-graph.properties"));
        // FIXME RRDs ?
        // I need a multi-ds JRB with bad DS and invalid name (spaces, quotes, etc.)
        // I need a ds.properties with bad DS
        // I need a .meta file with bad DS
        System.setProperty("opennms.home", homeDir.getCanonicalPath());
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
        JmxRrdMigratorOffline obj = new JmxRrdMigratorOffline();
        obj.preExecute();
        obj.execute();
        obj.postExecute();
        Assert.assertEquals(60, obj.badMetrics.size());
        // FIXME Verify updated configuration files.
        // FIXME Verify updated JRBs.
    }
}

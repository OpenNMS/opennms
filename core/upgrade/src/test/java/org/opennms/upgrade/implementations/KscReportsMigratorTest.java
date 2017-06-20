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
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Test Class for KscReportsMigrator.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class KscReportsMigratorTest {

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
        KSC_PerformanceReportFactory.init();
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
        KscReportsMigrator obj = new KscReportsMigrator() {
            protected void initializeDatasource() throws OnmsUpgradeException {
            }
            protected List<SnmpInterface> getInterfacesToMerge() throws OnmsUpgradeException {
                List<SnmpInterface> interfaces = new ArrayList<SnmpInterface>();
                interfaces.add(new SnmpInterface(1, null, null, "eth0", "eth0", "005056c00008", false));
                interfaces.add(new SnmpInterface(1, null, null, "eth1", "eth1", "005056c00009", false));
                return interfaces;
            }
        };
        Assert.assertTrue(KSC_PerformanceReportFactory.getInstance().getReportByIndex(1).getGraphs().get(0).getResourceId().isPresent());
        Assert.assertEquals("node[1].interfaceSnmp[eth0]", KSC_PerformanceReportFactory.getInstance().getReportByIndex(1).getGraphs().get(0).getResourceId().orElse(null));
        try {
            obj.preExecute();
            obj.execute();
            obj.postExecute();
        } catch (OnmsUpgradeException e) {
            obj.rollback();
            Assert.fail();
        }
        Assert.assertTrue(KSC_PerformanceReportFactory.getInstance().getReportByIndex(1).getGraphs().get(0).getResourceId().isPresent());
        Assert.assertEquals("node[1].interfaceSnmp[eth0-005056c00008]", KSC_PerformanceReportFactory.getInstance().getReportByIndex(1).getGraphs().get(0).getResourceId().orElse(null));
    }

    /**
     * Test upgrade.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRollback() throws Exception {
        KscReportsMigrator obj = new KscReportsMigrator() {
            protected List<SnmpInterface> getInterfacesToMerge() throws OnmsUpgradeException {
                throw new OnmsUpgradeException("Forcing rollback");
            }
        };
        try {
            obj.preExecute();
            obj.execute();
            obj.postExecute();
        } catch (OnmsUpgradeException e) {
            obj.rollback();
            return;
        }
        Assert.fail();
    }

}

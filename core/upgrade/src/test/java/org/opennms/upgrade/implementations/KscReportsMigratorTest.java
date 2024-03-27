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
                List<SnmpInterface> interfaces = new ArrayList<>();
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

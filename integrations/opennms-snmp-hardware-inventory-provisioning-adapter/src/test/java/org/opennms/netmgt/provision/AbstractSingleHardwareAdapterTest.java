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
package org.opennms.netmgt.provision;

import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationSchedule;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

/**
 * The Abstract Class AbstractSingleHardwareAdapterTest
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public abstract class AbstractSingleHardwareAdapterTest implements InitializingBean {

    /** The SNMP Hardware Provisioning Adapter. */
    @Autowired
    private SnmpHardwareInventoryProvisioningAdapter m_adapter;

    /** The node DAO. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The entity DAO. */
    @Autowired
    private HwEntityDao m_entityDao;

    /** The Node id. */
    private Integer m_nodeId;

    /** The adapter operation. */
    private AdapterOperation m_operation;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @BeforeTransaction
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);

        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("Test").setForeignSource("Test").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.9.1.1196");
        nb.addInterface("192.168.0.1").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        m_adapter.afterPropertiesSet();

        m_nodeId = m_nodeDao.findByForeignId("Test", "1").getId();
        AdapterOperationSchedule ops = new AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS);
        m_operation = m_adapter.new AdapterOperation(m_nodeId, AdapterOperationType.ADD, ops);
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @AfterTransaction
    public void tearDown() throws Exception{
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test adapter.
     *
     * @throws Exception the exception
     */
    public abstract void testAdapter() throws Exception;

    /**
     * Perform test.
     *
     * @param expectedCount the expected count
     * @throws Exception the exception
     */
    protected void performTest(int expectedCount) throws Exception {
        m_adapter.processPendingOperationForNode(m_operation);

        OnmsHwEntity root = m_entityDao.findRootByNodeId(m_nodeId);
        Assert.assertNotNull(root);
        Assert.assertTrue(root.isRoot());
        FileWriter w = new FileWriter("target/" + m_nodeId + ".xml");
        JaxbUtils.marshal(root, w);
        w.close();

        m_nodeDao.flush();
        m_entityDao.flush();

        Assert.assertEquals(expectedCount, m_entityDao.countAll());
    }

}

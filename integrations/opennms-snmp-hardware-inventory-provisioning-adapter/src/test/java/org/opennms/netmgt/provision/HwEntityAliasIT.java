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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.provision.SnmpHardwareInventoryProvisioningAdapter;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationSchedule;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
/**
 * The Test Class for SnmpHardwareInventoryProvisioningAdapter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host = "192.168.0.1", resource = "NMS-8506-cisco.properties")
})
public class HwEntityAliasIT implements InitializingBean {

    /**
     * The Class TestOperation.
     */
    public final static class TestOperation {

        /** The node id. */
        public Integer nodeId;

        /** The operation. */
        public AdapterOperation operation;

        /**
         * The Constructor.
         *
         * @param nodeId the node id
         * @param operation the operation
         */
        public TestOperation(Integer nodeId, AdapterOperation operation) {
            super();
            this.nodeId = nodeId;
            this.operation = operation;
        }
    }

    /** The SNMP Hardware Provisioning Adapter. */
    @Autowired
    private SnmpHardwareInventoryProvisioningAdapter m_adapter;

    /** The node DAO. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The entity DAO. */
    @Autowired
    private HwEntityDao m_entityDao;

    /** The operation. */
    private TestOperation testOperation;

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

        nb.addNode("R1").setForeignSource("Cisco").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.1").setIsSnmpPrimary("P").setIsManaged("P");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

        m_adapter.afterPropertiesSet();

        Integer nodeId = m_nodeDao.findByForeignId("Cisco", Integer.toString(1)).getId();
        AdapterOperationSchedule ops = new AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS);
        AdapterOperation adapterOperation = m_adapter.new AdapterOperation(nodeId, AdapterOperationType.ADD, ops);
        testOperation = new TestOperation(nodeId, adapterOperation);
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
     * Test discover SNMP entities.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testDiscoverSnmpEntities() throws Exception {
        m_adapter.processPendingOperationForNode(testOperation.operation);

        OnmsHwEntity root = m_entityDao.findRootByNodeId(testOperation.nodeId);
        assertThat(root, is(notNullValue()));
        assertThat(root.isRoot(), is(true));

        m_nodeDao.flush();
        m_entityDao.flush();

        List<OnmsHwEntityAlias> aliases = new ArrayList<>();
        for (OnmsHwEntity entity : m_entityDao.findAll()) {
            Set<OnmsHwEntityAlias> entAliases = entity.getEntAliases();
            if (entAliases != null && !entAliases.isEmpty()) {
                aliases.addAll(entAliases);
            }
        }

        assertThat(aliases, hasSize(4));

        List<String> aliasOids = aliases.stream().map(a -> a.getOid()).collect(Collectors.toList());
        assertThat(aliasOids, hasItem(".1.3.6.1.2.1.2.2.1.1.10101"));
        assertThat(aliasOids, hasItem(".1.3.6.1.2.1.2.2.1.1.10102"));
        assertThat(aliasOids, hasItem(".1.3.6.1.2.1.2.2.1.1.10104"));
        assertThat(aliasOids, hasItem(".1.3.6.1.2.1.2.2.1.1.10502"));
    }

}

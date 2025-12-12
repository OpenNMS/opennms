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
package org.opennms.netmgt.collectd.tca;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collectd.DefaultSnmpCollectionAgent;
import org.opennms.netmgt.collectd.tca.config.TcaDataCollectionConfig;
import org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.ImmutableMap;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/junit-component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitSnmpAgent(host = TcaCollectorIT.TEST_NODE_IP, port = 9161, resource = "classpath:juniperTcaSample.properties")
public class TcaCollectorComplianceIT extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    @Autowired
    private LocationAwareSnmpClient m_client;

    public TcaCollectorComplianceIT() {
        super(TcaCollector.class, false);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .build();
    }

    @Override
    public CollectionAgent createAgent(Integer ifaceId, IpInterfaceDao ifaceDao, PlatformTransactionManager transMgr) {
        return DefaultSnmpCollectionAgent.create(ifaceId, ifaceDao, transMgr);
    };

    @Override
    public Map<String, Object> getRequiredBeans() {
        TcaDataCollectionConfig config = mock(TcaDataCollectionConfig.class);
        RrdRepository rrdRepository = new RrdRepository();
        rrdRepository.setRrdBaseDir(new File("target"));
        when(config.buildRrdRepository(COLLECTION)).thenReturn(rrdRepository);

        TcaDataCollectionConfigDao tcaDataCollectionConfigDao = mock(TcaDataCollectionConfigDao.class);
        when(tcaDataCollectionConfigDao.getConfig()).thenReturn(config);
        ResourceStorageDao resourceStorageDao = mock(ResourceStorageDao.class);
        ResourceTypesDao resourceTypesDao = mock(ResourceTypesDao.class);

        ResourceType resourceType = TcaCollectorIT.getJuniperTcaEntryResourceType();
        when(resourceTypesDao.getResourceTypeByName(TcaCollectionHandler.RESOURCE_TYPE_NAME)).thenReturn(resourceType);

        BlobStore blobStore = mock(BlobStore.class);

        return new ImmutableMap.Builder<String, Object>()
                .put("tcaDataCollectionConfigDao", tcaDataCollectionConfigDao)
                .put("resourceStorageDao", resourceStorageDao)
                .put("resourceTypesDao", resourceTypesDao)
                .put("locationAwareSnmpClient", m_client)
                .put("blobStore", blobStore)
                .build();
    }
}

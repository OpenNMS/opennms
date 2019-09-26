/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
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
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/junit-component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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

        return new ImmutableMap.Builder<String, Object>()
                .put("tcaDataCollectionConfigDao", tcaDataCollectionConfigDao)
                .put("resourceStorageDao", resourceStorageDao)
                .put("resourceTypesDao", resourceTypesDao)
                .put("locationAwareSnmpClient", m_client)
                .build();
    }
}

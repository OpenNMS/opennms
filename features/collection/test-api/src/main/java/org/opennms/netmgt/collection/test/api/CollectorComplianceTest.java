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

package org.opennms.netmgt.collection.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Used to verify that a {@link ServiceCollector} behaves
 * correctly when used in different workflows i.e.:
 *  1) Ad-hoc collection via the console
 *  2) Collection from OpenNMS via Collectd
 *  3) Collection from Minion via an RPC triggered by Collectd
 *
 * @author jwhite
 */
public abstract class CollectorComplianceTest {

    private final Class<? extends ServiceCollector> collectorClass;
    private final boolean runsOnMinion;

    private ServiceCollectorRegistry serviceCollectorRegistry = new DefaultServiceCollectorRegistry();

    public CollectorComplianceTest(Class<? extends ServiceCollector> collectorClass, boolean runsOnMinion) {
        this.collectorClass = collectorClass;
        this.runsOnMinion = runsOnMinion;
    }

    public abstract String getCollectionName();

    public abstract Map<String, Object> getRequiredParameters();

    public CollectionAgent createAgent(Integer ifaceId, IpInterfaceDao ifaceDao, PlatformTransactionManager transMgr) {
        return DefaultCollectionAgent.create(ifaceId, ifaceDao, transMgr);
    };

    public void beforeMinion() { }

    public void afterMinion() { }

    public Map<String, Object> getRequiredBeans() {
        return Collections.emptyMap();
    }

    @Test
    public void isAvailableInDefaultRegistry() {
        assertNotNull(collectorClass.getCanonicalName() + " was not found in the default registry.", getCollector());
    }

    @Test
    public void canInitializeManyTimes() throws CollectionInitializationException {
        final ServiceCollector collector = getCollector();
        initialize(collector);
        initialize(collector);
        initialize(collector);
    }

    @Test
    public void canCollectUsingOpenNMSWorkflow() throws CollectionInitializationException, CollectionException {
        // create the agent
        OnmsNode node = mock(OnmsNode.class);
        OnmsIpInterface iface = mock(OnmsIpInterface.class);
        when(iface.getNode()).thenReturn(node);
        when(iface.getIpAddress()).thenReturn(InetAddrUtils.getLocalHostAddress());

        IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        when(ifaceDao.load(1)).thenReturn(iface);
        PlatformTransactionManager transMgr = mock(PlatformTransactionManager.class);
        final CollectionAgent agent = createAgent(1, ifaceDao, transMgr);

        // init() should execute without any exceptions
        final ServiceCollector opennmsCollector = getCollector();
        initialize(opennmsCollector);

        // getEffectiveLocation() should execute without any exceptions
        // in this context there are no requirements on its return value
        final String targetLocation = "!" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        opennmsCollector.getEffectiveLocation(targetLocation);

        // getRuntimeAttributes() should return a valid map
        final Map<String, Object> requiredParams = getRequiredParameters();
        final Map<String, Object> runtimeAttrs = opennmsCollector.getRuntimeAttributes(agent, Collections.unmodifiableMap(requiredParams));

        // collect() should return a valid collection set
        final Map<String, Object> allParms = new HashMap<>();
        allParms.putAll(requiredParams);
        allParms.putAll(runtimeAttrs);
        final CollectionSet collectionSet = opennmsCollector.collect(agent, Collections.unmodifiableMap(allParms));
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        // getRrdRepository() should return a valid repository
        assertNotNull(opennmsCollector.getRrdRepository(getCollectionName()));
    }

    @Test
    public void canCollectUsingMinionWorkflow() throws CollectionInitializationException, CollectionException {
        Assume.assumeTrue(runsOnMinion);

        // create the agent
        OnmsNode node = mock(OnmsNode.class);
        when(node.getId()).thenReturn(1);
        OnmsIpInterface iface = mock(OnmsIpInterface.class);
        when(iface.getNode()).thenReturn(node);
        when(iface.getIpAddress()).thenReturn(InetAddrUtils.getLocalHostAddress());

        IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        when(ifaceDao.load(1)).thenReturn(iface);
        PlatformTransactionManager transMgr = mock(PlatformTransactionManager.class);
        final CollectionAgent agent = DefaultCollectionAgent.create(1, ifaceDao, transMgr);

        // init() should execute without any exceptions
        final ServiceCollector opennmsCollector = getCollector();
        initialize(opennmsCollector);

        // getEffectiveLocation() should return the original location
        final String targetLocation = "!" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        assertEquals("Location cannot be altered.", targetLocation, opennmsCollector.getEffectiveLocation(targetLocation));

        // getRuntimeAttributes() should return a valid map
        final Map<String, Object> requiredParams = getRequiredParameters();
        final Map<String, Object> runtimeAttrs = opennmsCollector.getRuntimeAttributes(agent, Collections.unmodifiableMap(requiredParams));

        // marshalParameters() should marshal all parameters to strings
        final Map<String, Object> allParms = new HashMap<>();
        allParms.putAll(requiredParams);
        allParms.putAll(runtimeAttrs);
        final Map<String, String> marshaledParms = opennmsCollector.marshalParameters(Collections.unmodifiableMap(allParms));

        beforeMinion();

        // create a separate instance of the collector
        final ServiceCollector minionCollector = getNewCollector();

        // unmarshalParameters() should unmarshal all parameters from strings
        final Map<String, Object> unmarshaledParms = minionCollector.unmarshalParameters(Collections.unmodifiableMap(marshaledParms));

        // collect() should return a valid collection set
        final CollectionAgentDTO agentDTO = new CollectionAgentDTO(agent);
        final CollectionSet collectionSet = minionCollector.collect(agentDTO, Collections.unmodifiableMap(unmarshaledParms));
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        afterMinion();

        // the collection set should be marshalable
        JaxbUtils.marshal(collectionSet);

        // getRrdRepository() should return a valid repository
        assertNotNull(opennmsCollector.getRrdRepository(getCollectionName()));
    }

    private ServiceCollector getCollector() {
        return serviceCollectorRegistry.getCollectorByClassName(collectorClass.getCanonicalName());
    }

    private ServiceCollector getNewCollector() {
        try {
            return collectorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize(ServiceCollector collector) throws CollectionInitializationException {
        ApplicationContext context = mock(ApplicationContext.class);
        getRequiredBeans().forEach((k,v) -> {
            when(context.getBean(k)).thenReturn(v);
        });
        BeanUtils.setStaticApplicationContext(context);
        collector.initialize();
        BeanUtils.setStaticApplicationContext(null);
    }
}

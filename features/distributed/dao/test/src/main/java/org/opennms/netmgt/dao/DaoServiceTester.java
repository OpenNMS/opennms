/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeLabel;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.api.SessionFactoryWrapper;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.StatisticsService;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.io.ByteStreams;

/**
 * All exposed DAOs from features/distributed/dao-impl should be loadable within a Sentinel Container.
 * While the bundle starts it is not guaranteed that this is actually the case.
 * In order to ensure the loading of the DAOs this class loads all exposed services and performs some tests.
 * The goal is not to cover ALL possibilities, but to ensure that the DAOs can at least be loaded.
 * Further tests may be necessary.
 *
 * @author mvrueden
 */
public class DaoServiceTester {

    private static final Logger LOG = LoggerFactory.getLogger(DaoServiceTester.class);

    private final static Pattern SERVICE_PATTERN = Pattern.compile("<onmsgi:service.*interface[\\s]*=[\\s]*\"([\\w\\d\\.-]*)\".*\\/>");

    private final BundleContext bundleContext;

    private final TestRegistry testRegistry = new TestRegistry()
            .withIgnoredClass(
                    SessionUtils.class,
                    SessionFactoryWrapper.class,
                    TransactionOperations.class,
                    PlatformTransactionManager.class,
                    AlarmEntityNotifier.class, // we skip testing this for now
                    TopologyEntityDao.class, // Hibernate cannot find the classes CdpLinkTopologyEntity and NodeTopologyEntity
                                             // Probably due to class loader issues. See NMS-10493 for more details
                    TopologyEntityCache.class // We don't need to test this, if the TopologyEntityDao is tested
            )
            .withTest(OnmsDao.class, dao -> dao.countAll())
            .withTest(BridgeTopologyService .class, bean -> {
                bean.load();
            })
            .withTest(AlarmRepository.class, bean -> {
                bean.unacknowledgeAll("ulf");
            })
            .withTest(IfLabel .class, bean -> {
                bean.getIfLabel(1, InetAddressUtils.addr("127.0.0.1"));
            })
            .withTest(InterfaceToNodeCache .class, bean -> {
                bean.dataSourceSync();
            })
            .withTest(FilterDao .class, bean -> {
                bean.getActiveIPAddressList("categoryName == 'Test'");
            })
            .withTest(GenericPersistenceAccessor .class, bean -> {
                bean.get(OnmsNode.class, 1);
            })
            .withTest(NodeDao .class, bean -> {
                bean.getDefaultFocusPoint();
            })
            .withTest(StatisticsService.class, bean -> {
                bean.getTotalCount(new CriteriaBuilder(OnmsNode.class).toCriteria());
            })
            .withTest(NodeLabel.class, bean -> {
                try {
                    bean.computeLabel(1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

    public DaoServiceTester(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public void verifyExposedDaos() throws Exception {
        // Load services to verify
        final List<Class> serviceTypes = loadExposedServices("/META-INF/opennms/applicationContext-shared.xml", "/META-INF/opennms/component-dao.xml");
        if (serviceTypes.isEmpty()) {
            throw new IllegalStateException("No exposed services found. This seems fishy. Bailing");
        }

        // Verify
        for (Class eachServiceType : serviceTypes) {
            if (!testRegistry.isIgnored(eachServiceType)) {
                LOG.info("Verifying service {} ...", eachServiceType);
                final Consumer consumer = testRegistry.getTest(eachServiceType);
                if (consumer == null) {
                    throw new IllegalStateException("No test for type " + eachServiceType + " was found. Bailing");
                }
                try (ServiceHolder serviceHolder = getService(eachServiceType)) {
                    final Object service = serviceHolder.getService();
                    consumer.accept(service);
                    testRegistry.markAsRun(eachServiceType);
                    LOG.info("Verifying service {}. OK", eachServiceType);
                }
            } else {
                LOG.info("Verifying service {}. SKIPPED.", eachServiceType);
            }
        }
        
        // Check that all services have been either ignored or tested
        // Verify all classes have been checked
        final List<Class> notTestedServices = serviceTypes.stream()
                .filter(e -> !testRegistry.isTested(e) && !testRegistry.isIgnored(e))
                .collect(Collectors.toList());
        
        if (!notTestedServices.isEmpty()) {
            notTestedServices.forEach(serviceType -> LOG.error("Service of type {} was not tested", serviceType));
            throw new IllegalStateException("Not all services have been tested. Bailing");
        }
    }

    private <T> ServiceHolder<T> getService(Class<T> serviceType) {
        final ServiceReference<T> serviceReference = bundleContext.getServiceReference(serviceType);
        if (serviceReference == null) {
            throw new IllegalStateException("Service of type '" + serviceType + "' not resolvable");
        }
        return new ServiceHolder(serviceReference);
    }

    private Bundle findDaoBundle() {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals("org.opennms.features.distributed.dao-impl")) {
                return b;
            }
        }
        throw new IllegalStateException("No bundle with symbolic name 'org.opennms.features.distributed.dao-impl' found");
    }

    private List<Class> loadExposedServices(String... locations) throws IOException, ClassNotFoundException {
        final Bundle daoBundle = findDaoBundle();
        final List<Class> entries = new ArrayList<>();
        for (String location : locations) {
            LOG.info("Bundle with id {} is used to load {}", daoBundle.getBundleId(), location);
            try (InputStream in = daoBundle.getResource(location).openStream()) {
                final byte[] bytes = ByteStreams.toByteArray(in);
                final String content = new String(bytes);

                final Matcher m = SERVICE_PATTERN.matcher(content);
                while (m.find()) {
                    if (m.groupCount() >= 1) {
                        final String group = m.group(1);
                        LOG.info("Found exposed service {}. Try loading it...", group);
                        final Class clazz = Class.forName(group);
                        entries.add(clazz);
                    }
                }
            }
        }
        return entries;
    }
}

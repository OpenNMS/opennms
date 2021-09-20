/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class BusinessServiceDaoIT { 

    @Autowired
    @Qualifier("bsmDatabasePopulator")
    private BsmDatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Autowired
    private BusinessServiceEdgeDao m_edgeDao;

    private HighestSeverityEntity m_highestSeverity;

    private IgnoreEntity m_ignore;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();
        m_highestSeverity = new HighestSeverityEntity();
        m_ignore = new IgnoreEntity();
    }

    @Test
    @Transactional
    public void canCreateReadUpdateAndDeleteBusinessServices() {
        final int ifServiceCount = m_monitoredServiceDao.countAll();

        // Initially there should be no business services
        assertEquals(0, m_businessServiceDao.countAll());

        // Create a business service
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("Web Servers")
                .addAttribute("dc", "RDU")
                .addReductionKey("TestReductionKeyA", new IdentityEntity())
                .addReductionKey("TestReductionKeyB", new IdentityEntity())
                .reduceFunction(m_highestSeverity)
                .toEntity();
        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Read a business service
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));
        assertEquals(2, m_businessServiceDao.get(bs.getId()).getReductionKeyEdges().size());

        // Update a business service
        bs.setName("Application Servers");
        bs.getAttributes().put("dc", "!RDU");
        bs.getAttributes().put("cd", "/");

        // Grab the first monitored service from node 1
        OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        bs.addIpServiceEdge(ipService, m_ignore);
        m_businessServiceDao.update(bs);
        m_businessServiceDao.flush();

        // Verify the update
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));

        // Delete
        m_businessServiceDao.delete(bs);
        m_businessServiceDao.flush();

        // There should be no business services after the delete
        assertEquals(0, m_businessServiceDao.countAll());

        // No if service should have been deleted
        assertEquals(ifServiceCount, m_monitoredServiceDao.countAll());
    }

    @Test
    @Transactional
    public void verifyBusinessServicesWithRelatedIpServicesAreDeletedOnCascade() throws InterruptedException {
        // Initially there should be no business services
        assertEquals("Check that there are no initial BusinessServices", 0, m_businessServiceDao.countAll());

        // Create a business service with an associated IP Service
        final BusinessServiceEntity bs = new BusinessServiceEntity();
        bs.setName("Mont Cascades");
        bs.setReductionFunction(m_highestSeverity);
        final OnmsNode node = m_databasePopulator.getNode1();
        final OnmsMonitoredService ipService = getMonitoredServiceFromNode1();
        bs.addIpServiceEdge(ipService, m_ignore);

        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // We should have a single business service with a single IP service associated
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_businessServiceDao.get(bs.getId()).getIpServiceEdges().size());
        assertNotNull(m_monitoredServiceDao.get(ipService.getId()));

        // Now delete the node
        m_nodeDao.delete(node);
        m_nodeDao.flush();

        // The business service should still be present, but the IP service should have been deleted by the foreign
        // key constraint. We have to clear the session, otherwise hibernate does not know about the node deletion
        m_businessServiceDao.clear();
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(0, m_businessServiceDao.get(bs.getId()).getIpServiceEdges().size());
    }

    /**
     * If we do not explicitly delete the map or reduce function it should be deleted if not referenced anymore.
     */
    @Test
    @Transactional
    public void verifyDeleteOnCascade() {
        BusinessServiceEntity child2 = new BusinessServiceEntityBuilder()
                .name("Child 2")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("some-key", new IdentityEntity())
                .toEntity();

        BusinessServiceEntity child1 = new BusinessServiceEntityBuilder()
                .name("Child 1")
                .reduceFunction(new HighestSeverityEntity())
                .addChildren(child2, new IdentityEntity())
                .toEntity();

        BusinessServiceEntity parent = new BusinessServiceEntityBuilder()
                .name("Parent Web Servers")
                .addAttribute("dc", "RDU")
                .addReductionKey("TestReductionKeyA", new IdentityEntity())
                .addReductionKey("TestReductionKeyB", new IdentityEntity())
                .addIpService(getMonitoredServiceFromNode1(), new IdentityEntity())
                .reduceFunction(m_highestSeverity)
                .addChildren(child1, new IdentityEntity())
                .toEntity();

        m_businessServiceDao.save(child2);
        m_businessServiceDao.save(child1);
        m_businessServiceDao.save(parent);
        m_businessServiceDao.flush();

        assertEquals(3, m_businessServiceDao.countAll());
        assertEquals(3, m_reductionFunctionDao.countAll());
        assertEquals(6, m_edgeDao.countAll());

        // Deletion of child does not delete the edges referencing to that child
        // remove all parent -> child associations manually
        BusinessServiceChildEdgeEntity parentToChild1Edge = parent.getChildEdges().iterator().next();
        parent.removeEdge(parentToChild1Edge);
        m_edgeDao.delete(parentToChild1Edge);
        m_businessServiceDao.delete(child1); // edges do not need to be deleted manually, deletes will be cascaded
        m_businessServiceDao.flush();
        assertEquals(2, m_businessServiceDao.countAll());
        assertEquals(2, m_reductionFunctionDao.countAll());
        assertEquals(4, m_edgeDao.countAll());

        // Deletion of parent should delete all references
        m_businessServiceDao.delete(parent);
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_reductionFunctionDao.countAll());
        assertEquals(1, m_edgeDao.countAll());

        // Deletion of Child 2 should also work
        m_businessServiceDao.delete(child2);
        assertEquals(0, m_businessServiceDao.countAll());
        assertEquals(0, m_reductionFunctionDao.countAll());
        assertEquals(0, m_edgeDao.countAll());
    }

    @Test
    @Transactional
    public void verifyDistinctObjectLoading() {
        BusinessServiceEntity entity = new BusinessServiceEntityBuilder()
                .name("Parent Web Servers")
                .addReductionKey("TestReductionKeyA", new IdentityEntity())
                .addReductionKey("TestReductionKeyB", new IdentityEntity())
                .addReductionKey("TestReductionKeyC", new IdentityEntity())
                .reduceFunction(m_highestSeverity)
                .toEntity();

        m_businessServiceDao.save(entity);
        m_businessServiceDao.flush();

        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(3, m_edgeDao.countAll());

        Criteria criteria = new CriteriaBuilder(BusinessServiceEntity.class).toCriteria();
        // verify that root entity is merged
        assertEquals(1, m_businessServiceDao.findMatching(criteria).size());
        // verify that countMatching also works
        assertEquals(1, m_businessServiceDao.countMatching(criteria));

    }

    @Test(expected=ConstraintViolationException.class)
    @Transactional
    public void verifyBeanValidation() {
        BusinessServiceEntity entity = new BusinessServiceEntityBuilder()
                .name("Some Custom Name")
                .addReductionKey("My Reduction Key", new IdentityEntity(), "so friendly")
                .addReductionKey("Another Reduction Key", new IdentityEntity(), Strings.padEnd("too", 30, 'o') + " friendly")
                .reduceFunction(m_highestSeverity)
                .toEntity();

        // Should throw a ConstraintViolationException (friendly name too long)
        m_businessServiceDao.save(entity);
        m_businessServiceDao.flush();
    }

    @Test()
    @Transactional
    public void verifyUniqueNameConstraint() {
        BusinessServiceEntity entity1 = new BusinessServiceEntityBuilder()
                .name("Some Custom Name")
                .reduceFunction(m_highestSeverity)
                .toEntity();

        m_businessServiceDao.save(entity1);
        m_businessServiceDao.flush();

        BusinessServiceEntity entity2 = new BusinessServiceEntityBuilder()
                .name("Some Custom Name")
                .reduceFunction(m_highestSeverity)
                .toEntity();
        
        m_businessServiceDao.save(entity2);

        // Should throw a ConstraintViolationException (name not unique)
        try {
            m_businessServiceDao.flush();
            fail("ConstraintViolationException must be thrown");
        } catch (final DataIntegrityViolationException e) {
        }
    }

    @Test
    @Transactional
    public void verifyFindMatching() {
        /*
         * Test that offset and limit work, when only ordered by id
         */
        // create test data
        Long bsId1 = m_businessServiceDao.save(new BusinessServiceEntityBuilder().name("BS 1.1")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("bs1.key1", new IgnoreEntity(), 1)
                .addReductionKey("bs1.key2", new IgnoreEntity(), 2)
                .addReductionKey("bs1.key3", new IgnoreEntity(), 3)
                .toEntity());
        Long bsId2 = m_businessServiceDao.save(new BusinessServiceEntityBuilder().name("BS 2.0")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("bs2.key1", new IgnoreEntity(), 1)
                .addReductionKey("bs2.key2", new IgnoreEntity(), 2)
                .addReductionKey("bs2.key3", new IgnoreEntity(), 3)
                .toEntity());
        m_businessServiceDao.flush();

        // create criteria to limit result
        org.opennms.core.criteria.Criteria criteria = new CriteriaBuilder(BusinessServiceEntity.class)
                .offset(0)
                .limit(2)
                .orderBy("id")
                .toCriteria();

        // verify that entities are distinct
        List<BusinessServiceEntity> filteredBusinessServices = m_businessServiceDao.findMatching(criteria);
        Assert.assertEquals(2, filteredBusinessServices.size());
        Assert.assertEquals(
                m_businessServiceDao.findAll().stream().sorted((bs1, bs2) -> bs1.getId().compareTo(bs2.getId())).collect(Collectors.toList()),
                filteredBusinessServices);

        // create another bs for a more complex test
        Long bsId3 = m_businessServiceDao.save(new BusinessServiceEntityBuilder().name("BS 3.1")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("bs3.key3.1", new IgnoreEntity(), 4)
                .addReductionKey("bs3.key3.2", new IgnoreEntity(), 5)
                .addReductionKey("bs3.key3.3", new IgnoreEntity(), 6)
                .toEntity());
        m_businessServiceDao.flush();

        // restrict to edge.weight > 3 and order by id descending
        criteria.setAliases(Lists.newArrayList(new Alias("edges", "edge", Alias.JoinType.INNER_JOIN, Restrictions.ge("edge.weight", 3))));
        criteria.setOrders(Lists.newArrayList(new Order("id", false)));

        // Verify
        filteredBusinessServices = m_businessServiceDao.findMatching(criteria);
        Assert.assertEquals(2, filteredBusinessServices.size());
        Assert.assertEquals(Lists.newArrayList(
                    m_businessServiceDao.get(bsId3),
                    m_businessServiceDao.get(bsId2)),
                filteredBusinessServices);

        /*
         * Verify that one can also order by name
         */
        Criteria nameCriteria = new CriteriaBuilder(BusinessServiceEntity.class)
                .ilike("name", "BS %.1")
                .orderBy("name")
                .limit(2)
                .toCriteria();
        filteredBusinessServices = m_businessServiceDao.findMatching(nameCriteria);
        Assert.assertEquals(2, filteredBusinessServices.size());
        Assert.assertEquals(Lists.newArrayList(
                    m_businessServiceDao.get(bsId1),
                    m_businessServiceDao.get(bsId3)),
                filteredBusinessServices);
    }

    private OnmsMonitoredService getMonitoredServiceFromNode1() {
        final OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        return Objects.requireNonNull(ipService);
    }
}

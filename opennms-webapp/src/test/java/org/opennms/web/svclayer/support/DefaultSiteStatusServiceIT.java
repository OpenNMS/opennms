/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.model.AggregateStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/testSiteStatusServiceContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
public class DefaultSiteStatusServiceIT implements InitializingBean {

    @Autowired
    private SiteStatusViewService m_aggregateService;
    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private OutageDao m_outageDao;
    @Autowired
    private EventDao m_eventDao;
    @Autowired
    private NodeDao m_nodeDao;
    @Autowired
    private CategoryDao m_categoryDao;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("distributed.layoutApplicationsVertically", "true");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @Transactional
    public void testCreateAggregateStatusView() {
        m_databasePopulator.populateDatabase();

        AggregateStatusView view = m_aggregateService.createAggregateStatusView(null);

        assertNotNull(view);
        assertFalse(view.getStatusDefinitions().isEmpty());

        m_databasePopulator.resetDatabase();
    }

    @Test
    @Transactional
    public void testCreateAggregateStatusUsingNodeId() {
        m_databasePopulator.populateDatabase();

        Collection<AggregateStatus> aggrStati = m_aggregateService.createAggregateStatusesUsingNodeId(m_databasePopulator.getNode1().getId(), "default");
        assertNotNull(aggrStati);

        m_databasePopulator.resetDatabase();
    }

    private void createOutageForNodeInCategory(String categoryName){
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        Collection<OnmsNode> nodes = m_nodeDao.findByCategory(category);

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        /*
         * There should only be 1 node per category based on the set of
         * default nodes that are stored in {@link DatabasePopulator}
         */
        assertEquals(1, nodes.size());

        OnmsNode node = nodes.iterator().next();

        node.visit(new AbstractEntityVisitor() {

            @Override
            public void visitMonitoredService(OnmsMonitoredService monSvc) {
                createOutageForService(monSvc);
            }
        });
    }

    protected void createOutageForService(OnmsMonitoredService monSvc) {
        OnmsEvent outageEvent = new OnmsEvent();
        outageEvent.setEventUei("TEST_UEI");
        outageEvent.setDistPoller(m_databasePopulator.getDistPollerDao().whoami());
        outageEvent.setEventTime(new Date());
        outageEvent.setEventSource("Me");
        outageEvent.setEventCreateTime(new Date());
        outageEvent.setEventSeverity(OnmsSeverity.INDETERMINATE.getId());
        outageEvent.setEventLog("L");
        outageEvent.setEventDisplay("D");
        m_eventDao.save(outageEvent);
        m_eventDao.flush();

        OnmsOutage outage = new OnmsOutage(new Date(), outageEvent, monSvc); 
        m_outageDao.save(outage);
        m_outageDao.flush();
    }

    @Test
    @Transactional
    public void testCreateAggregateStatusUsingBuilding() {
        m_databasePopulator.populateDatabase();

        createOutageForNodeInCategory("Routers");
        createOutageForNodeInCategory("Servers");

        Set<AggregateStatusDefinition> defs = new LinkedHashSet<>();

        AggregateStatusDefinition definition;
        definition = new AggregateStatusDefinition("Routers", Collections.singleton(new OnmsCategory("Routers")));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Switches", Collections.singleton(new OnmsCategory("Switches")));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Servers", Collections.singleton(new OnmsCategory("Servers")));
        defs.add(definition);

        //        AggregateStatusDefinition definition;
        //        definition = new AggregateStatusDefinition("LB/Router", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_ROUTER"), new OnmsCategory("DEV_LOADBAL") })));
        //        defs.add(definition);        
        //        definition = new AggregateStatusDefinition("Access Controller", Collections.singleton(new OnmsCategory("DEV_AC")));
        //        defs.add(definition);
        //        definition = new AggregateStatusDefinition("Switches", Collections.singleton(new OnmsCategory("DEV_SWITCH")));
        //        defs.add(definition);
        //        definition = new AggregateStatusDefinition("Access Points", Collections.singleton(new OnmsCategory("DEV_AP")));
        //        defs.add(definition);
        //        definition = new AggregateStatusDefinition("BCPC", Collections.singleton(new OnmsCategory("DEV_BCPC")));
        //        defs.add(definition);

        AggregateStatusView view = new AggregateStatusView();
        view.setName("building");
        view.setColumnName("building");
        view.setColumnValue("HQ");
        view.setStatusDefinitions(defs);

        List<AggregateStatus> aggrStati = new ArrayList<AggregateStatus>(m_aggregateService.createAggregateStatuses(view));

        AggregateStatus status;
        status = aggrStati.get(0);
        assertEquals("Routers", status.getLabel());
        assertEquals(AggregateStatus.NODES_ARE_DOWN, status.getStatus());

        status = aggrStati.get(1);
        assertEquals("Switches", status.getLabel());
        assertEquals(AggregateStatus.ALL_NODES_UP, status.getStatus());

        status = aggrStati.get(2);
        assertEquals("Servers", status.getLabel());
        assertEquals(AggregateStatus.NODES_ARE_DOWN, status.getStatus());

        //        status = aggrStati.get(3);
        //        assertEquals(AggregateStatus.NODES_ARE_DOWN, status.getStatus());
        //        assertEquals(new Integer(6), status.getDownEntityCount());
        //        
        //        status = aggrStati.get(4);
        //        assertEquals(AggregateStatus.ALL_NODES_UP, status.getStatus());

        m_databasePopulator.resetDatabase();
    }

}

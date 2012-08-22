/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-soa.xml",
        "classpath:META-INF/opennms/applicationContext-dao.xml",
        "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:org/opennms/web/svclayer/applicationContext-svclayer.xml",
        "classpath:META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath:/META-INF/opennms/applicationContext-insertData-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultSiteStatusServiceIntegrationTest implements InitializingBean {

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
    }
    
    @Test
    @Transactional
    public void testCreateAggregateStatusUsingNodeId() {
        m_databasePopulator.populateDatabase();
        
        Collection<AggregateStatus> aggrStati = m_aggregateService.createAggregateStatusesUsingNodeId(m_databasePopulator.getNode1().getId(), "default");
        assertNotNull(aggrStati);
    }
    
    private void createOutageForNodeInCategory(String categoryName){
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        Collection<OnmsNode> nodes = m_nodeDao.findByCategory(category);
        
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
        
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
        outageEvent.setDistPoller(monSvc.getIpInterface().getNode().getDistPoller());
        outageEvent.setEventTime(new Date());
        outageEvent.setEventSource("Me");
        outageEvent.setEventCreateTime(new Date());
        outageEvent.setEventSeverity(0);
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
        
        List<AggregateStatus> aggrStati;
        Collection<AggregateStatusDefinition> defs = new ArrayList<AggregateStatusDefinition>();
        
        AggregateStatusDefinition definition;
        definition = new AggregateStatusDefinition("Routers", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("Routers") })));
        defs.add(definition);        
        definition = new AggregateStatusDefinition("Switches", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("Switches") })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Servers", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("Servers") })));
        defs.add(definition);
        
//        AggregateStatusDefinition definition;
//        definition = new AggregateStatusDefinition("LB/Router", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_ROUTER"), new OnmsCategory("DEV_LOADBAL") })));
//        defs.add(definition);        
//        definition = new AggregateStatusDefinition("Access Controller", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_AC") })));
//        defs.add(definition);
//        definition = new AggregateStatusDefinition("Switches", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_SWITCH") })));
//        defs.add(definition);
//        definition = new AggregateStatusDefinition("Access Points", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_AP") })));
//        defs.add(definition);
//        definition = new AggregateStatusDefinition("BCPC", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_BCPC") })));
//        defs.add(definition);
        
        AggregateStatusView view = new AggregateStatusView();
        view.setName("building");
        view.setColumnName("building");
        view.setColumnValue("HQ");
        view.setStatusDefinitions(new LinkedHashSet<AggregateStatusDefinition>(defs));

        aggrStati = new ArrayList<AggregateStatus>(m_aggregateService.createAggregateStatuses(view));
        
        AggregateStatus status;
        status = aggrStati.get(0);
        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);
        
        status = aggrStati.get(1);
        assertEquals(status.getStatus(), AggregateStatus.ALL_NODES_UP);
        
        status = aggrStati.get(2);
        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);

//        status = aggrStati.get(3);
//        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);
//        assertEquals(new Integer(6), status.getDownEntityCount());
//        
//        status = aggrStati.get(4);
//        assertEquals(status.getStatus(), AggregateStatus.ALL_NODES_UP);

    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.Rows;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SiteStatusViewConfigDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;

public class DefaultSiteStatusServiceTest {
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SiteStatusViewConfigDao m_siteStatusViewConfigDao;
    
    @Before
    public void setUp() throws Exception {
        m_nodeDao = createMock(NodeDao.class);
        m_categoryDao = createMock(CategoryDao.class);
        m_siteStatusViewConfigDao = createMock(SiteStatusViewConfigDao.class);
    }
    
    @Test
    public void testCreateAggregateStatusUsingNodeId() {
        Collection<AggregateStatus> aggrStati;
        Collection<AggregateStatusDefinition> defs = new HashSet<AggregateStatusDefinition>();
        
        OnmsCategory catRouters = new OnmsCategory("routers");
        OnmsCategory catSwitches = new OnmsCategory("switches");
        
        AggregateStatusDefinition definition = 
            new AggregateStatusDefinition("Routers/Switches", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ catRouters, catSwitches })));
        defs.add(definition);
        
        OnmsCategory catServers = new OnmsCategory("servers");
        
        definition = 
            new AggregateStatusDefinition("Servers", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ catServers })));
        defs.add(definition);
        
        DefaultSiteStatusViewService aggregateSvc = new DefaultSiteStatusViewService();
        aggregateSvc.setNodeDao(m_nodeDao);
        aggregateSvc.setCategoryDao(m_categoryDao);
        aggregateSvc.setSiteStatusViewConfigDao(m_siteStatusViewConfigDao);
        
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.getAssetRecord().setBuilding("HQ");
        List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        nodes.add(node);
        
        for (AggregateStatusDefinition def : defs) {
            expect(m_nodeDao.findAllByVarCharAssetColumnCategoryList("building", "HQ", def.getCategories())).andReturn(nodes);
        }
        for (OnmsNode n : nodes) {
            expect(m_nodeDao.load(n.getId())).andReturn(n);
        }
        replay(m_nodeDao);
        
        expect(m_categoryDao.findByName("switches")).andReturn(catSwitches);
        expect(m_categoryDao.findByName("routers")).andReturn(catRouters);
        expect(m_categoryDao.findByName("servers")).andReturn(catServers);
        replay(m_categoryDao);
        
        Rows rows = new Rows();
        RowDef rowDef = new RowDef();
        Category category = new Category();
        category.setName("servers");
        rowDef.addCategory(category);
        rows.addRowDef(rowDef);
        
        rowDef = new RowDef();
        category = new Category();
        category.setName("switches");
        rowDef.addCategory(category);
        category = new Category();
        category.setName("routers");
        rowDef.addCategory(category);
        rows.addRowDef(rowDef);

        View view = new View();
        view.setRows(rows);
        expect(m_siteStatusViewConfigDao.getView("building")).andReturn(view);
        replay(m_siteStatusViewConfigDao);
        
        aggrStati = aggregateSvc.createAggregateStatusesUsingNodeId(node.getId(), "building");
        
        verify(m_nodeDao);
        verify(m_categoryDao);
        verify(m_siteStatusViewConfigDao);
        
        assertNotNull(aggrStati);
    }
    
    @Test
    public void testCreateAggregateStatusUsingBuilding() {
        
        Collection<AggregateStatus> aggrStati;
        Collection<AggregateStatusDefinition> defs = new HashSet<AggregateStatusDefinition>();
        
        AggregateStatusDefinition definition = 
            new AggregateStatusDefinition("Routers/Switches", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("routers"), new OnmsCategory("switches") })));
        defs.add(definition);
        
        definition = 
            new AggregateStatusDefinition("Servers", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("servers") })));
            
        defs.add(definition);
        
        DefaultSiteStatusViewService aggregateSvc = new DefaultSiteStatusViewService();
        aggregateSvc.setNodeDao(m_nodeDao);
        
        OnmsNode node = new OnmsNode();
        List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        nodes.add(node);
        
        for (AggregateStatusDefinition def : defs) {
            expect(m_nodeDao.findAllByVarCharAssetColumnCategoryList("building", "HQ", def.getCategories())).andReturn(nodes);
        }
        replay(m_nodeDao);
        
        AggregateStatusView view = new AggregateStatusView();
        view.setColumnName("building");
        view.setColumnValue("HQ");
        view.setTableName("assets");
        view.setStatusDefinitions(new LinkedHashSet<AggregateStatusDefinition>(defs));
        aggrStati = aggregateSvc.createAggregateStatusUsingAssetColumn(view);
        verify(m_nodeDao);
        
        assertNotNull(aggrStati);

    }

}

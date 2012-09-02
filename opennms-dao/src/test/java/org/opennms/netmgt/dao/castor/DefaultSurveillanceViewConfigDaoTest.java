/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.castor;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.config.surveillanceViews.Views;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class DefaultSurveillanceViewConfigDaoTest extends TestCase {
    private static final String CONFIG_WITH_VIEWS_RESOURCE = "/surveillance-views.testdata.xml";
    private static final String CONFIG_NO_VIEWS_RESOURCE = "/surveillance-views.testdata.noviews.xml";
    
    private DefaultSurveillanceViewConfigDao m_dao;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();
        
        createDaoWithResource(CONFIG_WITH_VIEWS_RESOURCE);
    }
    
    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    public void testNothing() {
        // test that setUp() / tearDown() works
    }
    
    public void testDefaultView() {
        View view = m_dao.getDefaultView();
        assertNotNull("default view should not be null", view);
        assertEquals("default view name", "default", view.getName());
        
        Columns columns = view.getColumns();
        assertNotNull("default view columns should not be null", columns);
        assertEquals("default view column count", 3, columns.getColumnDefCount());
        
        Rows rows = view.getRows();
        assertNotNull("default view rows should not be null", rows);
        assertEquals("default view row count", 3, rows.getRowDefCount());
    }
    
    public void testViewByName() {
        View view = m_dao.getView("default");
        assertNotNull("default view should not be null", view);
        assertEquals("default view name", "default", view.getName());
        
        Columns columns = view.getColumns();
        assertNotNull("default view columns should not be null", columns);
        assertEquals("default view column count", 3, columns.getColumnDefCount());
        
        Rows rows = view.getRows();
        assertNotNull("default view rows should not be null", rows);
        assertEquals("default view row count", 3, rows.getRowDefCount());
    }
    
    public void testGetViews() {
        Views views = m_dao.getViews();
        assertNotNull("views should not be null", views);
        assertEquals("view count", 1, views.getViewCount());
        
        View view = views.getView(0);
        assertNotNull("first view should not be null", view);
        assertEquals("first view name", "default", view.getName());
        
        Columns columns = view.getColumns();
        assertNotNull("first view columns should not be null", columns);
        assertEquals("first view column count", 3, columns.getColumnDefCount());
        
        Rows rows = view.getRows();
        assertNotNull("first view rows should not be null", rows);
        assertEquals("first view row count", 3, rows.getRowDefCount());
    }
    
    public void testGetViewMap() {
        Map<String, View> viewMap = m_dao.getViewMap();
        assertEquals("view count", 1, viewMap.size());
        
        assertNull("shouldn't have 'bogus' view", viewMap.get("bogus"));
        
        View view = viewMap.get("default");
        assertNotNull("should have 'default' view", view);
        
        assertNotNull("first view should not be null", view);
        assertEquals("first view name", "default", view.getName());
        
        Columns columns = view.getColumns();
        assertNotNull("first view columns should not be null", columns);
        assertEquals("first view column count", 3, columns.getColumnDefCount());
        
        Rows rows = view.getRows();
        assertNotNull("first view rows should not be null", rows);
        assertEquals("first view row count", 3, rows.getRowDefCount());
    }
    
    public void testInitNoViews() throws IOException {
        createDaoWithResource(CONFIG_NO_VIEWS_RESOURCE);
    }
    
    public void testGetDefaultViewNoViews() throws IOException {
        createDaoWithResource(CONFIG_NO_VIEWS_RESOURCE);
        
        View view = m_dao.getDefaultView();
        assertNull("default view should be null", view);
    }
    
    public void testGetViewByNameNoViews() throws IOException {
        createDaoWithResource(CONFIG_NO_VIEWS_RESOURCE);
        
        View view = m_dao.getView("default");
        assertNull("view by name 'default' should be null", view);
    }
    
    public void testGetViewsNoViews() throws IOException {
        createDaoWithResource(CONFIG_NO_VIEWS_RESOURCE);
        
        Views views = m_dao.getViews();
        assertNotNull("views should not be null", views);
        assertEquals("view count", 0, views.getViewCount());
    }
    
    public void testGetViewMapNoViews() throws IOException {
        createDaoWithResource(CONFIG_NO_VIEWS_RESOURCE);
        
        Map<String, View> viewMap = m_dao.getViewMap();
        assertNotNull("viewMap should not be null", viewMap);
        assertEquals("view count", 0, viewMap.size());
    }
    
    public void testConfigProduction() throws IOException {
        createDaoWithConfigFile("surveillance-views.xml");
    }
    
    public void testConfigExample() throws IOException {
        createDaoWithConfigFile("examples/surveillance-views.xml");
    }

    private void createDaoWithResource(final String configResource) throws IOException {
        Resource resource = new ClassPathResource(configResource);
        m_dao = new DefaultSurveillanceViewConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
    }

    private void createDaoWithConfigFile(final String configFileName) throws IOException {
        Resource resource = new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile(configFileName));
        m_dao = new DefaultSurveillanceViewConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
    }


}

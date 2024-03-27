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
package org.opennms.netmgt.dao.jaxb;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

/**
 * Takes too long to run, so it's an IT test now.
 */
public class DefaultSurveillanceViewConfigDaoIT extends TestCase {
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
        
        assertNotNull("default view columns should not be null", view.getColumns());
        assertEquals("default view column count", 3, view.getColumns().size());
        
        assertNotNull("default view rows should not be null", view.getRows());
        assertEquals("default view row count", 3, view.getRows().size());
    }
    
    public void testViewByName() {
        View view = m_dao.getView("default");
        assertNotNull("default view should not be null", view);
        assertEquals("default view name", "default", view.getName());
        
        assertNotNull("default view columns should not be null", view.getColumns());
        assertEquals("default view column count", 3, view.getColumns().size());
        
        assertNotNull("default view rows should not be null", view.getRows());
        assertEquals("default view row count", 3, view.getRows().size());
    }
    
    public void testGetViews() {
        List<View> views = m_dao.getViews();
        assertNotNull("views should not be null", views);
        assertEquals("view count", 1, views.size());
        
        View view = views.get(0);
        assertNotNull("first view should not be null", view);
        assertEquals("first view name", "default", view.getName());
        
        List<ColumnDef> columns = view.getColumns();
        assertNotNull("first view columns should not be null", columns);
        assertEquals("first view column count", 3, columns.size());
        
        List<RowDef> rows = view.getRows();
        assertNotNull("first view rows should not be null", rows);
        assertEquals("first view row count", 3, rows.size());
    }
    
    public void testGetViewMap() {
        Map<String, View> viewMap = m_dao.getViewMap();
        assertEquals("view count", 1, viewMap.size());
        
        assertNull("shouldn't have 'bogus' view", viewMap.get("bogus"));
        
        View view = viewMap.get("default");
        assertNotNull("should have 'default' view", view);
        
        assertNotNull("first view should not be null", view);
        assertEquals("first view name", "default", view.getName());
        
        List<ColumnDef> columns = view.getColumns();
        assertNotNull("first view columns should not be null", columns);
        assertEquals("first view column count", 3, columns.size());
        
        List<RowDef> rows = view.getRows();
        assertNotNull("first view rows should not be null", rows);
        assertEquals("first view row count", 3, rows.size());
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
        
        List<View> views = m_dao.getViews();
        assertNotNull("views should not be null", views);
        assertEquals("view count", 0, views.size());
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

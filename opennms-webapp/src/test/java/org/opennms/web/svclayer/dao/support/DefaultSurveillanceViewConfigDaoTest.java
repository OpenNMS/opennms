package org.opennms.web.svclayer.dao.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SurveillanceViewsFactory;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.config.surveillanceViews.Views;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

import junit.framework.TestCase;

public class DefaultSurveillanceViewConfigDaoTest extends TestCase {
    private DefaultSurveillanceViewConfigDao m_dao;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();
        
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
        m_dao = new DefaultSurveillanceViewConfigDao();
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
    
    public void testInitNoViews() throws MarshalException, ValidationException, IOException {
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.noviews.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
    }
    
    public void testGetDefaultViewNoViews() throws MarshalException, ValidationException, IOException {
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.noviews.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
        
        View view = m_dao.getDefaultView();
        assertNull("default view should be null", view);

    }
    
    public void testGetViewByNameNoViews() throws MarshalException, ValidationException, IOException {
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.noviews.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
        
        View view = m_dao.getView("default");
        assertNull("view by name 'default' should be null", view);

    }
    
    public void testGetViewsNoViews() throws MarshalException, ValidationException, IOException {
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.noviews.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
        
        Views views = m_dao.getViews();
        assertNotNull("views should not be null", views);
        assertEquals("view count", 0, views.getViewCount());
    }
    
    public void testGetViewMapNoViews() throws MarshalException, ValidationException, IOException {
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/surveillance-views.testdata.noviews.xml"));
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
        
        Map<String, View> viewMap = m_dao.getViewMap();
        assertNotNull("viewMap should not be null", viewMap);
        assertEquals("view count", 0, viewMap.size());
    }
    
    
    public void testConfigProduction() throws MarshalException, ValidationException, IOException {
        Reader rdr = ConfigurationTestUtils.getReaderForConfigFile("surveillance-views.xml");
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
    }
    
    public void testConfigExample() throws MarshalException, ValidationException, IOException {
        Reader rdr = ConfigurationTestUtils.getReaderForConfigFile("examples/surveillance-views.xml");
        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory(rdr));
        rdr.close();
    }

}

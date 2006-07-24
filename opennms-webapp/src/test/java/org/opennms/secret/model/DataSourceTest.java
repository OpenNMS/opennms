package org.opennms.secret.model;

import junit.framework.TestCase;

public class DataSourceTest extends TestCase {
    private DataSource m_dataSource;
    
    public void populate() {
        m_dataSource = new DataSource("test_id", "test_name", "test_source", "test_datasource");
    }
    
    /*
     * Test constructor for 'org.opennms.secret.model.DataSource()'
     */
    public void testConstructorEmpty() {
        DataSource a = new DataSource();
        
        assertNull("uninitialized id should be null", a.getId());
        assertNull("uninitialized name should be null", a.getName());
        assertNull("uninitialized source should be null", a.getSource());
        assertNull("uninitialized datasource should be null", a.getDataSource());
    }

    /*
     * Test constructor for 'org.opennms.secret.model.DataSource(String, String, String, String)'
     */
    public void testConstructorArgs() {
        DataSource a = new DataSource("test_id", "test_name", "test_source", "test_datasource");
        
        assertEquals("id field", "test_id", a.getId());
        assertEquals("name field", "test_name", a.getName());
        assertEquals("source field", "test_source", a.getSource());
        assertEquals("datasource field", "test_datasource", a.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getDataSource()'
     */
    public void testGetDataSource() {
        populate();
        assertEquals("datasource field", "test_datasource", m_dataSource.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setDataSource(String)'
     */
    public void testSetDataSource() {
        populate();
        assertEquals("datasource field", "test_datasource", m_dataSource.getDataSource());
        m_dataSource.setDataSource("test_datasource_changed");
        assertEquals("datasource field", "test_datasource_changed", m_dataSource.getDataSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getId()'
     */
    public void testGetId() {
        populate();
        assertEquals("id field", "test_id", m_dataSource.getId());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setId(String)'
     */
    public void testSetId() {
        populate();
        assertEquals("id field", "test_id", m_dataSource.getId());
        m_dataSource.setId("test_id_changed");
        assertEquals("id field", "test_id_changed", m_dataSource.getId());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getName()'
     */
    public void testGetName() {
        populate();
        assertEquals("name field", "test_name", m_dataSource.getName());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setName(String)'
     */
    public void testSetName() {
        populate();
        assertEquals("name field", "test_name", m_dataSource.getName());
        m_dataSource.setName("test_name_changed");
        assertEquals("name field", "test_name_changed", m_dataSource.getName());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.getSource()'
     */
    public void testGetSource() {
        populate();
        assertEquals("source field", "test_source", m_dataSource.getSource());
    }

    /*
     * Test method for 'org.opennms.secret.model.DataSource.setSource(String)'
     */
    public void testSetSource() {
        populate();
        assertEquals("source field", "test_source", m_dataSource.getSource());
        m_dataSource.setSource("test_source_changed");
        assertEquals("source field", "test_source_changed", m_dataSource.getSource());
    }

}

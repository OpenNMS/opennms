package org.opennms.netmgt.invd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.InventoryAssetDao;
import org.opennms.netmgt.dao.InventoryAssetPropertyDao;
import org.opennms.netmgt.dao.InventoryCategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.dao.hibernate.InvdConfigurationExecutionListener;
import org.opennms.netmgt.dao.hibernate.InventoryDatabasePopulator;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	InvdConfigurationExecutionListener.class,
	TemporaryDatabaseExecutionListener.class,
	DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={		
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-invDatabasePopulator.xml"
})
@JUnitTemporaryDatabase()
public class InventoryResourcePersisterTest {
	@Autowired
    InventoryDatabasePopulator m_dbPopulator;
	
	@Autowired
	private InventoryCategoryDao m_invCategoryDao;
	
	@Autowired
	private InventoryAssetDao m_invAssetDao;
	
	@Autowired
	private InventoryAssetPropertyDao m_invAssetPropDao;
	
	@Autowired
	private NodeDao m_nodeDao;
	
	InventoryResourcePersister m_persister;
	
	@Before
	public void setUp() throws Exception {
		m_dbPopulator.populateDatabase();
		
		m_persister = new InventoryResourcePersister();
		m_persister.setInvAssetDao(m_invAssetDao);
		m_persister.setInvAssetPropDao(m_invAssetPropDao);
		m_persister.setInvCategoryDao(m_invCategoryDao);
		m_persister.setNodeDao(m_nodeDao);
	}
	
	@Test
	public void testNewInventoryAsset() {
		assertEquals("total asset count ", 1, m_invAssetDao.countAll());

		// Create a new result.
		InventorySet scanResultSet=new InventorySet() {

            public int getStatus() {
                return InventoryScanner.SCAN_SUCCEEDED;
            }

            public List<InventoryResource> getInventoryResources() {
            	ArrayList<InventoryResource> resources = new ArrayList<InventoryResource>();
            	InventoryResource resource = new InventoryResource() {
            		@Override
            		public Integer getOwnerNodeId() { return 1; }
            		
            		@Override
            		public String getResourceCategory() { return "Network Equipment"; }
            		
            		public Date getResourceDate() { return new Date(); }
            		
            		@Override
            		public String getResourceName() { return "Intel Pro 1000"; }
            		
            		@Override
            		public Map<String, String> getResourceProperties() {
            			Map<String, String> resourceProps = new HashMap<String, String>();
            			resourceProps.put("manufacturer", "Intel");
            			return resourceProps;
            		}
            		
            		@Override
            		public String getResourceSource() { return "User-defined"; }
            		
            		@Override
            		public boolean rescanNeeded() { return false; }
            	};
            	resources.add(resource);
            	return resources;
            }
			
			public void setInventoryResources(List<InventoryResource> resources) {
                return;
            }
        };
        
        m_persister.persist(scanResultSet);
        
        assertEquals("total asset count ", 2, m_invAssetDao.countAll());
	}
	
	@Test
	public void testUpdateInventoryAsset() {
		assertEquals("total asset count ", 1, m_invAssetDao.countAll());
		OnmsInventoryAsset asset = m_dbPopulator.getInvAsset1();

		// Create a new result.
		InventorySet scanResultSet=new InventorySet() {

            public int getStatus() {
                return InventoryScanner.SCAN_SUCCEEDED;
            }

            public List<InventoryResource> getInventoryResources() {
            	ArrayList<InventoryResource> resources = new ArrayList<InventoryResource>();
            	InventoryResource resource = new InventoryResource() {
            		@Override
            		public Integer getOwnerNodeId() { return m_dbPopulator.getNode1().getId(); }
            		
            		@Override
            		public String getResourceCategory() { return m_dbPopulator.getInvAsset1().getCategory().getCategoryName(); }
            		
            		public Date getResourceDate() { return new Date(); }
            		
            		@Override
            		public String getResourceName() { return m_dbPopulator.getInvAsset1().getAssetName(); }
            		
            		@Override
            		public Map<String, String> getResourceProperties() {
            			Map<String, String> resourceProps = new HashMap<String, String>();
            			resourceProps.put("manufacturer", "Intel, Inc");
            			return resourceProps;
            		}
            		
            		@Override
            		public String getResourceSource() { return "Invd"; }
            		
            		@Override
            		public boolean rescanNeeded() { return false; }
            	};
            	resources.add(resource);
            	return resources;
            }
			
			public void setInventoryResources(List<InventoryResource> resources) {
                return;
            }
        };
        
        m_persister.persist(scanResultSet);
        
        assertEquals("total asset count ", 1, m_invAssetDao.countAll());
        OnmsInventoryAsset asset2 = m_invAssetDao.findByNameNodeAndCategory(asset.getAssetName(), asset.getOwnerNode(), asset.getCategory());
		assertTrue("asset property value for manufacturer changed", !asset2.getPropertyByName("manufacturer").equals(asset.getPropertyByName("manufacturer")));
	}
	
	@Test
	public void testInventoryAssetNewProperty() {
		// TODO
	}
	
	public void testUpdateInventoryAssetNoProps() {
		// TODO
	}
	
	@Test
	public void testUpdatePropsNoInventoryAsset() {
		// TODO
	}
}

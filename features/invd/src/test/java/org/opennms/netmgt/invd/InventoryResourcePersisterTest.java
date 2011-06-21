package org.opennms.netmgt.invd;

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
@JUnitTemporaryDatabase(populate=true)
public class InventoryResourcePersisterTest {
	@Autowired
	private InventoryCategoryDao m_invCategoryDao;
	
	@Autowired
	private InventoryAssetDao m_invAssetDao;
	
	@Autowired
	private InventoryAssetPropertyDao m_invAssetPropDao;
	
	@Autowired
	private NodeDao m_nodeDao;
	
	private InventorySet m_result;
	
	@Before
	public void setUp() {
		// need a mock.
		//m_result = new InventorySet();
		
	}
	
	@Test
	public void testNewInventoryAsset() {
		InventoryResourcePersister persister = new InventoryResourcePersister();
		
		// Create a new result.
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

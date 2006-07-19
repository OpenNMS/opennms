package org.opennms.web.svclayer;

import static org.easymock.EasyMock.*;

import java.util.Collection;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

import junit.framework.TestCase;

public class PerfGraphBuilderServiceTest extends TestCase {
	
	DefaultPerfGraphBuilderService m_perfGraphBuilderService;
	NodeDao m_mockNodeDao;

	protected void setUp() throws Exception {
		
		m_mockNodeDao = createMock(NodeDao.class);
		
		m_perfGraphBuilderService = new DefaultPerfGraphBuilderService();
		m_perfGraphBuilderService.setNodeDao(m_mockNodeDao);
	}
	
	public void testCreateGraphDefinition() {
		
		assertNotNull(m_perfGraphBuilderService.createGraphDefinition());
		
	}
	
	public void testGetAttributesForResource() {

		OnmsNode node = new OnmsNode();
		node.setId(1);
		node.setLabel("TestNode");
		
		int ifCount = 3;
		// add ip interfaces
		for(int i = 1; i <= ifCount; i++) {
			OnmsIpInterface ipIface = new OnmsIpInterface("192.168.1."+i, node);
			// these add themselves to the node
		}		
		
		expect(m_mockNodeDao.get(1)).andReturn(node);
		replay(m_mockNodeDao);
		
		Palette palette = m_perfGraphBuilderService.getAttributePalette(1);
		assertNotNull(palette);
		assertEquals("TestNode", palette.getLabel());
		assertNotNull(palette.getCategories());

		Collection<PaletteCategory> categories = palette.getCategories();

		// a category for the node plus a category for each iface
		assertEquals(1+ifCount, categories.size());
		
		PaletteCategory[] categoryArray = (PaletteCategory[]) categories.toArray(new PaletteCategory[categories.size()]);

		for (int i = 0; i < categoryArray.length; i++) {
			PaletteCategory cat = categoryArray[i];
			if (i == 0)
				assertEquals("Node Attributes", cat.getLabel());
			else
				assertTrue(cat.getLabel().startsWith("Interface: "));
		}
		
		verify(m_mockNodeDao);
	}
	

}

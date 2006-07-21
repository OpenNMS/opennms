package org.opennms.web.svclayer;

import junit.framework.TestCase;

import static org.opennms.web.svclayer.PaletteTestUtils.assertPaletteEquals;

public class PaletteBuilderTest extends TestCase {
	
	private Palette m_palette;
	private PaletteCategory m_currentCat;

	public void testBuildingPalatte() {
		
		m_palette = new Palette("paletteLabel");
		PaletteBuilder m_builder = new PaletteBuilder("paletteLabel");
		
		m_builder.addCategory("cat1");
		createCategory("cat1");
		m_builder.addItem("c1a_id", "c1a_label");
		createItem("c1a_id", "c1a_label");
		m_builder.addItem("c1b_id", "c1b_label");
		createItem("c1b_id", "c1b_label");
		m_builder.addItem("c1c_id", "c1c_label");
		createItem("c1c_id", "c1c_label");
		
		m_builder.addCategory("cat2");
		createCategory("cat2");
		m_builder.addItem("c2a_id", "c2a_label");
		createItem("c2a_id", "c2a_label");
		m_builder.addSpacer();
		createSpacer();
		m_builder.addItem("c2b_id", "c2b_label");
		createItem("c2b_id", "c2b_label");
		
		assertPaletteEquals(m_palette, m_builder.getPalette());
		
		
	}

	private void createSpacer() {
		m_currentCat.addItem(PaletteItem.SPACER);
	}

	private void createCategory(String label) {
		PaletteCategory cat = new PaletteCategory(label);
		m_palette.addCategory(cat);
		m_currentCat = cat;
	}
	
	private void createItem(String id, String label) {
		PaletteItem item = new PaletteItem(id, label);
		m_currentCat.addItem(item);
	}
	
	
	

}

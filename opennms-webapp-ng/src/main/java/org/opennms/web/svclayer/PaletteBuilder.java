package org.opennms.web.svclayer;

public class PaletteBuilder {
	
	private Palette m_palette;
	private PaletteCategory m_currentCategory;
	
	public PaletteBuilder(String label) {
		m_palette = new Palette(label);
	}
	
	public PaletteBuilder addCategory(String label) {
		m_currentCategory = new PaletteCategory(label);
		m_palette.addCategory(m_currentCategory);
		return this;
	}
	
	public PaletteBuilder addItem(String id, String label) {
		PaletteItem item = new PaletteItem(id, label);
		m_currentCategory.addItem(item);
		return this;
	}
	
	public PaletteBuilder addSpacer() {
		m_currentCategory.addItem(PaletteItem.SPACER);
		return this;
	}
	
	public Palette getPalette() {
		return m_palette;
	}
	
	

}

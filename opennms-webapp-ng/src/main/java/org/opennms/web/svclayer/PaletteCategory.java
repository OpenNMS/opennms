package org.opennms.web.svclayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PaletteCategory {

	private String m_label;
	private List<PaletteItem> m_items = new LinkedList<PaletteItem>();

	public PaletteCategory() {
		this(null);
	}
	
	public PaletteCategory(String label) {
		m_label = label;
	}

	public String getLabel() {
		return m_label;
	}
	
	public void setLabel(String label) {
		m_label = label;
	}
	
	public List<PaletteItem> getItems() {
		return Collections.unmodifiableList(m_items);
	}
	
	public void addItem(PaletteItem item) {
		m_items.add(item);
	}
	
	
}

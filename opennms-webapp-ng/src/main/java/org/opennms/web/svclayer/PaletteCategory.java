package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class PaletteCategory {

	private String m_label;
	private Collection<PalleteItem> m_items = new LinkedList<PalleteItem>();

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
	
	public Collection<PalleteItem> getItems() {
		return Collections.unmodifiableCollection(m_items);
	}
	
	public void addItem(PalleteItem item) {
		m_items.add(item);
	}
	
	
}

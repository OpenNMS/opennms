package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Palette {
	
	private String m_label;
	private Collection<PaletteCategory> m_categories = new LinkedList<PaletteCategory>();
	
	public Palette() {
		this(null);
	}
	
	public Palette(String label) {
		m_label = label;
	}

	public String getLabel() {
		return m_label;
	}
	
	public void setLabel(String label) {
		m_label = label;
	}
	
	public Collection<PaletteCategory> getCategories() {
		return Collections.unmodifiableCollection(m_categories);
	}
	
	public void addCategory(PaletteCategory category) {
		m_categories.add(category);
	}
	
	
}

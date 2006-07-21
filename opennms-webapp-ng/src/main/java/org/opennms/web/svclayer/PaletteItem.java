package org.opennms.web.svclayer;

public class PaletteItem {
	
	public static final PaletteItem SPACER = new PaletteItem(null, null, true);
	
	private String m_label;
	private String m_id;
	private boolean m_spacer = false;
	
	protected PaletteItem(String id, String label, boolean spacer) {
		m_id = id;
		m_label = label;
		m_spacer = spacer;
	}
	
	public PaletteItem(String id, String label) {
		this(id, label, false);
	}
	
	public String getLabel() {
		return m_label;
	}
	
	public String getId() {
		return m_id;
	}
	
	public boolean isSpacer() {
		return m_spacer;
	}
	
	public String toString() {
		if (isSpacer()) {
			return "SPACER";
		}
		else {
			return m_label+"<"+m_id+">";
		}
	}
	
}

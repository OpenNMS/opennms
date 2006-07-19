package org.opennms.web.svclayer;

public class PalleteItem {
	
	public static final PalleteItem SPACER = new PalleteItem(null, null, true);
	
	private String m_label;
	private String m_id;
	private boolean m_spacer = false;
	
	protected PalleteItem(String id, String label, boolean spacer) {
		m_id = id;
		m_label = label;
		m_spacer = spacer;
	}
	
	public PalleteItem(String id, String label) {
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
	
}

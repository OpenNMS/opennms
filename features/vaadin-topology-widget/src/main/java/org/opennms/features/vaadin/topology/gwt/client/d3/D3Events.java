package org.opennms.features.vaadin.topology.gwt.client.d3;

public enum D3Events {
	
	CLICK("click");
	
	private String m_event;
	
	D3Events(String event){
		m_event = event;
	}
	
	public String event() {
		return m_event;
	}
	
	public interface Handler <T>{
		public void call(T t, int index);
	}
	
}

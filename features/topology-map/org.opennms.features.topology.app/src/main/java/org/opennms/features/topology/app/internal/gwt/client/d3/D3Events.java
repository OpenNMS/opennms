package org.opennms.features.topology.app.internal.gwt.client.d3;

public enum D3Events {
	
	CLICK("click"),
	MOUSE_DOWN("mousedown"),
	KEY_DOWN("keydown"), 
	CONTEXT_MENU("contextmenu"),
	DRAG_START("dragstart"),
	DRAG("drag"),
	DRAG_END("dragend"), 
	MOUSE_OVER("mouseover"), 
	MOUSE_OUT("mouseout");
	
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
	
	public interface XMLHandler<T>{
	    public void call(T t);
	}
	
	public interface AnonymousHandler{
	    public void call();
	}
}

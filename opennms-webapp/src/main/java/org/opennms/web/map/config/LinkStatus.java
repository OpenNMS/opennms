package org.opennms.web.map.config;

public class LinkStatus {
	String id;
	String color;
	boolean flash;
	
	
	public LinkStatus(String id, String color, boolean flash) {
		super();
		this.id = id;
		this.color = color;
		this.flash = flash;
	}
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public boolean getFlash() {
		return flash;
	}
	public void setFlash(boolean flash) {
		this.flash = flash;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	
}

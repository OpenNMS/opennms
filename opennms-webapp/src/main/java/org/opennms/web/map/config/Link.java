package org.opennms.web.map.config;

public class Link {
	String speed; 
	String text; 
	String width;
	int dasharray=-1;
	
	
	
	Link(String speed, String text, String width, int dasharray) {
		super();
		this.speed = speed;
		this.text = text;
		this.width = width;
		this.dasharray = dasharray;
	}
	public int getDasharray() {
		return dasharray;
	}
	public void setDasharray(int dasharray) {
		this.dasharray = dasharray;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	
}

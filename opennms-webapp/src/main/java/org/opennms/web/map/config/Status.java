/*
 * Created on 11-lug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.config;

/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Status {
	private int id;
	private String uei;
	private String color;
	private String text;
	
	
	/**
	 * @param id
	 * @param uei
	 * @param color
	 * @param text
	 */
	public Status(int id, String uei, String color, String text) {
		super();
		this.id = id;
		this.uei = uei;
		this.color = color;
		this.text = text;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUei() {
		return uei;
	}
	public void setUei(String uei) {
		this.uei = uei;
	}
}

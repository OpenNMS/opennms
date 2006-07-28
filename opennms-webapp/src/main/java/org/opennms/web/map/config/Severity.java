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
public class Severity {
	private int id;
	private String label;
	private String color;

	private boolean flash = false; 
	/**
	 * @param id
	 * @param label
	 * @param color
	 */
	public Severity(int id, String label, String color) {
		super();
		this.id = id;
		this.label = label;
		this.color = color;
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean isFlash() {
		return flash;
	}
	public void setFlash(boolean flash) {
		this.flash = flash;
	}
}

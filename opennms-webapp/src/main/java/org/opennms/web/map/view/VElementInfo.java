/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

/**
 * @author
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class VElementInfo implements Cloneable {
    
	private int id;

    private String uei;
    
    private int severity;
    
    private String label;
    
    
    
	public String getLabel() {
		return label;
	}
	public int getId() {
		return id;
	}
	public int getSeverity() {
		return severity;
	}
	public String getUei() {
		return uei;
	}

    
    
	/**
	 * @param id
	 * @param uei
	 * @param severity
	 * @param label
	 */
	public VElementInfo(int id, String uei, int severity, String label) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
		this.label=label;
	}
	
	/**
	 * @param id
	 * @param uei
	 * @param severity
	 */
	public VElementInfo(int id, String uei, int severity) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
	}
}

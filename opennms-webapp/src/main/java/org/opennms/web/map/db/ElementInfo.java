/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

/**
 * @author
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ElementInfo implements Cloneable {

	public int getId() {
		return id;
	}
	public int getSeverity() {
		return severity;
	}
	public String getUei() {
		return uei;
	}
    private int id;

    private String uei;
    
    private int severity;
    
    
	/**
	 * @param id
	 * @param uei
	 * @param severity
	 */
	protected ElementInfo(int id, String uei, int severity) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
	}
}

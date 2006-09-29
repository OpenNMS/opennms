/* 
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.db.Element;

/**
 * @author micmas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VElement extends Element {

	// static info for status
	/*public static final int NODE_UNDEF_STATUS = 0;
	
	public static final int NODE_DOWN_STATUS = 1;

	public static final int INTERFACE_DOWN_STATUS = 2;

	public static final int INTERFACE_LINK_DOWN_STATUS = 3;
	
	public static final int SERVICE_DOWN_STATUS = 4;

	public static final int NODE_UP_STATUS = 5;

	private static final int LOWER_STATUS = 0;
	private static final int UPPER_STATUS = 4;

	// static info for severity
	public static final int UNDEFINED_SEVERITY = 0;
	
	public static final int CRITICAL_SEVERITY = 1;

	public static final int MAJOR_SEVERITY = 2;

	public static final int WARN_SEVERITY = 3;

	public static final int NORMAL_SEVERITY = 4;

	private static final int LOWER_SEVERITY = 0;
	private static final int UPPER_SEVERITY = 4;*/

	// boolean that represents if this is a childnode useful to avoid loops  
	protected boolean isChild = false;
    
    // this is to define the status of the element
	protected int status = 0;

	// this is used to understand if some fact happen is important
	protected int severity = 0;

	// this represents the global information elements
	protected double rtc = 0;

    /**
     * 
     */
    protected VElement() {
        super();
    }

    /**
     * @param e
     */
    VElement(Element e) throws MapsException {
        super(e);
    }

    /**
     * @param mapId
     * @param id
     * @param type
     * @param label
     * @param iconName
     * @param x
     * @param y
     */
    protected VElement(int mapId, int id, String type, String iconName,String label,
            int x, int y) throws MapsException {
        super(mapId, id, type, label, iconName, x, y);
        isChild = true;
    }    

    protected VElement(int mapId, int id, String type, String label, String iconName) throws MapsException {
        super(mapId, id, type, label, iconName, 0, 0);
        isChild = true;
    }    

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		//if (status > LOWER_STATUS && status <= UPPER_STATUS) 
			this.status = status;
	}
	
	public int getSeverity() {
		return severity;
	}
	
	public void setSeverity(int severity) {
		//if (severity > LOWER_SEVERITY && severity <= UPPER_SEVERITY) 
			this.severity = severity;
	}

	public double getRtc() {
		return rtc;
	}
	
	public void setRtc(double rtc) {
		this.rtc = rtc;
	}
	
    public boolean isChild() {
        return isChild;
    }
    
    /**
     * @param elem
     * @return
     */

    public boolean equalsIgnorePosition(VElement elem) {
    	if (this.getMapId() == elem.getMapId() &&
    		this.getId() == elem.getId() && this.type.equals( elem.getType()) &&
    		this.rtc == elem.getRtc() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel() )) return true;
    	return false;
    }

    public boolean equalsIgnorePositionParentMap(VElement elem) {
    	if (this.getId() == elem.getId() && this.type.equals( elem.getType() ) &&
    		this.rtc == elem.getRtc() && this.status == elem.getStatus() &&
			this.severity == elem.getSeverity() && this.getLabel().equals( elem.getLabel())) return true;
    	return false;
    }

    public void setMapId(int mapId) {
        super.setMapId(mapId);
        isChild = true;
    }
    
    final public int getContainerMap()throws VElementNotChildException {
    	if(isChild==true)
    		return getMapId();
    	throw new VElementNotChildException();
    }
}

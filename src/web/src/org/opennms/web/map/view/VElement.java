/* 
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.sql.SQLException;

import org.opennms.web.map.MapsException;
import org.opennms.web.map.db.Element;
import org.opennms.web.map.db.Factory;

/**
 * @author micmas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class VElement extends Element {
    protected boolean isChild = false;
    
    /**
     * 
     */
    protected VElement() {
        super();
    }

    /**
     * @param e
     */
    VElement(Element e) {
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
    protected VElement(int mapId, int id, String type, String label, String iconName,
            int x, int y) {
        super(mapId, id, type, label, iconName, x, y);
        isChild = true;
    }    
    
	public abstract String getStatus() throws MapsException;
	public abstract int getSeverity() throws MapsException;
	public abstract double getRtc();
	
	public abstract boolean equals(Object obj);
	public abstract boolean isSubmap();
	public abstract boolean isNode();
    public abstract boolean isLinkedWith(Element mapElement);

    public abstract Element[] getLinkedElement();

    public abstract void cutLinks();
    public abstract void restoreLinks();
    
    // to Factory
    final public boolean isContained(int mapId) throws MapsException {
        try {
            return Factory.isElementInMap(getId(), mapId);
        }catch(SQLException e){
            throw new MapsException();
        }
    }

    public boolean isChild() {
        return isChild;
    }
    
    protected void setMapId(int mapId) {
        super.setMapId(mapId);
        isChild = true;
    }
    
    final public int getContainerMap()throws VElementNotChildException {
    	if(isChild==true)
    		return getMapId();
    	throw new VElementNotChildException();
    }
}

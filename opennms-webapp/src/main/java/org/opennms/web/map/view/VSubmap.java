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
final public class VSubmap extends VElement {
    /**
     * create a VSubMap with all the values of the Element e in input
     * @param e
     */
    public VSubmap(Element e)throws MapsException {
        super(e);
        super.type=MAP_TYPE;
       
    }
    
 /**
  * Create a VSubMap with the id in input and set label with the id too.
  * @param id
  */      
    protected VSubmap(int id) {
        super.setId(id);
        super.setLabel(String.valueOf(id));
        super.type=MAP_TYPE;
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
    VSubmap(int mapId, int id, String label, String iconName,
            int x, int y) throws MapsException{
        super(mapId, id, MAP_TYPE, label, iconName, x, y);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#getRtc()
     */
    public double getRtc() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isSubmap()
     */
    public boolean isSubmap() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isNode()
     */
    public boolean isNode() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isLinkedWith(org.opennms.web.map.db.Element)
     */
    public boolean isLinkedWith(Element mapElement) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#getLinkedElement()
     */
    public Element[] getLinkedElement() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#cutLinks()
     */
    public void cutLinks() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#restoreLinks()
     */
    public void restoreLinks() {
        // TODO Auto-generated method stub
    }
    
   
}

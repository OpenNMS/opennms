/*
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import org.opennms.core.resource.Vault;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.db.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * @author micmas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final public class VNode extends VElement {
    /**
     * @param e
     */
    VNode(Element e) {
        super(e);
        super.setId(e.getId());
        super.type=NODE_TYPE;
    }
    
    /**
     * Create a new VNode with id in input and label retrieved from the 
     * NetworkElementFactory.
     * @param id
     * @throws MapsException if an error occours
     */
    VNode(int id)throws MapsException{
    	try{
    		Node n = NetworkElementFactory.getNode(id);
    		if(n==null){
    			throw new MapsException("Cannot create a VNode with id "+id+". A node with this id doesn't exist.");
    		}
    		super.setLabel(n.getLabel());	
    	}catch(SQLException se){
    		throw new MapsException(se);
    	}
    	super.setId(id);
    	super.type=NODE_TYPE;
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
    VNode(int mapId, int id, String label, String iconName,
            int x, int y) {
        super(mapId, id, NODE_TYPE, label, iconName, x, y);
    }  
    
    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#getRtc()
     */
    public double getRtc() {
        throw new RuntimeException("Not implemented.");
        // TODO 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
    	throw new RuntimeException("Not implemented.");
        // TODO 
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isSubmap()
     */
    public boolean isSubmap() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isNode()
     */
    public boolean isNode() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#isLinkedWith(org.opennms.web.map.interfaces.ElementInterface)
     */
    public boolean isLinkedWith(Element e) {
        // TODO
    	throw new RuntimeException("Not implemented.");
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#getLinkedElement()
     */
    public Element[] getLinkedElement() {
        // TODO 
    	throw new RuntimeException("Not implemented.");
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#cutLinks()
     */
    public void cutLinks() {
        // TODO 
    	throw new RuntimeException("Not implemented.");
    }

    /* (non-Javadoc)
     * @see org.opennms.web.map.temp.VElement#restoreLinks()
     */
    public void restoreLinks() {
        // TODO 
    	throw new RuntimeException("Not implemented.");
    }
}

/*
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;

import org.opennms.web.map.db.Map;

/**
 * @author micmas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
final public class VMap extends Map {
    Hashtable elements = new Hashtable();
    final String DEFAULT_NAME = "";
    /**
     *  Create a new VMap with empty name.
     */
    VMap() {
    	super();
    	super.setName(DEFAULT_NAME);
    }

    protected void setId(int id) {
        super.setId(id);
        VElement[] elements = getAllElements();
        if(elements!=null){
	        for(int i = 0, n = elements.length; i < n; i++) {
	            elements[i].setMapId(id);
	        }
        }
    }
    /**
     * Copy constructor: create a VMap with all properties and elements of the input VMap without 
     * time informations (create time, lastmodifiedtime) and id that will be automatically setted.   
     * @param map
     */
    public VMap(VMap map){
        super();
        setAccessMode(map.getAccessMode());
        setBackground(map.getBackground());
        setName(map.getName());
        setOffsetX(map.getOffsetX());
        setOffsetY(map.getOffsetY());
        setOwner(map.getOwner());
        setScale(map.getScale());
        setType(map.getType());
        setUserLastModifies(map.getUserLastModifies());
        addElements(map.getCloneAllElements());
    }
    
    /**
     * @param id
     * @param name
     * @param background
     * @param owner
     * @param accessMode
     * @param userLastModifies
     * @param scale
     * @param offsetX
     * @param offsetY
     * @param type
     */
    protected VMap(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type) {
        super(id, name, background, owner, accessMode, userLastModifies, scale,
                offsetX, offsetY, type);
    }

    public void addElement(VElement ve) {
        if(ve!=null){
	        elements.put(new Integer(ve.getId()), ve);
	        ve.setMapId(getId());
        }
    }

    public void addElements(VElement[] ve) {
        if(ve!=null){
	        for (int i = 0; i < ve.length; i++)
	            addElement(ve[i]);
        }
    }

    public VElement removeElement(int id) {
        VElement ve = (VElement) elements.remove(new Integer(id));
        if (ve != null) { 
            ve.isChild = false;
        }
        return ve;
    }

    public void removeElements(int[] ids) {
        if(ids!=null){
	        for (int i = 0; i < ids.length; i++) {
	            removeElement(ids[i]);
	        }
        }
    }

    public VElement getElement(int id) {
    	return (VElement) elements.get(new Integer(id));
    }

    public VElement[] getAllElements() {
    	if(elements.size()==0){
    		return null;
    	}
    	VElement[] arrayElems = new VElement[elements.size()];
    	elements.values().toArray(arrayElems);
        return arrayElems;
    } 

    public VElement[] getCloneAllElements() {
    	if(elements.size()==0){
    		return null;
    	}
    	VElement[] arrayElems = new VElement[elements.size()];
    	Iterator iterator = elements.values().iterator();
    	int i = 0;
    	while(iterator.hasNext()) {
    	    arrayElems[i++] = (VElement)((VElement)iterator.next()).clone();
    	}
        return arrayElems;
    }     
    
    public void removeAllElements() {
        elements.clear();
    }
    
    public int size() {
        return elements.size();
    }
    
    public boolean contains(int id) {
        return elements.contains(new Integer(id));
    }
    
    public  void setAccessMode(String accessMode) {
    	super.setAccessMode(accessMode);
    }
    
    public  void setBackground(String background) {
    	super.setBackground(background);
    }
    
    public void setCreateTime(Timestamp createTime){
    	super.setCreateTime(createTime);
    }
    
    public void setLastModifiedTime(Timestamp lastModifiedTime){
    	super.setLastModifiedTime(lastModifiedTime);
    }
    
    public void setName(String name){
    	super.setName(name);
    }
    
    public void setOffsetX(int offsetX){
    	super.setOffsetX(offsetX);
    }
    
    public void setOffsetY(int offsetY){
    	super.setOffsetY(offsetY);
    }
    
    public void setOwner(String owner){
    	super.setOwner(owner);
    }
    
    public void setScale(float scale){
    	super.setScale(scale);
    }
    
    public void setType(String type){
    	super.setType(type);
    }
    
    public void setUserLastModifies(String userLastModifies){
    	super.setUserLastModifies(userLastModifies);
    }
    
    public boolean isNew(){
    	return super.isNew();
    }
}

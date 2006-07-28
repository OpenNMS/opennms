/*
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.util.List;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    
    Hashtable links = new Hashtable();
    
    public static final String DEFAULT_NAME = "NewMap";
    /**
     *  Create a new VMap with empty name.
     */
    VMap() {
    	super();
    	super.setName(DEFAULT_NAME);
    }

    public void setId(int id) {
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
        setWidth(map.getWidth());
        setHeight(map.getHeight());
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
            int offsetX, int offsetY, String type, int width, int height) {
        super(id, name, background, owner, accessMode, userLastModifies, scale,
                offsetX, offsetY, type, width, height);
    }

    public void addElement(VElement ve) {
        if(ve!=null){
        	String elementId = (new Integer(ve.getId()).toString())+ve.getType();
	        elements.put(elementId, ve);
	        ve.setMapId(getId());
        }
    }

    public void addElements(VElement[] ve) {
        if(ve!=null){
	        for (int i = 0; i < ve.length; i++)
	            addElement(ve[i]);
        }
    }

    public void addLink(VLink link) {
    	// add a link only if map contains element.
    	VElement first = link.getFirst();
    	VElement second = link.getSecond();
    	addElement(first);
    	addElement(second);
    	links.put(getLinkId(first.getId(),first.getType(),second.getId(),second.getType()),link);
    }

    public void addLinks(VLink[] vl) {
        if(vl!=null){
	        for (int i = 0; i < vl.length; i++)
	            addLink(vl[i]);
        }
    }
 
    public VElement removeElement(int id, String type) {
    	String elementId = (new Integer(id).toString())+type;
    	VElement ve = (VElement) elements.remove(elementId);
        if (ve != null) { 
            ve.isChild = false;
        }
        return ve;
    }

    public VLink removeLink(int id1, String type1, int id2, String type2) {
    	VLink vl = (VLink) links.remove(getLinkId(id1,type1,id2,type2));
    	return vl;
    }
    
    public VLink[] removeLinksOnElement(int id, String type) {
    	VElement[] elems = getAllElements();
    	List links = new ArrayList();
    	if (elems != null) {
    		for (int i=0; i< elems.length; i++) {
    			VElement ve = (VElement) elems[i];
    			if (containsLink(id,type,ve.getId(),ve.getType())) {
    				links.add(removeLink(id,type,ve.getId(),ve.getType())); 
    			}
    		}
    	}
    	return (VLink[]) links.toArray(new VLink[0]);
    }

    public List removeLinksOnElementList(int id, String type) {
    	VElement[] elems = getAllElements();
    	List links = new ArrayList();
    	if (elems != null) {
    		for (int i=0; i< elems.length; i++) {
    			VElement ve = (VElement) elems[i];
    			if (containsLink(id,type,ve.getId(),ve.getType())) {
    				links.add(removeLink(id,type,ve.getId(),ve.getType())); 
    			}
    		}
    	}
    	return links;
    }

    public void removeElements(int[] ids, String type) {
        if(ids!=null){
	        for (int i = 0; i < ids.length; i++) {
	            removeElement(ids[i],type);
	        }
        }
    }

    public VElement getElement(int id, String type) {
    	String elementId = (new Integer(id).toString())+type;
    	return (VElement) elements.get(elementId);
    }

    public VElement[] getAllElements() {
    	if(elements.size()==0){
    		return null;
    	}
    	VElement[] arrayElems = new VElement[elements.size()];
    	elements.values().toArray(arrayElems);
        return arrayElems;
    } 

    public VLink[] getAllLinks() {
    	if(links.size()==0){
    		return null;
    	}
    	VLink[] arrayLinks = new VLink[links.size()];
    	links.values().toArray(arrayLinks);
        return arrayLinks;
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
    
    public void removeAllLinks() {
        links.clear();
    }
    
    public int size() {
        return elements.size();
    }
    
    public int linksize() {
    	return links.size();
    }

    public boolean containsElement(int id, String type) {
    	String elementId = (new Integer(id).toString())+type;
    	return elements.containsKey(elementId);
    }

    public boolean containsLink(int id1, String type1, int id2, String type2) {
     	return links.containsKey(getLinkId(id1,type1,id2,type2));
    }

    private String getLinkId(int id1, String type1, int id2, String type2) {
    	return (new Integer(id1).toString())+type1+"-"+(new Integer(id2).toString())+type2;
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
    
    public void setWidth(int width){
    	super.setWidth(width);
    }
    
    public void setHeight(int height){
    	super.setHeight(height);
    }
    
    public void setUserLastModifies(String userLastModifies){
    	super.setUserLastModifies(userLastModifies);
    }
    
    public boolean isNew(){
    	return super.isNew();
    }
}

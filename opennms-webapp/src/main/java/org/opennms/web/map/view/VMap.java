//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics, refactor a bit, format code. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
import java.util.Arrays;
import java.util.Collection;
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
    Hashtable<String, VElement> elements = new Hashtable<String, VElement>();
    
    Hashtable<String, VLink> links = new Hashtable<String, VLink>();
    
    public static final String DEFAULT_NAME = "NewMap";
    
    /**
     *  Create a new VMap with empty name.
     */
    public VMap() {
    	super();
    	super.setName(DEFAULT_NAME);
    }

    public void setId(int id) {
        super.setId(id);
        VElement[] elements = getAllElements();
        if (elements != null) {
            for (VElement element : elements) {
	            element.setMapId(id);
	        }
        }
    }
    
    /**
     * Copy constructor: create a VMap with all properties and elements of the input VMap without 
     * time informations (create time, lastmodifiedtime) and id that will be automatically setted.   
     * @param map
     */
    public VMap(VMap map) {
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
     * Copy constructor: create a VMap with all properties and elements of the input VMap without 
     * time informations (create time, lastmodifiedtime) and id that will be automatically setted.   
     * @param map
     */
    public VMap(Map map) {
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
    public VMap(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type, int width, int height) {
        super(id, name, background, owner, accessMode, userLastModifies, scale,
                offsetX, offsetY, type, width, height);
    }
    
    public void addElement(VElement ve) {
        if (ve != null) {
	        elements.put(getElementId(ve), ve);
	        ve.setMapId(getId());
        }
    }

    public void addElements(VElement[] ve) {
        if (ve != null) {
	        for (int i = 0; i < ve.length; i++) {
	            addElement(ve[i]);
            }
        }
    }

    public void addElements(List<VElement> elems) {
        if (elems != null) {
            for (VElement elem : elems) {
                addElement(elem);
            }
        }
    }


    public void addLink(VLink link) {
    	// add a link only if map contains element.
    	VElement first = link.getFirst();
    	VElement second = link.getSecond();
    	addElement(first);
    	addElement(second);
    	links.put(link.getId(), link);
    }

    public void addLinks(VLink[] vl) {
        if (vl != null) {
	        for (VLink link : vl) {
	            addLink(link);
            }
        }
    }
 
    public void addLinks(List<VLink> elems) {
        if (elems != null) {
            for (VLink elem : elems) {
                addLink(elem);
            }
        }
    }


    public VElement removeElement(int id, String type) {
    	VElement ve = elements.remove(getElementId(id, type));
        
        if (ve != null) { 
            ve.isChild = false;
        }
        
        return ve;
    }

    public VLink removeLink(VLink link) {
    	return links.remove(link.getId());
    }
    
    public VLink[] removeLinksOnElement(int id, String type) {
     	VLink[] lnksToDelete = getLinksOnElement(id, type);
    	List<VLink> links = new ArrayList<VLink>();
        
    	for (int i = 0; i < lnksToDelete.length; i++) {
    		links.add(removeLink(lnksToDelete[i])); 
    	}
        
    	return links.toArray(new VLink[links.size()]);
    }
    
    
    public VLink[] getLinksOnElement(int id, String type) {
    	List<VLink> lns = new ArrayList<VLink>();
        
        for (String linkId : links.keySet()) {
    		if (linkId.indexOf(getElementId(id, type)) != -1) {
    			lns.add(links.get(linkId));
    		}
    	}
        
    	return lns.toArray(new VLink[lns.size()]);
    }

    public List<VLink> removeLinksOnElementList(int id, String type) {
    	return Arrays.asList(removeLinksOnElement(id, type));
    }

    public void removeElements(int[] ids, String type) {
        if (ids != null) {
	        for (int id : ids) {
	            removeElement(id, type);
	        }
        }
    }

    public VElement getElement(int id, String type) {
    	return elements.get(getElementId(id, type));
    }

    public VElement[] getAllElements() {
    	if (elements.size() == 0) {
    		return null;
    	}
        
    	return elements.values().toArray(new VElement[elements.size()]);
    } 

    public Collection<VElement> getElements() {        
        return elements.values();
    } 

    public VLink[] getAllLinks() {
    	if (links.size() == 0) {
    		return null;
    	}
        
    	return links.values().toArray(new VLink[links.size()]);
    } 

    public VElement[] getCloneAllElements() {
    	if (elements.size() == 0) {
    		return null;
    	}
        
    	VElement[] arrayElems = new VElement[elements.size()];
    	Iterator<VElement> iterator = elements.values().iterator();
    	int i = 0;
        
    	while (iterator.hasNext()) {
    	    arrayElems[i++] = iterator.next().clone();
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
    	return elements.containsKey(getElementId(id, type));
    }

    public boolean containsLink(VLink link) {
     	return links.containsKey(link.getId());
    }
    
    public  void setAccessMode(String accessMode) {
    	super.setAccessMode(accessMode);
    }
    
    public  void setBackground(String background) {
    	super.setBackground(background);
    }
    
    public void setCreateTime(Timestamp createTime) {
    	super.setCreateTime(createTime);
    }
    
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
    	super.setLastModifiedTime(lastModifiedTime);
    }
    
    public void setName(String name) {
    	super.setName(name);
    }
    
    public void setOffsetX(int offsetX) {
    	super.setOffsetX(offsetX);
    }
    
    public void setOffsetY(int offsetY) {
    	super.setOffsetY(offsetY);
    }
    
    public void setOwner(String owner) {
    	super.setOwner(owner);
    }
    
    public void setScale(float scale) {
    	super.setScale(scale);
    }
    
    public void setType(String type) {
    	super.setType(type);
    }
    
    public void setWidth(int width) {
    	super.setWidth(width);
    }
    
    public void setHeight(int height) {
    	super.setHeight(height);
    }
    
    public void setUserLastModifies(String userLastModifies) {
    	super.setUserLastModifies(userLastModifies);
    }
    
    public boolean isNew() {
    	return super.isNew();
    }

    private String getElementId(VElement element) {
        return element.getId() + element.getType();
    }

    private String getElementId(int id, String type) {
        return id + type;
    }
}

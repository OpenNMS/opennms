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

import java.util.ArrayList;
import java.util.Hashtable;

import java.sql.Timestamp;

import org.opennms.web.map.db.DbMap;

/**
 * <p>VMap class.</p>
 *
 * @author micmas
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 * @since 1.8.1
 */
final public class VMap extends DbMap {
    Hashtable<String, VElement> elements = new Hashtable<String, VElement>();
    
    List<VLink> links = new ArrayList<VLink>();
    
    String createTimeString;
    String lastModifiedTimeString;
    
    /**
     *  Create a new VMap with empty name.
     *
     * @param mapName a {@link java.lang.String} object.
     */
    public VMap(String mapName) {
    	super();
    	super.setName(mapName);
    }
        
    /**
     * <p>Constructor for VMap.</p>
     *
     * @param id a int.
     * @param name a {@link java.lang.String} object.
     * @param background a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param accessMode a {@link java.lang.String} object.
     * @param userLastModifies a {@link java.lang.String} object.
     * @param scale a float.
     * @param offsetX a int.
     * @param offsetY a int.
     * @param type a {@link java.lang.String} object.
     * @param width a int.
     * @param height a int.
     */
    public VMap(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type, int width, int height) {
        super(id, name, background, owner, accessMode, userLastModifies, scale,
                offsetX, offsetY, type, width, height);
    }
    
    /**
     * <p>addElement</p>
     *
     * @param ve a {@link org.opennms.web.map.view.VElement} object.
     */
    public void addElement(VElement ve) {
        if (ve != null) {
	        elements.put(getElementId(ve), ve);
	        ve.setMapId(getId());
        }
    }

    /**
     * <p>addElements</p>
     *
     * @param ve an array of {@link org.opennms.web.map.view.VElement} objects.
     */
    public void addElements(VElement[] ve) {
        if (ve != null) {
	        for (int i = 0; i < ve.length; i++) {
	            addElement(ve[i]);
            }
        }
    }

    /**
     * <p>addElements</p>
     *
     * @param elems a {@link java.util.List} object.
     */
    public void addElements(List<VElement> elems) {
        if (elems != null) {
            for (VElement elem : elems) {
                addElement(elem);
            }
        }
    }


    /**
     * <p>addLink</p>
     *
     * @param link a {@link org.opennms.web.map.view.VLink} object.
     */
    public void addLink(VLink link) {
    	this.links.add(link);
    }
 
    /**
     * <p>addLinks</p>
     *
     * @param links a {@link java.util.List} object.
     */
    public void addLinks(List<VLink> links) {
        this.links.addAll(links);
    }


    /**
     * <p>removeElement</p>
     *
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.map.view.VElement} object.
     */
    public VElement removeElement(int id, String type) {
    	VElement ve = elements.remove(getElementId(id, type));
        
        if (ve != null) { 
            ve.isChild = false;
        }
        
        return ve;
    }

    /**
     * <p>removeLink</p>
     *
     * @param link a {@link org.opennms.web.map.view.VLink} object.
     * @return a {@link org.opennms.web.map.view.VLink} object.
     */
    public VLink removeLink(VLink link) {
        int index = links.indexOf(link);
    	return links.remove(index);
    }
        
    /**
     * <p>getLinksOnElement</p>
     *
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<VLink> getLinksOnElement(int id, String type) {
    	List<VLink> lns = new ArrayList<VLink>();
        
        for (VLink vlink : links) {
    		if (vlink.getId().indexOf(getElementId(id, type)) != -1) {
    			lns.add(vlink);
    		}
    	}
        
    	return lns;
    }

    /**
     * <p>removeLinksOnElementList</p>
     *
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<VLink> removeLinksOnElementList(int id, String type) {
        List<VLink> links = new ArrayList<VLink>();
        
        for (VLink vlink: getLinksOnElement(id, type)) {
            links.add(removeLink(vlink)); 
        }
            
    	return links;
    }

    /**
     * <p>removeElements</p>
     *
     * @param ids an array of int.
     * @param type a {@link java.lang.String} object.
     */
    public void removeElements(int[] ids, String type) {
        if (ids != null) {
	        for (int id : ids) {
	            removeElement(id, type);
	        }
        }
    }

    /**
     * <p>getElement</p>
     *
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.map.view.VElement} object.
     */
    public VElement getElement(int id, String type) {
    	return elements.get(getElementId(id, type));
    }

    /**
     * <p>Getter for the field <code>elements</code>.</p>
     *
     * @return a java$util$Map object.
     */
    public java.util.Map<String,VElement> getElements() {        
        return elements;
    } 

    /**
     * <p>Getter for the field <code>links</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<VLink> getLinks() {
    	return links;
    } 
    
    /**
     * <p>removeAllElements</p>
     */
    public void removeAllElements() {
        elements.clear();
    }
    
    /**
     * <p>removeAllLinks</p>
     */
    public void removeAllLinks() {
        links.clear();
    }
    
    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        return elements.size();
    }
    
    /**
     * <p>linksize</p>
     *
     * @return a int.
     */
    public int linksize() {
    	return links.size();
    }

    /**
     * <p>containsElement</p>
     *
     * @param id a int.
     * @param type a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean containsElement(int id, String type) {
    	return elements.containsKey(getElementId(id, type));
    }

    /**
     * <p>containsLink</p>
     *
     * @param link a {@link org.opennms.web.map.view.VLink} object.
     * @return a boolean.
     */
    public boolean containsLink(VLink link) {
     	return links.contains(link);
    }
    
    /** {@inheritDoc} */
    public  void setAccessMode(String accessMode) {
    	super.setAccessMode(accessMode);
    }
    
    /** {@inheritDoc} */
    public  void setBackground(String background) {
    	super.setBackground(background);
    }
    
    /** {@inheritDoc} */
    public void setCreateTime(Timestamp createTime) {
    	super.setCreateTime(createTime);
    }
    
    /** {@inheritDoc} */
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
    	super.setLastModifiedTime(lastModifiedTime);
    }
    
    /** {@inheritDoc} */
    public void setName(String name) {
    	super.setName(name);
    }
    
    /** {@inheritDoc} */
    public void setOffsetX(int offsetX) {
    	super.setOffsetX(offsetX);
    }
    
    /** {@inheritDoc} */
    public void setOffsetY(int offsetY) {
    	super.setOffsetY(offsetY);
    }
    
    /** {@inheritDoc} */
    public void setOwner(String owner) {
    	super.setOwner(owner);
    }
    
    /** {@inheritDoc} */
    public void setScale(float scale) {
    	super.setScale(scale);
    }
    
    /** {@inheritDoc} */
    public void setType(String type) {
    	super.setType(type);
    }
    
    /** {@inheritDoc} */
    public void setWidth(int width) {
    	super.setWidth(width);
    }
    
    /** {@inheritDoc} */
    public void setHeight(int height) {
    	super.setHeight(height);
    }
    
    /** {@inheritDoc} */
    public void setUserLastModifies(String userLastModifies) {
    	super.setUserLastModifies(userLastModifies);
    }
    
    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
    	return super.isNew();
    }

    private String getElementId(VElement element) {
        return element.getId() + element.getType();
    }

    private String getElementId(int id, String type) {
        return id + type;
    }
    
    /**
     * <p>Getter for the field <code>createTimeString</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreateTimeString() {
        return createTimeString;
    }

    /**
     * <p>Setter for the field <code>createTimeString</code>.</p>
     *
     * @param createTimeString a {@link java.lang.String} object.
     */
    public void setCreateTimeString(String createTimeString) {
        this.createTimeString = createTimeString;
    }

    /**
     * <p>Getter for the field <code>lastModifiedTimeString</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastModifiedTimeString() {
        return lastModifiedTimeString;
    }

    /**
     * <p>Setter for the field <code>lastModifiedTimeString</code>.</p>
     *
     * @param lastModifiedTimeString a {@link java.lang.String} object.
     */
    public void setLastModifiedTimeString(String lastModifiedTimeString) {
        this.lastModifiedTimeString = lastModifiedTimeString;
    }


}

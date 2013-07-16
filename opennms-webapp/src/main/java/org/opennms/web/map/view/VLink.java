/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.map.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.web.map.MapsConstants;

/**
 * <p>VLink class.</p>
 *
 * @author antonio
 * @version $Id: $
 * @since 1.8.1
 */
final public class VLink {
	private final String elem1Type;
	private final int elem1Id;
	private final String elem2Type;
    private final int elem2Id;
    private final Set<Integer> nodeids = new TreeSet<Integer>();

    private final Map<String, Integer> vlinkStatusMap = new HashMap<String, Integer>();
	private int numberOfLinks;
    //the link type defined in the map properties file
	private int linkTypeId;
	
	private String linkStatusString;
	
    private final String id;
	
	/**
	 * <p>Constructor for VLink.</p>
	 *
	 * @param elem1Id a int.
	 * @param elem1Type a {@link java.lang.String} object.
	 * @param elem2Id a int.
	 * @param elem2Type a {@link java.lang.String} object.
	 * @param linkTypeId a int.
	 */
	public VLink(int elem1Id, String elem1Type, int elem2Id, String elem2Type, int linkTypeId) {
		this.elem1Type = elem1Type;
		this.elem2Type = elem2Type;
        this.elem1Id = elem1Id;
        this.elem2Id = elem2Id;
        this.linkTypeId = linkTypeId;
        this.numberOfLinks = 1;
        String  a = elem1Id+elem1Type;
        String  b = elem2Id+elem2Type;
        String id = a + "-" + b;
        
        if (elem1Id > elem2Id) {
            id = b + "-" + a;
        }
        
        if (elem1Id == elem2Id && elem2Type.equals(MapsConstants.MAP_TYPE)) {
            id = b + "-" + a;
        }
		id = id+"-"+linkTypeId;
		this.id = id;
	}
	
	/**
	 * <p>Getter for the field <code>vlinkStatusMap</code>.</p>
	 *
	 * @return a java$util$Map object.
	 */
	public Map<String, Integer> getVlinkStatusMap() {
        return vlinkStatusMap;
    }

    /**
     * <p>Setter for the field <code>vlinkStatusMap</code>.</p>
     *
     * @param vlinkStatusMap a java$util$Map object.
     */
    public void setVlinkStatusMap(final Map<String, Integer> vlinkStatusMap) {
        if (this.vlinkStatusMap == vlinkStatusMap) return;
        this.vlinkStatusMap.clear();
        this.vlinkStatusMap.putAll(vlinkStatusMap);
    }

    /**
     * <p>Getter for the field <code>numberOfLinks</code>.</p>
     *
     * @return a int.
     */
    public int getNumberOfLinks() {
        return numberOfLinks;
    }

    /**
     * <p>Setter for the field <code>numberOfLinks</code>.</p>
     *
     * @param numberOfLinks a int.
     */
    public void setNumberOfLinks(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
    }

    /**
     * <p>Getter for the field <code>linkStatusString</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLinkStatusString() {
	    return linkStatusString;
	}

    /**
     * <p>Setter for the field <code>linkStatusString</code>.</p>
     *
     * @param linkStatusString a {@link java.lang.String} object.
     */
    public void setLinkStatusString(String linkStatusString) {
        this.linkStatusString = linkStatusString;
    }

	/**
	 * {@inheritDoc}
	 *
	 * Asserts if the links are linking the same elements without considering their statuses
	 */
        @Override
	public boolean equals(Object otherLink) {
		if (!(otherLink instanceof VLink)) return false;
		VLink link = (VLink) otherLink;
		if ( !getId().equals(link.getId())) return false;
		for (int nodeid : getNodeids()) {
		    if (!link.getNodeids().contains(nodeid))
		        return false;
		}
        for (int nodeid : link.getNodeids()) {
            if (!getNodeids().contains(nodeid))
                return false;
        }

		return true;
	}
	
	/**
	 * <p>getFirst</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFirst() {
		return elem1Id+elem1Type;
	}

	/**
	 * <p>getSecond</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSecond() {
		return elem2Id+elem2Type;
	}
	
	/**
	 * <p>Getter for the field <code>linkTypeId</code>.</p>
	 *
	 * @return a int.
	 */
	public int getLinkTypeId() {
		return linkTypeId;
	}
	
	/**
	 * <p>Setter for the field <code>linkTypeId</code>.</p>
	 *
	 * @param typeId a int.
	 */
	public void setLinkTypeId(int typeId) {
		linkTypeId = typeId;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
			return ""+getFirst()+"-"+getSecond()+"-"+linkTypeId+"-"+linkStatusString+" nodeids:"+this.nodeids.toString();
	}
	
    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
		return id;
	}
    
    /**
     * <p>getIdWithoutLinkType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIdWithoutLinkType() {
        return id.substring(0, id.lastIndexOf("-"));
    }
    /**
     * <p>increaseLinks</p>
     *
     * @return a int.
     */
    public int increaseLinks() {
        return ++numberOfLinks;
    }

    /**
     * <p>increaseStatusMapLinks</p>
     *
     * @param statusString a {@link java.lang.String} object.
     * @return a int.
     */
    public int increaseStatusMapLinks(String statusString) {
        int i=0;
        if (vlinkStatusMap.containsKey(statusString)) {
            i = vlinkStatusMap.get(statusString);
        } 
        vlinkStatusMap.put(statusString, ++i);
        return i;
    }

    /**
     * <p>Getter for the field <code>nodeids</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getNodeids() {
        return nodeids;
    }

    /**
     * <p>Setter for the field <code>nodeids</code>.</p>
     *
     * @param nodeids a {@link java.util.Set} object.
     */
    public void setNodeids(final Set<Integer> nodeids) {
        if (this.nodeids == nodeids) return;
        this.nodeids.clear();
        nodeids.addAll(nodeids);
    }

}

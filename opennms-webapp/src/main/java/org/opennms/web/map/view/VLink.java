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
 * Created on 8-mag-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.web.map.MapsConstants;

/**
 * @author antonio
 * 
 */

final public class VLink {
	private String elem1Type;
	private int elem1Id;
	private String elem2Type;
    private int elem2Id;	
    private Set<Integer> nodeids;

    private Map<String, Integer> vlinkStatusMap;
	private int numberOfLinks;
    //the link type defined in the map properties file
	private int linkTypeId;
	
	private String linkStatusString;
	
    private String id;
	
	public VLink(int elem1Id, String elem1Type, int elem2Id, String elem2Type, int linkTypeId) {
		this.elem1Type = elem1Type;
		this.elem2Type = elem2Type;
        this.elem1Id = elem1Id;
        this.elem2Id = elem2Id;
        this.linkTypeId = linkTypeId;
        this.numberOfLinks = 1;
        this.vlinkStatusMap = new HashMap<String, Integer>();
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
		this.nodeids = new TreeSet<Integer>();
	}
	
	public Map<String, Integer> getVlinkStatusMap() {
        return vlinkStatusMap;
    }

    public void setVlinkStatusMap(Map<String, Integer> vlinkStatusMap) {
        this.vlinkStatusMap = vlinkStatusMap;
    }

    public int getNumberOfLinks() {
        return numberOfLinks;
    }

    public void setNumberOfLinks(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
    }

    public String getLinkStatusString() {
	    return linkStatusString;
	}

    public void setLinkStatusString(String linkStatusString) {
        this.linkStatusString = linkStatusString;
    }

    /**
	 * Asserts if the links are linking the same elements without considering their statuses
	 */
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
	
	public String getFirst() {
		return elem1Id+elem1Type;
	}

	public String getSecond() {
		return elem2Id+elem2Type;
	}
	
	public int getLinkTypeId() {
		return linkTypeId;
	}
	
	public void setLinkTypeId(int typeId) {
		linkTypeId = typeId;
	}

	public String toString() {
			return ""+getFirst()+"-"+getSecond()+"-"+linkTypeId+"-"+linkStatusString+" nodeids:"+this.nodeids.toString();
	}
	
    public String getId() {
		return id;
	}
    
    public String getIdWithoutLinkType() {
        return id.substring(0, id.lastIndexOf("-"));
    }
    public int increaseLinks() {
        return ++numberOfLinks;
    }

    public int increaseStatusMapLinks(String statusString) {
        int i=0;
        if (vlinkStatusMap.containsKey(statusString)) {
            i = vlinkStatusMap.get(statusString);
        } 
        vlinkStatusMap.put(statusString, ++i);
        return i;
    }

    public Set<Integer> getNodeids() {
        return nodeids;
    }

    public void setNodeids(Set<Integer> nodeids) {
        this.nodeids = nodeids;
    }

}

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

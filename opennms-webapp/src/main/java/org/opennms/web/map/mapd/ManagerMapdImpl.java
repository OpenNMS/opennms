/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map.mapd;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.web.map.InitializationObj;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.MapsException;

import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;


/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class ManagerMapdImpl implements Manager {

    public void clearMap() throws MapNotFoundException, MapsException {
        // TODO Auto-generated method stub
        
    }

    public void closeMap() {
        // TODO Auto-generated method stub
        
    }

    public VMap createMapByLabelSearch(String label, String user)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteMap() throws MapsException, MapNotFoundException {
        // TODO Auto-generated method stub
        
    }

    public boolean foundLoopOnMaps(VMap parentMap, int mapId)
            throws MapsException {
        // TODO Auto-generated method stub
        return false;
    }

    public VElementInfo[] getAllElementInfo() throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<Integer, Integer> getAllNodesOccursOnMap(VMap map)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VMapInfo getDefaultMapsMenu(String user) throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VElement getElement(int mapId, int elementId, String type)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VElementInfo[] getElementInfoLike(String like)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public InitializationObj getInitObj(boolean isUserAdmin)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<VLink> getLinks(Collection<VElement> elems)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Set<Integer>> getNodeLabelToMaps(String user)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<VMapInfo> getVisibleMapsMenu(String user)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VElement newElement(int elementId, String type)
            throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VElement newElement(int mapId, int elementId, String type,
            String iconname, int x, int y) throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VMap newMap(String name, String accessMode, String owner,
            String userModifies, int width, int height) {
        // TODO Auto-generated method stub
        return null;
    }

    public VMap openMap() throws MapNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    public VMap openMap(int id, String user, boolean refreshElems)
            throws MapNotFoundException, MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VMap refreshMap(VMap map) throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public VMap reloadMap(VMap map) throws MapsException {
        // TODO Auto-generated method stub
        return null;
    }

    public void save(VMap map) throws MapsException {
        // TODO Auto-generated method stub
        
    }

}

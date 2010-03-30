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

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
package org.opennms.web.map;

import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONSerializer;

//import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
import org.opennms.web.map.view.VProperties;

public class ResponseAssembler {
	
	//TODO change the ApplicationMap
	@SuppressWarnings("unchecked")
    protected static String getRefreshResponse(String action, VMap map){		
		Map refreshResponseMap = new HashMap();
		refreshResponseMap.put("elems",map.getElements().values());
		refreshResponseMap.put("links", map.getLinks().values());
		return JSONSerializer.toJSON(refreshResponseMap).toString();
	}

    //TODO change the ApplicationMap
    @SuppressWarnings("unchecked")
	protected static String getAddElementResponse(String action,  List<Integer> mapsWithLoopInfo, Collection<VElement> elems, Collection<VLink> links){
		Map addElementResponseMap = new HashMap();
        addElementResponseMap.put("mapsWithLoop",mapsWithLoopInfo);
        addElementResponseMap.put("elems",elems);
        addElementResponseMap.put("links", links);
        return JSONSerializer.toJSON(addElementResponseMap).toString();		
	}
	
    //TODO change the ApplicationMap
	protected static String getDeleteElementsResponse(String action, List<VElement> velems){
        return JSONSerializer.toJSON(velems).toString();     
	}
		
	protected static String getLoadNodesResponse(String action, List<VElementInfo> elemInfos){
		return JSONSerializer.toJSON(elemInfos).toString();
	}
	
   protected static String getLoadMapsResponse(String action, List<VMapInfo> maps) {
       return JSONSerializer.toJSON(maps).toString();
    }

   //TODO change the ApplicationMap
   protected static String getLoadDefaultMapResponse(String action, VMapInfo map) {
       return JSONSerializer.toJSON(map).toString();
    }

	@SuppressWarnings("unchecked")
    protected static String getSaveMapResponse(String action,VMap map){
		Map saveMapResponse = new HashMap();
		saveMapResponse.put("id", map.getId());
		saveMapResponse.put("accessMode",map.getAccessMode());
        saveMapResponse.put("owner",map.getOwner());
        saveMapResponse.put("userLastModifies",map.getUserLastModifies());

        SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
        Date now = new Date();
        if (map.getLastModifiedTime() != null)
            saveMapResponse.put("lastModifiedTimeString", formatter.format(map.getLastModifiedTime()));
        else
            saveMapResponse.put("lastModifiedTimeString", formatter.format(now));


        if (map.getCreateTime() != null)
            saveMapResponse.put("createTimeString",formatter.format(map.getCreateTime()));
        else 
            saveMapResponse.put("createTimeString",formatter.format(now));
        
        return JSONSerializer.toJSON(saveMapResponse).toString(); 
	}
	
	protected static String getMapResponse(VMap map) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
        Date now = new Date();
        if (map.getLastModifiedTime() != null)
            map.setLastModifiedTimeString(formatter.format(map.getLastModifiedTime()));
        else
            map.setLastModifiedTimeString(formatter.format(now));

        if (map.getCreateTime() != null)
            map.setCreateTimeString(formatter.format(map.getCreateTime()));
        else 
            map.setCreateTimeString(formatter.format(now));

        return JSONSerializer.toJSON(map).toString(); 
	}
	
    protected static String getStartupResponse(VProperties initObj)throws Exception{
         return JSONSerializer.toJSON(initObj).toString();
    }

    protected static String getLoadLabelMapResponse(Map<String, Set<Integer>> labelMap) {
        return JSONSerializer.toJSON(labelMap).toString();
    }

	protected static String getActionOKMapResponse(String action) {
		return action+"OK";
	}

	protected static String getMapErrorResponse(String action) {
		return action+"Failed";
	}


}

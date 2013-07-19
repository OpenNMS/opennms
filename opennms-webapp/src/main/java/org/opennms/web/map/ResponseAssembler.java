/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

/**
 * <p>ResponseAssembler class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
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

import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
import org.opennms.web.map.view.VProperties;
public class ResponseAssembler {
	
	/**
	 * <p>getRefreshResponse</p>
	 *
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @return a {@link java.lang.String} object.
	 */
    protected static String getRefreshResponse(VMap map){		
		Map<String,Object> refreshResponseMap = new HashMap<String,Object>();
		refreshResponseMap.put("elems",map.getElements().values());
		refreshResponseMap.put("links", map.getLinks());
		return JSONSerializer.toJSON(refreshResponseMap).toString();
	}

    /**
     * <p>getAddElementResponse</p>
     *
     * @param mapsWithLoopInfo a {@link java.util.List} object.
     * @param elems a {@link java.util.Collection} object.
     * @param links a {@link java.util.Collection} object.
     * @return a {@link java.lang.String} object.
     */
	protected static String getAddElementResponse(List<Integer> mapsWithLoopInfo, Collection<VElement> elems, Collection<VLink> links){
		Map<String,Object> addElementResponseMap = new HashMap<String,Object>();
        addElementResponseMap.put("mapsWithLoop",mapsWithLoopInfo);
        addElementResponseMap.put("elems",elems);
        addElementResponseMap.put("links", links);
        return JSONSerializer.toJSON(addElementResponseMap).toString();		
	}
	
	/**
	 * <p>getDeleteElementsResponse</p>
	 *
	 * @param velemsids a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getDeleteElementsResponse(List<String> velemsids){
        return JSONSerializer.toJSON(velemsids).toString();     
	}
		
	/**
	 * <p>getLoadNodesResponse</p>
	 *
	 * @param elemInfos a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getLoadNodesResponse(List<VElementInfo> elemInfos){
		return JSONSerializer.toJSON(elemInfos).toString();
	}
	
   /**
    * <p>getLoadMapsResponse</p>
    *
    * @param vmapinfos a {@link java.util.List} object.
    * @return a {@link java.lang.String} object.
    */
   protected static String getLoadMapsResponse(List<VMapInfo> vmapinfos) {
       return JSONSerializer.toJSON(vmapinfos).toString();
    }

   /**
    * <p>getLoadDefaultMapResponse</p>
    *
    * @param vmapinfo a {@link org.opennms.web.map.view.VMapInfo} object.
    * @return a {@link java.lang.String} object.
    */
   protected static String getLoadDefaultMapResponse(VMapInfo vmapinfo) {
       return JSONSerializer.toJSON(vmapinfo).toString();
    }

	/**
	 * <p>getSaveMapResponse</p>
	 *
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @return a {@link java.lang.String} object.
	 */
    protected static String getSaveMapResponse(VMap map){
		Map<String,Object> saveMapResponse = new HashMap<String,Object>();
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
	
	/**
	 * <p>getMapResponse</p>
	 *
	 * @param map a {@link org.opennms.web.map.view.VMap} object.
	 * @return a {@link java.lang.String} object.
	 */
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
	
    /**
     * <p>getStartupResponse</p>
     *
     * @param initObj a {@link org.opennms.web.map.view.VProperties} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    protected static String getStartupResponse(VProperties initObj)throws Exception{
         return JSONSerializer.toJSON(initObj).toString();
    }

    /**
     * <p>getLoadLabelMapResponse</p>
     *
     * @param labelMap a {@link java.util.Map} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String getLoadLabelMapResponse(Map<String, Set<Integer>> labelMap) {
        return JSONSerializer.toJSON(labelMap).toString();
    }

	/**
	 * <p>getActionOKMapResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getActionOKMapResponse(String action) {
		return action+MapsConstants.success_string;
	}

	/**
	 * <p>getMapErrorResponse</p>
	 *
	 * @param action a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected static String getMapErrorResponse(String action) {
		return action+MapsConstants.failed_string;
	}


}

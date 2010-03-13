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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONSerializer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;

public class ResponseAssembler {
	private static Category log;
	
	protected static String getRefreshResponse(String action, VMap map){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		
		//checks for only changed velements 
		String response=getActionOKMapResponse(action);
		for(VElement ve: map.getElements().values()){
			response += "&" + ve.getId() + ve.getType() + "+"
					+ ve.getIcon() + "+" + ve.getLabel();
			response += "+" + ve.getAvail() + "+"
					+ ve.getStatus() + "+" + ve.getSeverity()+ "+" + ve.getX()+ "+" + ve.getY();
		}
		// construct string response considering links also
		for(VLink vl : map.getLinks().values()){
			response += "&" + vl.getFirst()+"+"
			+ vl.getSecond()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkStatusString()
			+ "+" + vl.getFirstNodeid()+"+"+vl.getSecondNodeid();
		} 
		log.debug("getRefreshResponse: String assembled: "+response);
		return response;
	}
	
	protected static String getAddMapsResponse(String action,  List<Integer> mapsWithLoopInfo, List<VElement> velems, List<VLink> links){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		
		Iterator<Integer>  ite = mapsWithLoopInfo.iterator();
		while(ite.hasNext()){
			Integer entry = ite.next();
			//if loop is found
			response += "&loopfound" + entry;
			log.debug("found loop for map "+entry);
		}
		
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType() + "+"
			+ ve.getIcon() + "+" + ve.getLabel();
			response += "+" + ve.getAvail() + "+"
			+ ve.getStatus() + "+" + ve.getSeverity() + "+" + "ADDED";
		}
		
		// add String to return containing Links
		if (velems != null) {
			Iterator<VLink> sub_ite = links.iterator();
			while (sub_ite.hasNext()) {
				VLink vl = sub_ite.next();
				response += "&" + vl.getFirst()+ "+"
						+ vl.getSecond()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkStatusString()
	                    + "+" + vl.getFirstNodeid()+"+"+vl.getSecondNodeid();			
				}
		}		
		log.debug("getAddMapsResponse: String assembled: "+response);
		return response;		
	}
	
	protected static String getDeleteElementsResponse(String action, List<VElement> velems){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType();
		}
		log.debug("getDeleteElementsResponse: String assembled: "+response);
		return response;
	}
	
	protected static String getAddElementResponse(String action, List<VElement> velems, List<VLink> links){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String response = getActionOKMapResponse(action);
		Iterator<VElement>it = velems.iterator();
		while(it.hasNext()){
			VElement ve = it.next();
			response += "&" + ve.getId() + ve.getType() + "+"
			+ ve.getIcon() + "+" + ve.getLabel();
			response += "+" + ve.getAvail() + "+"
			+ ve.getStatus() + "+" + ve.getSeverity() + "+"+"ADDED";
		}
		
		// add String to return containing Links
		if (velems != null) {
			Iterator<VLink> sub_ite = links.iterator();
			while (sub_ite.hasNext()) {
				VLink vl = sub_ite.next();
				response += "&" + vl.getFirst()+ "+"
						+ vl.getSecond()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkStatusString()
	                    + "+" + vl.getFirstNodeid()+"+"+vl.getSecondNodeid();			
				}
		}		
		log.debug("getAddElementResponse: String assembled: "+response);
		return response;
	}

	
	protected static String getLoadNodesResponse(String action, VElementInfo[] elemInfos){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		String strToSend =getActionOKMapResponse(action);
		if(elemInfos!=null){
			for (int i = 0; i < elemInfos.length; i++) {
				VElementInfo n = elemInfos[i];
				if (i > 0) {
					strToSend += "&";
				}

				String nodeStr = n.getId() + "+" + n.getLabel();
				strToSend += nodeStr;
			}
		}
		log.debug("getLoadNodesResponse: String assembled: "+strToSend);
		return strToSend;
	}
	
	protected static String getSaveMapResponse(String action,VMap map){
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
	 String strToSend =getActionOKMapResponse(action)+map.getId()
		+ "+"
		+ map.getBackground()
		+ "+"
		+ map.getAccessMode()
		+ "+"
		+ map.getName()
		+ "+"
		+ map.getOwner()
		+ "+"
		+ map.getUserLastModifies()
		+ "+"
		+ ((map.getCreateTime() != null) ? formatter.format(map
				.getCreateTime()) : "")
		+ "+"
		+ ((map.getLastModifiedTime() != null) ? formatter
				.format(map.getLastModifiedTime()) : "");
		log.debug("getSaveMapResponse: String assembled: "+strToSend);
	 return strToSend;
	}
	
	protected static String getMapResponse(VMap map) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
        map.setLastModifiedTimeString(formatter.format(map.getLastModifiedTime()));
        map.setCreateTimeString(formatter.format(map.getCreateTime()));

		 return JSONSerializer.toJSON(map).toString(); 
	}
	
	protected static String getMapsResponse(String action, List<VMapInfo> maps) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		String strToSend=getActionOKMapResponse(action);
				
		if(maps!=null){
			// create the string containing the main informations about maps
			// the string will have the form:
			// mapid1+mapname1+mapowner1&mapid2+mapname2+mapowner2...
			for (int i = 0; i < maps.size(); i++) {
				if (i > 0) {
					strToSend += "&";
				}
				VMapInfo map = (VMapInfo) maps.get(i);
				strToSend += map.getId() + "+" + map.getName() + "+" + map.getOwner();
			}
		} else {
			strToSend=getMapErrorResponse(action);
		}
		log.debug("getMapsResponse: String assembled: "+strToSend);
		return strToSend;
	}

	   protected static String getMapsResponse(String action, VMapInfo map) {
	        ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
	        log = ThreadCategory.getInstance(ResponseAssembler.class);
	        
	        String strToSend=getActionOKMapResponse(action);
	                
	        if(map!=null){
	            // create the string containing the main informations about maps
	            // the string will have the form:
	            // mapid1+mapname1+mapowner1&mapid2+mapname2+mapowner2...
	                strToSend += map.getId() + "+" + map.getName() + "+" + map.getOwner();
	        } else {
	            strToSend=getMapErrorResponse(action);
	        }
	        log.debug("getMapsResponse: String assembled: "+strToSend);
	        return strToSend;
	    }

	protected static String getCloseMapResponse(String action) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(ResponseAssembler.class);
		
		String strToSend = action+"OK";
		strToSend += MapsConstants.MAP_NOT_OPENED + "+" + MapsConstants.DEFAULT_BACKGROUND_COLOR;
		log.debug("getCloseMapResponse: String assembled: "+strToSend);
		return strToSend;
		
	}
	
    protected static String getStartupResponse(InitializationObj initObj)throws Exception{
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

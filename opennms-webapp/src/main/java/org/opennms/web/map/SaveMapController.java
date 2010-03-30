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
package org.opennms.web.map;


import java.io.BufferedWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.web.WebSecurityUtils;

import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.*;

/**
 * @author mmigliore
 * @author antonio@opennms.it
 */

public class SaveMapController implements Controller {

	Category log;

	private Manager manager;


	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	private static List<VElement> elems = null;

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));
		if (!request.isUserInRole(org.opennms.web.springframework.security.Authentication.ADMIN_ROLE)) {
			log.warn("User is not in Admin mode, cannot save");
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.SAVEMAP_ACTION));
			bw.close();
			return null;
			
		}
			
		String mapName = request.getParameter("MapName");
		String mapBackground = request.getParameter("MapBackground");
		int mapWidth = WebSecurityUtils.safeParseInt(request.getParameter("MapWidth"));
		int mapHeight = WebSecurityUtils.safeParseInt(request.getParameter("MapHeight"));
		
		String query = request.getQueryString();
		String queryNodes = request.getParameter("Nodes");
		
			log.debug("Saving map " + mapName + " the query received is '" + query + "'");
	        log.debug("Saving map " + mapName + " the data received is '" + queryNodes + "'");

		try {
			VMap map = manager.openMap();

			log.debug("Instantiating new elems ArrayList");
			elems = new ArrayList<VElement>();

			StringTokenizer st = new StringTokenizer(queryNodes, "*");
			while (st.hasMoreTokens()) {
				String nodeToken = st.nextToken();
				StringTokenizer nodeST = new StringTokenizer(nodeToken, ",");
				int counter = 1;
				String icon = "";
				String type = MapsConstants.NODE_TYPE;

				int id = 0, x = 0, y = 0;
				while (nodeST.hasMoreTokens()) {
					String tmp = nodeST.nextToken();
					if (counter == 1) {
						id = WebSecurityUtils.safeParseInt(tmp);
					}
					if (counter == 2) {
						x = WebSecurityUtils.safeParseInt(tmp);
					}
					if (counter == 3) {
						y = WebSecurityUtils.safeParseInt(tmp);
					}
					if (counter == 4) {
						icon = tmp;
					}
					if (counter == 5) {
						type = tmp;
					}
					counter++;
				}
				if (!type.equals(MapsConstants.NODE_TYPE)
						&& !type.equals(MapsConstants.MAP_TYPE)) {
					throw new MapsException("Map element type " + type
							+ " not valid! Valid values are:"
							+ MapsConstants.NODE_TYPE + " and "
							+ MapsConstants.MAP_TYPE);
				}
				
				String label=null;
                if (map.getElement(id, type) != null && map.getElement(id, type).getLabel() != null) {
                    log.debug("preserving the label: " + map.getElement(id, type).getLabel());
                    label = map.getElement(id, type).getLabel();
                }                
                
                VElement ve = manager.newElement(map.getId(), id, type, icon, x, y);
                if (label != null )
                    ve.setLabel(label);                    
                
                log.debug("adding map element to map with id: " +id+type + " and label: " + ve.getLabel());
				elems.add(ve);
			}

			map.removeAllLinks();
			map.removeAllElements();
				
			map.addElements(elems);

				
			map.setUserLastModifies(request.getRemoteUser());
			map.setName(mapName);
			map.setBackground(mapBackground);
			map.setWidth(mapWidth);
			map.setHeight(mapHeight);
			
			if (map.isNew()) {
			    log.debug("Map is New Map");
				map.setType(MapsConstants.USER_GENERATED_MAP);
				map.setAccessMode(MapsConstants.ACCESS_MODE_ADMIN);
			}
			else if (map.getType().trim().equalsIgnoreCase(MapsConstants.AUTOMATICALLY_GENERATED_MAP)) {
                log.debug("Map is Automated Map, saving as Static");
                map.setType(MapsConstants.AUTOMATIC_SAVED_MAP);
			}
			int mapId = manager.save(map);

	        log.info(map.getName() + " Map saved. " + "With map id: " + mapId);

			if (map.isNew()) 
			    map.setId(mapId);

			bw.write(ResponseAssembler.getSaveMapResponse(MapsConstants.SAVEMAP_ACTION, map));
		} catch (Exception e) {
			log.error("Map save error: " + e,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.SAVEMAP_ACTION));
		} finally {
			bw.close();
			log.info("Sending response to the client");
		}
		return null;
	}

}

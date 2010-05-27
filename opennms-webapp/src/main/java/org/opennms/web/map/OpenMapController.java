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

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.opennms.core.utils.ThreadCategory;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * @author mmigliore
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class OpenMapController implements Controller {
	ThreadCategory log;

	private Manager manager;
	
	
	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		log.debug(request.getQueryString());
		String mapIdStr = request.getParameter("MapId");
		log.debug("MapId=" + mapIdStr);
		String mapWidthStr = request.getParameter("MapWidth");
        log.debug("MapWidth=" + mapWidthStr);
        String mapHeightStr = request.getParameter("MapHeight");
        log.debug("MapHeight=" + mapHeightStr);
        String adminModeStr = request.getParameter("adminMode");
        log.debug("adminMode=" + adminModeStr);
		
		String user = request.getRemoteUser();
		
		if ((request.isUserInRole(org.opennms.web.springframework.security.Authentication.ADMIN_ROLE))) {
			log.info(user + " has Admin admin Role");
		}					

		float widthFactor = 1;
		float heightFactor =1;
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));

		try {
			int mapWidth = WebSecurityUtils.safeParseInt(mapWidthStr);
			int mapHeight = WebSecurityUtils.safeParseInt(mapHeightStr);
			
			log.debug("Current mapWidth=" + mapWidth
						+ " and MapHeight=" + mapHeight);
			VMap map = null;
			if(mapIdStr!=null){
				int mapid = WebSecurityUtils.safeParseInt(mapIdStr);
				log.debug("Opening map "+mapid+" for user "+user);
				map = manager.openMap(mapid, user, !(adminModeStr.equals("true")));
			}else{
				log.debug("Try to Opening default map");
				VMapInfo defaultmapinfo = manager.getDefaultMapsMenu(user);
				if (defaultmapinfo != null ) {
	                map = manager.openMap(defaultmapinfo.getId(),user,!(adminModeStr.equals("true")));
				} else {
				    map = manager.openMap();
				}
			}
			 

			if(map != null){
				int dbMapWidth = map.getWidth();
				int dbMapHeight = map.getHeight();
				widthFactor = (float) mapWidth / dbMapWidth;
				heightFactor = (float) mapHeight / dbMapHeight;

				log.debug("Old saved mapWidth=" + dbMapWidth
						+ " and MapHeight=" + dbMapHeight);
				log.debug("widthFactor=" + widthFactor);
				log.debug("heightFactor=" + heightFactor);
				log.debug("Setting new width and height to the session map");
				
				map.setHeight(mapHeight);
				map.setWidth(mapWidth);
				
				for (VElement ve : map.getElements().values()) {
				    ve.setX((int) (ve.getX() * widthFactor));
                    ve.setY((int) (ve.getY() * heightFactor));
				}
			}
			
			bw.write(ResponseAssembler.getMapResponse(map));

		} catch (Exception e) {
			log.error("Error while opening map with id:"+mapIdStr+", for user:"+user,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.OPENMAP_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}
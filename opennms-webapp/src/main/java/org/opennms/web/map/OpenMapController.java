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


import org.slf4j.MDC;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.WebSecurityUtils;

import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>OpenMapController class.</p>
 *
 * @author mmigliore
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class OpenMapController implements Controller {
	
	private static final Logger LOG = LoggerFactory.getLogger(OpenMapController.class);


	private Manager manager;
	
	
	/**
	 * <p>Getter for the field <code>manager</code>.</p>
	 *
	 * @return a {@link org.opennms.web.map.view.Manager} object.
	 */
	public Manager getManager() {
		return manager;
	}

	/**
	 * <p>Setter for the field <code>manager</code>.</p>
	 *
	 * @param manager a {@link org.opennms.web.map.view.Manager} object.
	 */
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	/** {@inheritDoc} */
        @Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Logging.putPrefix(MapsConstants.LOG4J_CATEGORY);
		

		LOG.debug(request.getQueryString());
		String mapIdStr = request.getParameter("MapId");
		LOG.debug("MapId={}", mapIdStr);
		String mapWidthStr = request.getParameter("MapWidth");
        LOG.debug("MapWidth={}", mapWidthStr);
        String mapHeightStr = request.getParameter("MapHeight");
        LOG.debug("MapHeight={}", mapHeightStr);
        String adminModeStr = request.getParameter("adminMode");
        LOG.debug("adminMode={}", adminModeStr);
		
		String user = request.getRemoteUser();
		
		if ((request.isUserInRole(org.opennms.web.springframework.security.Authentication.ROLE_ADMIN))) {
			LOG.info("{} has Admin admin Role", user);
		}					

		float widthFactor = 1;
		float heightFactor =1;
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));

		try {
			int mapWidth = WebSecurityUtils.safeParseInt(mapWidthStr);
			int mapHeight = WebSecurityUtils.safeParseInt(mapHeightStr);
			
			LOG.debug("Current mapWidth={} and MapHeight={}", mapWidth, mapHeight);
			VMap map = null;
			if(mapIdStr!=null){
				int mapid = WebSecurityUtils.safeParseInt(mapIdStr);
				LOG.debug("Opening map {} for user {}", mapid, user);
				map = manager.openMap(mapid, user, !(adminModeStr.equals("true")));
			}else{
				LOG.debug("Try to Opening default map");
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

				LOG.debug("Old saved mapWidth={} and MapHeight={}", dbMapWidth, dbMapHeight);
				LOG.debug("widthFactor={}", widthFactor);
				LOG.debug("heightFactor={}", heightFactor);
				LOG.debug("Setting new width and height to the session map");
				
				map.setHeight(mapHeight);
				map.setWidth(mapWidth);
				
				for (VElement ve : map.getElements().values()) {
				    ve.setX((int) (ve.getX() * widthFactor));
                    ve.setY((int) (ve.getY() * heightFactor));
				}
			}
			
			bw.write(ResponseAssembler.getMapResponse(map));

		} catch (Throwable e) {
			LOG.error("Error while opening map with id:{}, for user:{}", mapIdStr, user,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.OPENMAP_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}

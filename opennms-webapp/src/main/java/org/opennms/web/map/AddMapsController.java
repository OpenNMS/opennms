/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;


import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * @author mmigliore
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class AddMapsController implements Controller {
	Category log;

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
		String action = request.getParameter("action");
		String elems = request.getParameter("elems");
		log.debug("Adding Maps; action:"+action+", elems="+elems );
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		try {
			if (!request.isUserInRole(org.opennms.web.springframework.security.Authentication.ADMIN_ROLE)) {
				log.warn(request.getRemoteUser() +": Cannot add maps because user role is:" + MapsConstants.ROLE_USER);
				throw new MapsException(request.getRemoteUser() +": Cannot add maps because user role is:" + MapsConstants.ROLE_USER);
			}
			VMap map = manager.openMap();
			if(log.isDebugEnabled())
				log.debug("Got map from manager "+map);
			
			Integer[] mapids = null;
			String type = VElement.MAP_TYPE;

			boolean actionfound = false;
			
			if (action.equals(MapsConstants.ADDMAPS_ACTION)) {
				log.debug("Adding maps by id: "+ elems);
				actionfound = true;
				String[] smapids = elems.split(",");
				mapids = new Integer[smapids.length];
				for (int i = 0; i<smapids.length;i++) {
					mapids[i] = new Integer(smapids[i]);
				}
			}
			
			List<VElement> velems = new ArrayList<VElement>();
			List<VLink> links = new ArrayList<VLink>();
			// response for addElement
			List<Integer> mapsWithLoop = new ArrayList<Integer>();
			if (actionfound) {
				
				for (int i = 0; i < mapids.length; i++) {
					int elemId = mapids[i].intValue();
					if (map.containsElement(elemId, type)) {
						log.debug("Action: " + action + " . Map Contains Element: " + elemId+type);
						continue;
						
					}
					boolean foundLoop = manager.foundLoopOnMaps(map,elemId);
					

					if(foundLoop)
						mapsWithLoop.add(elemId);
					else
						velems.add(manager.newElement(elemId, type));
				} // end for

				//get links and add elements to map
				if (velems != null) {
					map.addElements(velems);
					links = manager.getLinks(map.getAllElements());
				}
				log.debug("After getting/adding links");
			} 				
			bw.write(ResponseAssembler.getAddMapsResponse(action, mapsWithLoop,velems, links));
		} catch (Exception e) {
			log.error("Error while adding Maps for action: "+action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

}
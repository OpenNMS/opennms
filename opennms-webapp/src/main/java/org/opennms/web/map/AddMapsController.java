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
			
			String type = VElement.MAP_TYPE;
			
			if (! action.equals(MapsConstants.ADDMAPS_ACTION))
                throw new MapsException(request.getRemoteUser() +": Cannot add maps because user role is:" + MapsConstants.ROLE_USER);
			log.debug("Adding maps by id: "+ elems);
			String[] smapids = elems.split(",");
	         List<Integer> mapids = new ArrayList<Integer>(smapids.length);			
			for (int i = 0; i<smapids.length;i++) {
				mapids.add(new Integer(smapids[i]));
			}
			
			List<VElement> velems = new ArrayList<VElement>();
			List<VLink> links = new ArrayList<VLink>();
			// response for addElement
			List<Integer> mapsWithLoop = new ArrayList<Integer>();
				
			for (Integer id : mapids) {
				if (map.containsElement(id, type)) {
					log.debug("Action: " + action + " . Map Contains Element: " + id+type);
					continue;
					
				}
				boolean foundLoop = manager.foundLoopOnMaps(map,id);
					

				if(foundLoop) {
					mapsWithLoop.add(id);
				} else {
				    VElement ve = manager.newElement(id, type);;
	                try {
	                    VElement hve = manager.getElement(map.getId(), id, VElement.MAP_HIDE_TYPE);
	                    if (hve.getLabel() != null) {
	                        ve.setLabel(hve.getLabel());
	                        log.debug("preserving label map is hidden: label found: " + hve.getLabel());
	                    }
	                } catch (Exception e) {
	                   log.debug("No Hidden Map found for id: " +id); 
                    }
	                log.debug("adding map element to map with id: " +id+type);
					velems.add(ve);
				}
			} // end for

				//get links and add elements to map
			if (velems != null) {
				map.addElements(velems);
				links = manager.getLinks(map.getAllElements());
			}
			log.debug("After getting/adding links");
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
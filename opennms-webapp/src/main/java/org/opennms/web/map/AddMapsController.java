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
		String elems = request.getParameter("elems");
		log.debug("Adding Maps: elems="+elems );
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		try {
			if (!request.isUserInRole(org.opennms.web.springframework.security.Authentication.ADMIN_ROLE)) {
				log.warn("Cannot add maps because not admin role for user: " + request.getRemoteUser() );
				throw new MapsException("User has not role admin");
			}
			VMap map = manager.openMap();
            List<VElement> velems = new ArrayList<VElement>();
            // response for addElement
            List<Integer> mapsWithLoop = new ArrayList<Integer>();
			log.debug("Got map from manager "+map);
			
			log.debug("Adding maps by id: "+ elems);
			String[] smapids = elems.split(",");

			for (int i = 0; i<smapids.length;i++) {
			    Integer id = new Integer(smapids[i]);
				if (map.containsElement(id, MapsConstants.MAP_TYPE)) {
					log.debug(" Map Contains Element: " + id+MapsConstants.MAP_TYPE);
					continue;
					
				}
				boolean foundLoop = manager.foundLoopOnMaps(map,id);

				if(foundLoop) {
					mapsWithLoop.add(id);
				} else {
				    velems.add(manager.newElement(map.getId(),id, MapsConstants.MAP_TYPE));
				}
			} // end for

			//get map
			map = manager.addElements(map, velems);
			bw.write(ResponseAssembler.getAddElementResponse(MapsConstants.ADDMAPS_ACTION, mapsWithLoop,velems,map.getLinks().values()));
		} catch (Exception e) {
			log.error("Error while adding Maps: ",e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.ADDMAPS_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}

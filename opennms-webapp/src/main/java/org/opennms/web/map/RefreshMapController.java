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
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
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
public class RefreshMapController implements Controller {
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
		log.debug("Received action="+action);
		
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		VMap map = null;
		try {
			if (action.equals(MapsConstants.REFRESH_ACTION)) {
				// First refresh Element objects
				List<VElement> velements=manager.refreshMap();
				//gets map refreshed
				map = manager.openMap();
				// Second Refresh Link Object on Map
				// Now is done using a very simple way
				// but really it's slow
				// the alternative is anyway to analize all 
				// links, 1 against other.
				// So with this solution more traffic
				// less stress on server
				// more work on client
				
				// We are waiting to attempt to mapd
				map.removeAllLinks();

				// get all links on map
				//List links = null;
				List<VLink> links = manager.getLinks(velements);

				// add links to map
				map.addLinks(links.toArray(new VLink[links.size()]));
			} 
			
			if (action.equals(MapsConstants.RELOAD_ACTION)) {
				map = manager.openMap();
				// First refresh Element objects
				map = manager.reloadMap(map);
				
				// Second Refresh Link Object on Map
				// Now is done using a very simple way
				// but really it's slow
				// the alternativ is anyway to analize all 
				// links, 1 against other.
				// So with this solution more traffic
				// less stress on server
				// more work on client
				
				// We are waiting to attempt to mapd
				map.removeAllLinks();

				// get all links on map
				//List links = null;
				List<VLink> links = manager.getLinks(map.getElements());

				// add links to map
				map.addLinks(links.toArray(new VLink[links.size()]));
			}
			
			if(map==null){
				throw new MapNotFoundException();
			}else{
				bw.write(ResponseAssembler.getRefreshResponse(action, map));
			}
		} catch (Exception e) {
			log.error("Error while refreshing map. Action "+action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

}
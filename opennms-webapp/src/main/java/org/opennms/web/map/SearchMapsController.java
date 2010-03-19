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


import org.opennms.web.WebSecurityUtils;
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
public class SearchMapsController implements Controller {
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
	    int mapWidth = WebSecurityUtils.safeParseInt(request
	                                                   .getParameter("MapWidth"));
        int mapHeight = WebSecurityUtils.safeParseInt(request
	                                                       .getParameter("MapHeight"));

        log.debug("Current mapWidth=" + mapWidth + " and MapHeight=" + mapHeight);

        int d = WebSecurityUtils.safeParseInt(request
                                                     .getParameter("MapElemDimension"));

        int n = mapWidth /4/d;
        log.debug("Map row at max elements="+n );

        String elems = request.getParameter("elems");
		log.debug("Adding Searching Maps: elems="+elems );
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

		try {
            List<VElement> velems = new ArrayList<VElement>();
            // response for addElement
			
			log.debug("Adding maps by id: "+ elems);
			String[] smapids = elems.split(",");
			int x = -1;
            int y = -1;
            int s = 1;

			for (int i = 0; i<smapids.length;i++) {

			    if (x < n) {
			        x++;
			    } else {
	               y++;
       		        if (s==1) {
       		            x=1;
       		            s--;
       		        } else {
                        x=0;
                        s++;
       		        }
			    }
			    velems.add(manager.newElement(MapsConstants.SEARCH_MAP, new Integer(smapids[i]), MapsConstants.MAP_TYPE, null, x*4*d+s*2*d, y*d+d));
			} // end for

			//get map
            VMap map = manager.searchMap(VMap.SEARCH_NAME, request
                                         .getRemoteUser(), request.getRemoteUser(),
                                         mapWidth, mapHeight,velems);
            log.debug("Got search map from manager "+map);
			bw.write(ResponseAssembler.getMapResponse(map));
		} catch (Exception e) {
			log.error("Error while adding Maps: ",e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.SEARCHMAPS_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}

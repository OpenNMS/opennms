/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;


/**
 * <p>SearchMapsController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class SearchMapsController extends MapsLoggingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchMapsController.class);


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
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    int mapWidth = WebSecurityUtils.safeParseInt(request
	                                                   .getParameter("MapWidth"));
        int mapHeight = WebSecurityUtils.safeParseInt(request
	                                                       .getParameter("MapHeight"));

        LOG.debug("Current mapWidth={} and MapHeight={}", mapWidth, mapHeight);

        int d = WebSecurityUtils.safeParseInt(request
                                                     .getParameter("MapElemDimension"));
        
        LOG.debug("default element dimension: {}", d );


        String elems = request.getParameter("elems");
        LOG.debug("Adding Searching Maps: elems={}", elems );


        int n = mapWidth /4/d;
        int k = mapHeight/2/d;
        LOG.debug("Max number of element on the row: {}", n );
        LOG.debug("Max number of element in the map: {}", n * k );

        String[] smapids = elems.split(",");

        LOG.debug("Map Element to add to the Search Map: {}", smapids.length);

        while (smapids.length > n*k) {
            LOG.info("the map dimension is too big: resizing");
            d = d - 5;
            LOG.info("new element dimension: {}", d);
            n = mapWidth /4/d;
            k = mapHeight/2/d;
            LOG.debug("Recalculated - Max number of element on the row: {}", n );
            LOG.debug("Recalculated - Max number of element in the map: {}", n * k );
        }
		
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
                                                                      .getOutputStream(), "UTF-8"));
		try {
            List<VElement> velems = new ArrayList<VElement>();
            // response for addElement
			
			int x = -1;
            int y = 0;
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
			    velems.add(manager.newElement(MapsConstants.SEARCH_MAP, new Integer(smapids[i]), MapsConstants.MAP_TYPE, null, x*4*d+s*2*d, y*2*d+d));
			} // end for

			//get map
            VMap map = manager.searchMap(request
                                         .getRemoteUser(), request.getRemoteUser(),
                                         mapWidth, mapHeight,velems);
            LOG.debug("Got search map from manager {}", map);
			bw.write(ResponseAssembler.getMapResponse(map));
		} catch (Throwable e) {
			LOG.error("Error while adding Maps: ",e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.SEARCHMAPS_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}

}

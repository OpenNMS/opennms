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


import java.io.BufferedWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.WebSecurityUtils;


import org.opennms.web.map.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.*;

/**
 * <p>SaveMapController class.</p>
 *
 * @author mmigliore
 * @author antonio@opennms.it
 * @version $Id: $
 * @since 1.8.1
 */
public class SaveMapController implements Controller {
	
	private static final Logger LOG = LoggerFactory.getLogger(SaveMapController.class);

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

	private static List<VElement> elems = null;

	/** {@inheritDoc} */
        @Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
            Logging.putPrefix(MapsConstants.LOG4J_CATEGORY);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));
			
		int mapId = WebSecurityUtils.safeParseInt(request.getParameter("MapId"));
		String mapName = request.getParameter("MapName");
		String mapBackground = request.getParameter("MapBackground");
		int mapWidth = WebSecurityUtils.safeParseInt(request.getParameter("MapWidth"));
		int mapHeight = WebSecurityUtils.safeParseInt(request.getParameter("MapHeight"));
		
		String query = request.getQueryString();
		String queryNodes = request.getParameter("Nodes");
		
		LOG.debug("Saving map {} the query received is '{}'", mapName, query);
        LOG.debug("Saving map {} the data received is '{}'", mapName, queryNodes);

		try {
			VMap map = manager.openMap();
			if (mapId != MapsConstants.NEW_MAP && map.isNew())
				map = manager.openMap(mapId, request.getRemoteUser(), false);

			LOG.debug("Instantiating new elems ArrayList");
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
                    LOG.debug("preserving the label: {}", map.getElement(id, type).getLabel());
                    label = map.getElement(id, type).getLabel();
                }                
                
                VElement ve = manager.newElement(map.getId(), id, type, icon, x, y);
                if (label != null )
                    ve.setLabel(label);                    
                
                LOG.debug("adding map element to map with id: {}{} and label: {}", id, type, ve.getLabel());
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
			    LOG.debug("Map is New Map");
				map.setType(MapsConstants.USER_GENERATED_MAP);
				map.setAccessMode(MapsConstants.ACCESS_MODE_ADMIN);
			}
			else if (map.getType().trim().equalsIgnoreCase(MapsConstants.AUTOMATICALLY_GENERATED_MAP)) {
                LOG.debug("Map is Automated Map, saving as Static");
                map.setType(MapsConstants.AUTOMATIC_SAVED_MAP);
			}
			mapId = manager.save(map);

	        LOG.info("{} Map saved. With map id: {}", map.getName(),  mapId);

			if (map.isNew()) 
			    map.setId(mapId);

			bw.write(ResponseAssembler.getSaveMapResponse(map));
		} catch (Throwable e) {
			LOG.error("Map save error: {}", e,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.SAVEMAP_ACTION));
		} finally {
			bw.close();
			LOG.info("Sending response to the client");
		}
		return null;
	}

}

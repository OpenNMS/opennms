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
import org.opennms.web.map.db.Element;

import org.opennms.web.map.view.*;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.*;

/**
 * <p>SaveMapController class.</p>
 *
 * @author mmigliore
 * @version $Id: $
 * @since 1.6.12
 */
public class SaveMapController implements Controller {

	Category log;

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
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		if (!manager.isUserAdmin()) {
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
		String packetStr = request.getParameter("packet");
		String totalPacketsStr = request.getParameter("totalPackets");
		
		if (log.isDebugEnabled())
			log.debug("Saving map " + mapName + " the query received is '" + query + "'");
		

		try {
			if (!manager.isUserAdmin()) {
				throw new MapsException("User not admin: cannot save map");
			}
			VMap map = manager.openMap();

			if ((packetStr == null && totalPacketsStr == null)
					|| (packetStr.equals("1"))) {
				if (log.isDebugEnabled())
					log.debug("Instantiating new elems ArrayList");
				elems = new ArrayList<VElement>();
			}

			StringTokenizer st = new StringTokenizer(queryNodes, "*");
			while (st.hasMoreTokens()) {
				String nodeToken = st.nextToken();
				StringTokenizer nodeST = new StringTokenizer(nodeToken, ",");
				int counter = 1;
				String icon = "";
				String type = Element.NODE_TYPE;

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
				VElement ve = null;
				if (!type.equals(Element.NODE_TYPE)
						&& !type.equals(Element.MAP_TYPE)) {
					throw new MapsException("Map element type " + type
							+ " not valid! Valid values are:"
							+ Element.NODE_TYPE + " and "
							+ Element.MAP_TYPE);
				}
				log.debug("adding map element to map with id " + id
						+ " and type " + type);
				ve = manager.newElement(map.getId(), id, type, icon, x, y);
				elems.add(ve);
			}
			// add elements and save if is a no-packet session or if is the
			// last packet
			if ((packetStr == null && totalPacketsStr == null)
					|| (packetStr.equals(totalPacketsStr))) {
				
				log.info("SaveMap: removing all links and elements.");
				map.removeAllLinks();
				map.removeAllElements();
				log.info("SaveMap: saving all elements.");
				
				Iterator it = elems.iterator();
				while (it.hasNext()) {
					map.addElement((VElement) it.next());
				}
				
				map.setUserLastModifies(request.getRemoteUser());
				map.setName(mapName);
				map.setBackground(mapBackground);
				map.setWidth(mapWidth);
				map.setHeight(mapHeight);
				
				if (map.isNew())
					map.setType(VMap.USER_GENERATED_MAP);
				manager.save(map);
				
				log.info("Map saved");
			}
			bw.write(ResponseAssembler.getSaveMapResponse(MapsConstants.SAVEMAP_ACTION, map, packetStr, totalPacketsStr));
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

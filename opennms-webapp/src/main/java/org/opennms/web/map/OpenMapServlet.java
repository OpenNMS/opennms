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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.map.dataaccess.MapMenu;
import org.opennms.web.map.view.*;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * @author mmigliore
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class OpenMapServlet extends HttpServlet {
	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedWriter bw = null;
		String strToSend=null;
		try {
			ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			bw = new BufferedWriter(new OutputStreamWriter(response
					.getOutputStream()));

			String action = request.getParameter("action");

			strToSend = action+"OK";
			HttpSession session = request.getSession(false);
			if (session != null) {
				Manager m = null;
				m = (Manager) session.getAttribute("manager");
				log.debug("Got manager from session: "+m);
				strToSend = action + "OK";
				String lastModTime = "";
				String createTime = "";
				float widthFactor = 1;
				float heightFactor =1;
				int mapId = Integer.parseInt(request.getParameter("MapId"));
				int mapWidth = Integer.parseInt(request
						.getParameter("MapWidth"));
				int mapHeight = Integer.parseInt(request
						.getParameter("MapHeight"));
				log.debug("Current mapWidth=" + mapWidth
						+ " and MapHeight=" + mapHeight);
				
				VMap map = null;
				
				if (action.equals(MapsConstants.OPENMAP_ACTION)) {
					SimpleDateFormat formatter = new SimpleDateFormat(
					"HH.mm.ss dd/MM/yy");
					String user = request.getRemoteUser();
					String role = null;
					if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
						role=Authentication.ADMIN_ROLE;
					}else if (request.isUserInRole(Authentication.USER_ROLE)) {
						role=Authentication.USER_ROLE;
					}					
					List<MapMenu> visibleMaps = m.getVisibleMapsMenu(user, role);
					Iterator<MapMenu> it = visibleMaps.iterator();
					boolean found=false;
					while(it.hasNext()){
						MapMenu mapMenu = it.next();
						if(mapMenu.getId()==mapId){
							found=true;
						}
					}
					if(found){
						map = m.getMap(mapId,true);
						int oldMapWidth = map.getWidth();
						int oldMapHeight = map.getHeight();
						widthFactor = (float) mapWidth / oldMapWidth;
						heightFactor = (float) mapHeight / oldMapHeight;
						log.debug("Old saved mapWidth=" + oldMapWidth
								+ " and MapHeight=" + oldMapHeight);
						log.debug("widthFactor=" + widthFactor);
						log.debug("heightFactor=" + heightFactor);
						log.debug("Setting new width and height to the session map");
						map.setHeight(mapHeight);
						map.setWidth(mapWidth);
						if (map.getCreateTime() != null)
							createTime = formatter.format(map.getCreateTime());
						if (map.getLastModifiedTime() != null)
							lastModTime = formatter.format(map
									.getLastModifiedTime());
						strToSend += map.getId() + "+" + map.getBackground();
					}
				}else{
					strToSend=MapsConstants.OPENMAP_ACTION+"Failed";
				}
				
				if(map!=null){
					strToSend +="+" + map.getAccessMode() + "+"
						+ map.getName() + "+" + map.getOwner() + "+"
						+ map.getUserLastModifies() + "+" + createTime
						+ "+" + lastModTime;
					
					VElement[] elems = map.getAllElements();
					if (elems != null) {
						for (int i = 0; i < elems.length; i++) {
							int x = (int) (elems[i].getX() * widthFactor);
							int y = (int) (elems[i].getY() * heightFactor);
		
							strToSend += "&" + elems[i].getId()
									+ elems[i].getType() + "+" + x + "+"
									+ y + "+" + elems[i].getIcon() + "+"
									+ elems[i].getLabel();
							strToSend += "+" + elems[i].getRtc() + "+"
									+ elems[i].getStatus() + "+"
									+ elems[i].getSeverity();
						}
					}
					VLink[] links = map.getAllLinks();
					if (links != null) {
						for (int i = 0; i < links.length; i++) {
							strToSend += "&" + links[i].getFirst().getId()
									+ links[i].getFirst().getType() + "+"
									+ links[i].getSecond().getId()
									+ links[i].getSecond().getType();
						}
					}
				} else {
					strToSend=MapsConstants.OPENMAP_ACTION + "Failed";
					log.error("Map with id "+mapId+" not visible for user "+request.getRemoteUser());
				}

				m.endSession();
				
				session.setAttribute("sessionMap", map);
				log.info("Sending response to the client '" + strToSend
						+ "'");

			} else {
				strToSend=MapsConstants.OPENMAP_ACTION + "Failed";
				log.error("HttpSession not initialized");
			}
		} catch (Exception e) {
			strToSend=MapsConstants.OPENMAP_ACTION + "Failed";
			if (bw == null) {
				bw = new BufferedWriter(new OutputStreamWriter(response
						.getOutputStream()));
			}
			log.error(this.getClass().getName()+" Error: "+e);
		}finally{
			bw.write(strToSend);
			bw.close();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}
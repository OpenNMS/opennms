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
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.db.MapMenu;
import org.opennms.web.map.view.*;

/**
 * @author mmigliore
 */
public class LoadMapsServlet extends HttpServlet {
	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		log.info("Loading maps");
		String action = request.getParameter("action");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String strToSend = action + "OK";

		HttpSession session = request.getSession(false);
		Manager m = (Manager)session.getAttribute("manager");
		try {
			m.startSession();
		} catch (MapsException e1) {
			log.error("Error while starting Manager session "+e1);
		}

		String user = request.getRemoteUser();
		String role = null;
		if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
			role=Authentication.ADMIN_ROLE;
		}else if (request.isUserInRole(Authentication.USER_ROLE)) {
			role=Authentication.USER_ROLE;
		}

		List visibleMapsList = new ArrayList();
		
		try {
			if (action.equals(MapsConstants.LOADMAPS_ACTION)) {
				visibleMapsList = m.getVisibleMapsMenu(user, role);
				// create the string containing the main informations about all maps
				// defined:
				// the string will have the form:
				// mapid1,mapname1,mapowner1-mapid2,mapname2,mapowner2...
				for (int i = 0; i < visibleMapsList.size(); i++) {
					if (i > 0) {
						strToSend += "&";
					}
					strToSend += mapToString((MapMenu) visibleMapsList.get(i));
		
				}
			} else {
				strToSend = MapsConstants.LOADMAPS_ACTION + "Failed";
			}

		} catch (MapsException e) {
			log.error("Error while getting visible maps for user "+user+ " and role "+role);
			log.error(e);
			strToSend = MapsConstants.LOADMAPS_ACTION + "Failed";
		}finally {
			bw.write(strToSend);
			bw.close();
			log.info("Sending response to the client '" + strToSend + "'");
			try {
				m.endSession();
			} catch (MapsException e1) {
				log.error("Error while ending Manager session "+e1);
			}
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	private String mapToString(MapMenu map) {
		String strToSend = map.getId() + "+" + map.getName() + "+"
				+ map.getOwner();
		return strToSend;
	}
}

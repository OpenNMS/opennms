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


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.config.MapsFactory;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VMap;

/**
 * The servlet inits the maps' application.
 * It sets some attribute session and call the init() method of the initclass defined in the using factory  
 * @author mmigliore
 */
public class InitMapsApplicationServlet extends HttpServlet {
	
	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session=null;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				response.getOutputStream()));
		String strToSend = MapsConstants.INIT_ACTION+"OK";
		try {
			ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			log.info("Init maps application");
			String mapFactoryLabel = request.getParameter("mapsFactory");

			session = request.getSession(true);
			session.setMaxInactiveInterval(-1);
			Manager m = null;
			if(mapFactoryLabel==null || mapFactoryLabel.equalsIgnoreCase("null")){
				log.debug("Instantiating Manager with default MapsFactory");
				try {
					m = new Manager();
				} catch (MapsException e) {
					strToSend=MapsConstants.INIT_ACTION+"Failed";
					log.fatal("Error while instantiating default Manager");
					bw.write(strToSend);
					bw.close();
					log.info("Sending response to the client "+strToSend);
					return;
				}
			}else{
				log.debug("Instantiating Manager with MapsFactory "+mapFactoryLabel);
				try {
					m = new Manager(mapFactoryLabel);
				} catch (MapsException e) {
					strToSend=MapsConstants.INIT_ACTION+"Failed";
					log.fatal("Error while instantiating Manager with factory "+mapFactoryLabel);
					bw.write(strToSend);
					bw.close();
					log.info("Sending response to the client "+strToSend);
					return;
				}
			}
			log.debug("Setting session manager with implementation data access manager "+m.getDataAccessManager().getClass().getName());
			session.setAttribute("manager", m);
			try {
				m.startSession();
			} catch (MapsException e1) {
				log.error("Error while starting Manager session "+e1);
				strToSend=MapsConstants.INIT_ACTION+"Failed";
				bw.write(strToSend);
				bw.close();
				return;
			}
			MapPropertiesFactory.init();
			MapPropertiesFactory mpf = MapPropertiesFactory.getInstance();
			MapsFactory mf =null;
			if (mapFactoryLabel == null || mapFactoryLabel.equalsIgnoreCase("null")) {
				mf = mpf.getDefaultFactory();
			}else{
				mf = mpf.getMapsFactory(mapFactoryLabel);
			}
			VMap sessionMap = m.newMap();
			boolean isMapEditable = false;
			if ((request.isUserInRole(Authentication.ADMIN_ROLE) && mf.isAdminModify()) || mf.isAllModify()) {
				isMapEditable = true;
			}

			session.setAttribute("sessionMap", sessionMap);
			Integer mapToOpen = (Integer) session.getAttribute("mapToOpen");
			String refreshTime = (String) session.getAttribute("refreshTime");

			strToSend += refreshTime + "&" + isMapEditable;
			if (mapToOpen != null) {
				strToSend += "&" + mapToOpen.intValue();
			}
			
			try {
				m.endSession();
			} catch (MapsException e1) {
				log.error("Error while ending Manager session "+e1);
				strToSend=MapsConstants.INIT_ACTION+"Failed";
				bw.write(strToSend);
				bw.close();
				return;
			}
			bw.write(strToSend);
			bw.close();
			log.info("Sending response to the client '" + strToSend + "'");
		} catch (Exception e) {
			session.invalidate();
			log.error("Init maps application: " + e);
			e.printStackTrace();
			strToSend=MapsConstants.INIT_ACTION+"Failed";
			bw.write(strToSend);
			bw.close();
			return;
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}

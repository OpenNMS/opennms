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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.asset.Asset;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.element.Interface;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VMap;

/**
 * @author mmigliore
 *
 */
public class LoadInfosServlet extends HttpServlet {

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		
		String action = request.getParameter("action");
		String elem = request.getParameter("elem");
		String type = request.getParameter("type");
		log.info("Loading infos for elem "+elem+" and type "+type);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		
		String strToSend = action + "OK";
		HttpSession session = request.getSession(false);
		org.opennms.web.map.dataaccess.Manager  implManager = null;
		VMap map =null;
		if (session != null) {
			Manager m = null;
			m = (Manager) session.getAttribute("manager");
			implManager=m.getDataAccessManager();
			log.debug("Got manager from session: "+m);
			map  = (VMap)session.getAttribute("sessionMap");
			log.debug("Got session map: "+map);
		}else{
			strToSend = action + "Failed";
		}
		
		
		java.util.Map infos=null;
		if(action.equals(MapsConstants.LOAD_NODES_INFO_ACTION)){
			try {
				infos = implManager.getElementInfo(Integer.parseInt(elem), map.getId(), type);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				log.error("Error while getting infos "+e);
				strToSend = action + "Failed";
			} catch (MapsException e) {
				e.printStackTrace();
				log.error("Error while getting infos "+e);
				strToSend = action + "Failed";
			}
		}else{
			strToSend = action + "Failed";
		}
		if(infos!=null){
			Iterator it = infos.keySet().iterator();
			while(it.hasNext()){
				String key = (String) it.next();
				strToSend+="+"+key+": "+(String)infos.get(key);
			}
		}
		bw.write(strToSend);
		bw.close();
		log.info("Sending response to the client '" + strToSend + "'");

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}
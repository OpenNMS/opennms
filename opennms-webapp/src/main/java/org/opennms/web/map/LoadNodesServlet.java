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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;

/**
 * @author mmigliore
 *
 */
public class LoadNodesServlet extends HttpServlet {

	

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		String action = request.getParameter("action");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String strToSend = action + "OK";
		log.info("Loading nodes");

		Node[] onmsNodes = null;

		try {
			if (action.equals(MapsConstants.LOADNODES_ACTION)) {
				onmsNodes = NetworkElementFactory.getAllNodes();
				for (int i = 0; i < onmsNodes.length; i++) {
					Node n = onmsNodes[i];
						if (i > 0) {
							strToSend += "&";
						}
		
						String nodeStr = n.getNodeId() + "+" + n.getLabel();
						strToSend += nodeStr;
				}
			} else {
				strToSend = MapsConstants.LOADNODES_ACTION + "Failed";
			}
		} catch (SQLException e) {
			log.error(e.toString());
			strToSend = MapsConstants.LOADNODES_ACTION + "Failed";
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
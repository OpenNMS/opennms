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
import org.opennms.web.map.view.*;

/**
 * @author mmigliore
 *
 * This servlet is called for 
 * deleting a map 
 */
public class DeleteMapServlet extends HttpServlet
{

 
    Category log;
    
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      
      ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

      String action = request.getParameter("action");
      
      String strToSend = action+"OK";
      int mapId = Integer.parseInt(request.getParameter("MapId"));

      if (log.isInfoEnabled())
    	  log.info("Deleting map with id="+mapId);
      
      try{
    	if(action.equals(MapsConstants.DELETEMAP_ACTION)){
			HttpSession session = request.getSession(false);
			Manager m = null;
			if(session!=null){
				m = (Manager) session.getAttribute("manager");
				log.debug("Got manager from session: "+m);
			}
	      	m.startSession();
	      	m.deleteMap(mapId);
	      	m.endSession();
    	}else{
    		strToSend=MapsConstants.DELETEMAP_ACTION+"Failed";
    	}
      }catch(Exception e){
      	log.error("Delete map error "+e);
      	strToSend=MapsConstants.DELETEMAP_ACTION+"Failed";
      }finally{
	      bw.write(strToSend);
	      bw.close();
      } 
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    

}

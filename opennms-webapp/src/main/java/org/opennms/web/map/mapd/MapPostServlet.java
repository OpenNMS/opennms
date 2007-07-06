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
package org.opennms.web.map.mapd;

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapsConstants;
import org.opennms.netmgt.xml.map.NodeChange;


/**
 * @author mmigliore
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class MapPostServlet extends HttpServlet {
	
	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedWriter bw = null;
		try {
			ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			bw = new BufferedWriter(new OutputStreamWriter(response
					.getOutputStream()));

			SharedChanges sharedChanges = (SharedChanges)getServletContext().getAttribute("MapSharedChanges");
			if(sharedChanges==null){
				log.error("No MapSharedChanges found. Creating a new one.");
				return;
			}
			ServletInputStream sis = request.getInputStream();
			 BufferedInputStream buf=new BufferedInputStream(sis);//for better performance
			 ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
			 byte[] buffer=new byte[1024];//byte buffer
			 int bytesRead=0;
			 while (true){
				 bytesRead=buf.read(buffer,0,1024);
	//			 bytesRead returns the actual number of bytes read from
	//			 the stream. returns -1 when end of stream is detected
				 if (bytesRead == -1) break;
				 output.write(buffer,0,bytesRead);
				 }
		 	
			if(buf!=null)buf.close();
			String nodeChangeXml = output.toString();
			log.info("Received node change xml:");
			log.info(nodeChangeXml);
			StringReader sr = new StringReader(nodeChangeXml);
			NodeChange nch = (NodeChange) NodeChange.unmarshal(sr);
			log.info("adding it to sharedChanges object");
			
			synchronized (sharedChanges) {
				sharedChanges.addChangedNode(nch);
				getServletContext().setAttribute("MapSharedChanges", sharedChanges);
				log.debug("NotifyAll on MapSharedChanges");
				sharedChanges.notifyAll();
				log.debug("did NotifyAll.");
			}
		} catch (Exception e) {
			if (bw == null) {
				bw = new BufferedWriter(new OutputStreamWriter(response
						.getOutputStream()));
			}
			bw.write("Failed");
			log.error("Failure: "+e,e);
		}finally{
			bw.close();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}
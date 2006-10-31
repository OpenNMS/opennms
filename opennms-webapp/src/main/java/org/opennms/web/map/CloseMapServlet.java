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

import java.text.SimpleDateFormat;

/**
 * @author mmigliore
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class CloseMapServlet extends HttpServlet {
	
	static final long serialVersionUID = 2006102300; 


	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedWriter bw = null;
		try {
			ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			bw = new BufferedWriter(new OutputStreamWriter(response
					.getOutputStream()));

			String action = request.getParameter("action");

			String strToSend = null;
			HttpSession session = request.getSession(false);
			if (session != null) {
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
				Manager m = new Manager();
				m.startSession();
				
				VMap map = null;
				

				
				if (action.equals(MapsConstants.CLOSEMAP_ACTION)) {
					log.info("Close Map: closing opened map.");
					map = null;
					strToSend += MapsConstants.MAP_NOT_OPENED + "+" + MapsConstants.DEFAULT_BACKGROUND_COLOR;
					//strToSend +="++++++";
				}else{
					strToSend=MapsConstants.CLOSEMAP_ACTION+"Failed";
				}
				
				bw.write(strToSend);
				bw.close();
				m.endSession();
				
				session.setAttribute("sessionMap", map);
				log.info("Sending response to the client '" + strToSend
						+ "'");

			} else {
				bw.write(MapsConstants.CLOSEMAP_ACTION + "Failed");
				bw.close();
				log.error("HttpSession not initialized");
			}
		} catch (Exception e) {
			if (bw == null) {
				bw = new BufferedWriter(new OutputStreamWriter(response
						.getOutputStream()));
			}
			bw.write(MapsConstants.CLOSEMAP_ACTION+ "Failed");
			bw.close();
			log.error(this.getClass().getName()+" Failure: "+e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}
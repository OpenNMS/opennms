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
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VMap;

/**
 * @author mmigliore
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class InitMapsApplicationServlet extends HttpServlet {

	static final long serialVersionUID = 2006102700;

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(response.getOutputStream()));
		String strToSend=null;
		try {
			ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			log.info("Init maps application");
			
			String action = request.getParameter("action");

			strToSend = action + "OK";

			if (action.equals(MapsConstants.INIT_ACTION)) {
				HttpSession userSession = request.getSession();
				Manager m = new Manager();
				VMap sessionMap = m.newMap();

				Integer mapToOpen = (Integer) userSession.getAttribute("mapToOpen");

				String refreshTime = (String) userSession.getAttribute("refreshTime");
				if(refreshTime!=null){
					strToSend += refreshTime;
				}
				if (mapToOpen != null) {
					strToSend += "&" + mapToOpen.intValue();
				} 
			} else {
				strToSend = MapsConstants.INIT_ACTION + "Failed";
			}
		} catch (Exception e) {
			log.error("Init maps application: " + e);
			strToSend = MapsConstants.INIT_ACTION + "Failed";
		}finally{
			bw.write(strToSend);
			bw.close();
			log.info("Sending response to the client '" + strToSend + "'");
		}
		
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}

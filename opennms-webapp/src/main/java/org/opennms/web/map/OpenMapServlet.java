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
import org.opennms.web.map.view.*;

import java.text.SimpleDateFormat;

/**
 * @author mmigliore
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OpenMapServlet extends HttpServlet {
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	
	private final int MAP_NOT_OPENED = -1;
	
	private final int NEW_MAP = -2;
	

	private final String DEFAULT_BACKGROUND_COLOR = "ffffff";

	private static final String NEWMAP_ACTION = "NewMap";

	private static final String OPENMAP_ACTION = "OpenMap";
	
	private static final String CLOSEMAP_ACTION = "CloseMap";

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedWriter bw = null;
		try {
			ThreadCategory.setPrefix(LOG4J_CATEGORY);
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
				VMap map = null;
				if (action.equals(NEWMAP_ACTION)) {
					log.info("New Map: creating new map");
					map = m.newMap(VMap.DEFAULT_NAME, "", request
						.getRemoteUser(), request.getRemoteUser(),
						mapWidth, mapHeight);
					map.setBackground(DEFAULT_BACKGROUND_COLOR);
					strToSend += NEW_MAP + "+" + DEFAULT_BACKGROUND_COLOR;
				}
				
				if (action.equals(CLOSEMAP_ACTION)) {
					log.info("Close Map: closing opened map.");
					map = null;
					strToSend += MAP_NOT_OPENED + "+" + DEFAULT_BACKGROUND_COLOR;
					strToSend +="+" + "" + "+"
					+ "" + "+" + "" + "+"
					+ "" + "+" + ""
					+ "+" + "";
				}				
				if (action.equals(OPENMAP_ACTION)) {
					SimpleDateFormat formatter = new SimpleDateFormat(
					"HH.mm.ss dd/MM/yy");

					map = m.getMap(mapId);
					int oldMapWidth = map.getWidth();
					int oldMapHeight = map.getHeight();
					widthFactor = (float) mapWidth / oldMapWidth;
					heightFactor = (float) mapHeight / oldMapHeight;
					log.debug("Old saved mapWidth=" + oldMapWidth
							+ " and MapHeight=" + oldMapHeight);
					log.debug("widthFactor=" + widthFactor);
					log.debug("heightFactor=" + heightFactor);
					if (map.getCreateTime() != null)
						createTime = formatter.format(map.getCreateTime());
					if (map.getLastModifiedTime() != null)
						lastModTime = formatter.format(map
								.getLastModifiedTime());
					strToSend += map.getId() + "+" + map.getBackground();
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
				}
				bw.write(strToSend);
				bw.close();
				session.setAttribute("sessionMap", map);
				log.info("Sending response to the client '" + strToSend
						+ "'");

			} else {
				bw.write(action + "Failed");
				bw.close();
				log.error("HttpSession not initialized");
			}
		} catch (Exception e) {
			if (bw == null) {
				bw = new BufferedWriter(new OutputStreamWriter(response
						.getOutputStream()));
			}
			bw.write(this.getClass().getName()+ "Failed");
			bw.close();
			log.error(this.getClass().getName()+" Failure: "+e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}
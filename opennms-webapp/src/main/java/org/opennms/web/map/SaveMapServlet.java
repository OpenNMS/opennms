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
import org.opennms.web.map.db.Element;
import org.opennms.web.map.view.*;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SaveMapServlet extends HttpServlet
{
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
	
	private static List elems=null;
	
    public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String query = request.getQueryString();
		String queryNodes = request.getParameter("Nodes");
		int mapId = Integer.parseInt(request.getParameter("MapId"));
		String mapName = request.getParameter("MapName");
		String mapBackground = request.getParameter("MapBackground");
		int mapWidth = Integer.parseInt(request.getParameter("MapWidth"));
		int mapHeight = Integer.parseInt(request.getParameter("MapHeight"));
		String packetStr = request.getParameter("packet");
		String totalPacketsStr = request.getParameter("totalPackets");
		HttpSession session = request.getSession(false);
		VMap map = null;
		String strToSend = "";
		if (session != null) {
			map = ((VMap) session.getAttribute("sessionMap"));
			log.debug("SaveMap: is the Map new?" + map.isNew());
			if (map == null) {
				strToSend = "saveMapFailed";
				log.error("Attribute session sessionMap is null");
			}
		} else {
			strToSend = "saveMapFailed";
			log.error("HttpSession not initialized");
		}
		log.info("Saving map " + mapName + " with mapId=" + mapId
				+ " the query received is '" + query + "'");
		try {
			Manager m = new Manager();
			if((packetStr==null && totalPacketsStr==null) || (packetStr.equals("1")) ){
				log.debug("SaveMap: Instantiating new elems ArrayList");
				elems = new ArrayList();
			}

			StringTokenizer st = new StringTokenizer(queryNodes, "*");
			while (st.hasMoreTokens()) {
				String nodeToken = st.nextToken();
				StringTokenizer nodeST = new StringTokenizer(nodeToken, ",");
				int counter = 1;
				String icon = "";
				String type = Element.NODE_TYPE;

				int id = 0, x = 0, y = 0;
				while (nodeST.hasMoreTokens()) {
					String tmp = nodeST.nextToken();
					if (counter == 1) {
						id = Integer.parseInt(tmp);
					}
					if (counter == 2) {
						x = Integer.parseInt(tmp);
					}
					if (counter == 3) {
						y = Integer.parseInt(tmp);
					}
					if (counter == 4) {
						icon = tmp;
					}
					if (counter == 5) {
						type = tmp;
					}
					counter++;
				}
				VElement ve = null;
				if (!type.equals(Element.NODE_TYPE)
						&& !type.equals(Element.MAP_TYPE)) {
					throw new MapsException("Map element type " + type
							+ " not valid! Valid values are:"
							+ Element.NODE_TYPE + " and " + Element.MAP_TYPE);
				}
				log.debug("adding map element to map with id " + id
						+ " and type " + type);
				VElement elemInMap = map.getElement(id,type);
				ve = m.newElement(mapId, id, type, icon, x, y);
				if(elemInMap!=null){
					ve.setRtc(elemInMap.getRtc());
					ve.setSeverity(elemInMap.getSeverity());
					ve.setStatus(elemInMap.getStatus());
				}
				elems.add(ve);
			}
			
			//add elements and save if is a no-packet session or if is the last packet
			if((packetStr==null && totalPacketsStr==null) || (packetStr.equals(totalPacketsStr))){
				m.startSession();
				log.info("SaveMap: removing all links and elements.");
				map.removeAllElements();
				map.removeAllLinks();				
				log.info("SaveMap: saving all elements.");
				Iterator it = elems.iterator();
				while(it.hasNext()){
					map.addElement((VElement)it.next());
				}
				map.setUserLastModifies(request.getRemoteUser());
				map.setName(mapName);
				map.setBackground(mapBackground);
				map.setWidth(mapWidth);
				map.setHeight(mapHeight);
				if (map.isNew())
					map.setType(VMap.USER_GENERATED_MAP);
				m.save(map);
				m.endSession();
				log.info("Map saved");
			}
			log.debug("Creating response to send to client. map="+ map+" strToSend="+strToSend+" map.getCreateTime()="+map.getCreateTime()+" map.getLastModifiedTime()="+map.getLastModifiedTime());
			SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
			strToSend = "saveMapOK+" + map.getId() + "+" + map.getBackground()
					+ "+" + map.getAccessMode() + "+" + map.getName() + "+"
					+ map.getOwner() + "+" + map.getUserLastModifies() + "+"
					+ ((map.getCreateTime()!=null)?formatter.format(map.getCreateTime()):"") + "+"
					+ ((map.getLastModifiedTime()!=null)?formatter.format(map.getLastModifiedTime()):"")+ "+"
					+ packetStr+ "+"
					+ totalPacketsStr;
			bw.write(strToSend);
			bw.close();
			log.info("Sending response to the client '" + strToSend + "'");
		} catch (Exception e) {
			log.error("Map save error: " + e);
			throw new ServletException(e);
		}
	}

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    

}

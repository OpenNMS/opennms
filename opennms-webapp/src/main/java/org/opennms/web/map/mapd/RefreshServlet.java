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
 * Created on 9-mar-2007
 *
 * 
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
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;


import java.util.*;

/**
 * @author mmigliore
 *
 */
public class RefreshServlet extends HttpServlet {

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		//elems is the feedback of the last refresh operation posted from client (counterOK or counterNOK)
		String feedback = request.getParameter("elems");
		String action = request.getParameter("action");
		log.debug("Received action="+action+" elems="+feedback);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String strToSend = "";
		try {
			HttpSession session = request.getSession(false);
			Manager m = null;
			if(session!=null){
				m = (Manager) session.getAttribute("manager");
				log.debug("Got manager from session: "+m);
			}
			m.startSession();
			VMap map = null;
 
			if (session != null) {
				map = (VMap) session.getAttribute("sessionMap");
				if (map != null) {
					strToSend = action + "OK";
					boolean actionfound = false;
					if (action.equals(MapsConstants.REFRESH_ACTION)) {
						//TODO inviare al client anche gli elementi e i links da rimuovere
						//     e farsi dare un feedback (contenuto nella successiva richiesta) per essere sicuri 
						//	   che la mappa visualizzata dal client sia sincronizzata con quella di sessione
						actionfound = true;
						String sessionId = session.getId();
						Refresher refresher = Refresher.getInstance();
						if(refresher==null){
							log.error("Refresher is null. InitializerServletContext must create it.");
							throw new IllegalStateException("Refresher is null. InitializerServletContext must create it.");
						}
						long refreshCounter = refresher.getRefreshCounter(sessionId, map, m);
						log.debug("Current refresh counter is "+refreshCounter);
						
						List<VElement> changedElements = refresher.getChangedElements(session.getId(), map, m);
						List<VLink> changedLinks = refresher.getChangedLinks(session.getId(), map, m);
						List<VLink> removedLinks = refresher.getDeletedLinks(session.getId(), map, m);
						if(feedback.equals(refreshCounter+"OK")){
							log.debug("Synchronizing last refresh (on current sessionMap)");
							VElement[] lastChangedElems = refresher.getLastChangedElements(sessionId, map, m).toArray(new VElement[0]);
							refresher.resetLastChangedElements(sessionId, map, m);
							log.debug("Adding changed elems of last refresh: "+lastChangedElems.toString());
							for(int k=0; k<lastChangedElems.length;k++){
								VElement ve = lastChangedElems[k];
								map.removeElement(ve.getId(), ve.getType());
								map.addElement(ve);
							}
														
							List<VLink> lastChangedLinks = refresher.getLastChangedLinks(sessionId, map, m);
							refresher.resetLastChangedLinks(sessionId, map, m);
							log.debug("Adding changed links of last refresh: "+lastChangedLinks.toString());
							Iterator<VLink> it = lastChangedLinks.iterator();
							while(it.hasNext()){
								VLink  curr = it.next();
								map.removeLink(curr);
								map.addLink(curr);
							}
							
							List<VLink> lastRemovedLinks = refresher.getLastDeletedLinks(sessionId, map, m);
							refresher.resetLastDeletedLinks(sessionId, map, m);
							log.debug("Removing links of last refresh: "+lastRemovedLinks.toString());
							it = lastRemovedLinks.iterator();
							while(it.hasNext()){
								VLink  curr = it.next();
								map.removeLink(curr);		
							}
							
						}else{
							log.debug("Feedback negative. Reusing last data (elements and links).");
							List<VElement> lastChangedElems = refresher.getLastChangedElements(sessionId, map, m);
							log.debug("lastChagedElems="+lastChangedElems.toString());
							log.debug("changedElems="+changedElements.toString());
							log.debug("Adding lastChangedElems to changedElems ");
							changedElements.addAll(lastChangedElems);
							
							
							List<VLink> lastChangedLinks = refresher.getLastChangedLinks(sessionId, map, m);
							log.debug("lastChangedLinks="+lastChangedLinks.toString());
							log.debug("changedLinks="+changedLinks.toString());
							log.debug("Adding lastChangedLinks to changedLinks ");	
							changedLinks.addAll(lastChangedLinks);
							
							
							List<VLink> lastRemovedLinks = refresher.getLastChangedLinks(sessionId, map, m);
							log.debug("lastRemovedLinks="+lastRemovedLinks.toString());
							log.debug("removedLinks="+removedLinks.toString());
							log.debug("Adding lastRemovedLinks to removedLinks ");	
							removedLinks.addAll(lastRemovedLinks);
							
	
						}
						
						//1. send changed elements
						VElement[] velements=(VElement[])changedElements.toArray(new VElement[0]);
						if (velements != null) {
							for(int k=0; k<velements.length;k++){
								VElement ve = velements[k];
								strToSend += "&" + ve.getId() + ve.getType() + "+"
										+ ve.getIcon() + "+" + ve.getLabel();
								strToSend += "+" + ve.getRtc() + "+"
										+ ve.getStatus() + "+" + ve.getSeverity();
							}
						}

						//2. send removed links
						if (removedLinks != null) {
							Iterator ite = removedLinks.iterator();
							while (ite.hasNext()) {
								VLink vl = (VLink) ite.next();
									strToSend += "&" + vl.getFirst().getId()
									+ vl.getFirst().getType() + "+"
									+ vl.getSecond().getId()
									+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
							}
						} 						

						//3. send changed links
						if (changedLinks != null) {
							Iterator ite = changedLinks.iterator();
							while (ite.hasNext()) {
								VLink vl = (VLink) ite.next();
									strToSend += "&" + vl.getFirst().getId()
									+ vl.getFirst().getType() + "+"
									+ vl.getSecond().getId()
									+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
							}
						} 
						
						//increment counter 
						log.debug("Incrementing refresh counter...");
						refresher.incrementRefreshCounter(sessionId, map, m);
						
					} 
					//TODO gestire la reload (decidere se tenerla)
					if (action.equals(MapsConstants.RELOAD_ACTION)) {
						actionfound = true;
						// First refresh Element objects
						map = m.reloadMap(map);
						VElement[] velements=map.getAllElements();
						
						//checks for only changed velements 
						if (velements != null) {
							for(int k=0; k<velements.length;k++){
								VElement ve = velements[k];
								strToSend += "&" + ve.getId() + ve.getType() + "+"
										+ ve.getIcon() + "+" + ve.getLabel();
								strToSend += "+" + ve.getRtc() + "+"
										+ ve.getStatus() + "+" + ve.getSeverity()+ "+" + ve.getX()+ "+" + ve.getY();
							}
						}

						// Second Refresh Link Object on Map
						// Now is done using a very simple way
						// but really it's slow
						// the alternativ is anyway to analize all 
						// links, 1 against other.
						// So with this solution more traffic
						// less stress on server
						// more work on client
						
						// We are waiting to attempt to mapd
						map.removeAllLinks();

						// get all links on map
						//List links = null;
						List links = m.getLinks(velements);

						// add links to map
						map.addLinks((VLink[]) links.toArray(new VLink[0]));

						// write to client
						if (links != null) {
							Iterator ite = links.iterator();
							while (ite.hasNext()) {
								VLink vl = (VLink) ite.next();
									strToSend += "&" + vl.getFirst().getId()
									+ vl.getFirst().getType() + "+"
									+ vl.getSecond().getId()
									+ vl.getSecond().getType()+"+"+vl.getLinkTypeId()+"+"+vl.getLinkOperStatusString();
							}
						} 
						
					}
					

					if (actionfound) {
						session.setAttribute("sessionMap",map);
					} else {
						throw new Exception("action " + action + " not exists");
					}

				} else {
					throw new Exception("Attribute session sessionMap is null");
				}
			} else {
				throw new Exception("HttpSession not initialized");
			}
			m.endSession();
			
		} catch (Exception e) {
			strToSend = action + "Failed";
			log.error("Exception catch " + e);
			StackTraceElement[] ste = e.getStackTrace();
			for(int k=0; k<ste.length; k++){
				log.error(ste[k].toString());
			}
		} finally {
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
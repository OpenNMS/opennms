/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.scriptd.ins.events;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.xml.event.Event;




/**
 * The InsServerListener will accept input from a socket, create a InsSession in
 * which communicate with an InsClient for the alarm synchronization.
 * 
 * @see main method for usage example 
 */

public class InsServerListener extends InsServerAbstractListener {
	
	private ServerSocket listener;
	
	private Set<InsSession> activeSessions=new HashSet<InsSession>();

	/**
	 * listens for incoming connection on defined port (default is 8154)
	 */
	public void run() {
	    Category log = getLog();
		if(criteriaRestriction ==null)
			throw new IllegalStateException("The property criteriaRestriction cannot be null!");
		log.info("InsServerListener started: listening on port "+listeningPort);
		try {
			listener = new ServerSocket(listeningPort);
			Socket server;

			while (true) {
				// when accepts an incoming connection, create an InsSession for
				// alarms exchange
				server = listener.accept();
				InsSession session = new InsSession(server);
				//only if the sharedASCIIString is valorized, requires authentication
				if(sharedAuthAsciiString!=null)
					session.setSharedASCIIString(sharedAuthAsciiString);
				session.setCriteriaRestriction(criteriaRestriction);
				session.start();
				activeSessions.add(session);
			}
		} catch (IOException ioe) {
			log.info("Socket closed." );
		}
	}

	@Override
	/**
	 * Stops the listener
	 */
	public void interrupt() {
        Category log = getLog();
		try {
			listener.close();
		} catch (IOException e) {
		    log.error("Gor Error closing listener: " + e.getLocalizedMessage());
		}
		super.interrupt();
        log.info("InsServerListener Interrupted!");
	}
	
	private synchronized void cleanActiveSessions(){
        Category log = getLog();
		synchronized (activeSessions){
			Iterator<InsSession> it = activeSessions.iterator();
			while(it.hasNext()){
				InsSession insSession = it.next();
				if(insSession==null || !insSession.isAlive()){
					log.debug("removing "+insSession);
					it.remove();
				}
			}
		}
		log.debug("active sessions are: "+activeSessions);
	}

	/**
	 * Flushes the event in input to all active sessions with clients
	 * @param event
	 */
	public void flushEvent(Event event){
	      Category log = getLog();
	      log.debug("Flushing "+event.getUei());
	      int nodeid = 0;
	      if (event.hasNodeid())
	          nodeid = (int) event.getNodeid();

          if (event.hasIfIndex() && event.getIfIndex() > 0 ) {
              event.setIfAlias(getIfAlias(nodeid,event.getIfIndex()));
          } else if (event.getInterface() != null && !event.getInterface().equals("0.0.0.0")) {
              OnmsSnmpInterface iface = getIfAlias(nodeid,event.getInterface()); 
              if (iface != null ) {
                  event.setIfIndex(iface.getIfIndex());
                  event.setIfAlias(iface.getIfAlias());
              } else {
                  event.setIfIndex(-1);
                  event.setIfAlias("-1");
              }
          } else {
              event.setIfIndex(-1);
              event.setIfAlias("-1");
          }
	         
		synchronized (activeSessions){
			cleanActiveSessions();
			Iterator<InsSession> it = activeSessions.iterator();
			while(it.hasNext()){			
				InsSession insSession = it.next();
				PrintStream ps = insSession.getStreamToClient();
				synchronized (ps) {
					if(ps!=null){
						try {
							event.marshal(new PrintWriter(ps));
						} catch (Exception e) {
							log.error("Error while sending current event to client"+e);
						}
					}	
				}
				
			}
		}
	}

	public static void main(String[] args) {
		InsServerListener isl = new InsServerListener();
		isl.setListeningPort(8155);
		//optional (if not setted, no authentication is required)
		isl.setSharedASCIIString("1234567890");
		
		isl.setCriteriaRestriction("eventuei is not null");
		//required properties
		
		
		isl.start();

	}

}



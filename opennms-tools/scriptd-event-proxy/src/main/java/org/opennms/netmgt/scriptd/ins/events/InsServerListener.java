package org.opennms.netmgt.scriptd.ins.events;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;




/**
 * The InsServerListener will accept input from a socket, create a InsSession in
 * which communicate with an InsClient for the alarm synchronization.
 * 
 * @see main method for usage example 
 */

public class InsServerListener extends InsServerAbstractListener {
	
	private Category log;

	private ServerSocket listener;
	
	private Set<InsSession> activeSessions=new HashSet<InsSession>();

	/**
	 * listens for incoming connection on defined port (default is 8154)
	 */
	public void run() {
		log=ThreadCategory.getInstance(this.getClass());
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
		log.info("InsServerListener Interrupted!");
		try {
			listener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.interrupt();
	}
	
	private synchronized void cleanActiveSessions(){
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
		log.debug("Flushing "+event.getUei());
		
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
		//isl.setSharedASCIIString("1234567890");
		
		//required properties
		
		isl.start();

	}

}



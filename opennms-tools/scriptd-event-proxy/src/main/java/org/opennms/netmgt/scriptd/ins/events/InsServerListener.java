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

public class InsServerListener extends Thread {
	
	private Category log;

	private final static int DEFAULT_LISTENING_PORT = 8154;

	/**
	 * the port on which server listens
	 */
	private int listeningPort = DEFAULT_LISTENING_PORT;
	
	/**
	 * the shared string for client authentication
	 * If the shared string is not setted, then server doesn't require authentication 
	 */
	private String sharedASCIIString = null;
	
	/**
	 * The uei of the event representing an alarm
	 */
	private String alarmUEI;
	
	/**
	 * The uei of the event to clear corrensponding alarm
	 */
	private String clearAlarmUEI;
	
	private ServerSocket listener;
	
	private Set<InsSession> activeSessions=new HashSet<InsSession>();

	public int getListeningPort() {
		return listeningPort;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}
	
	public void setSharedASCIIString(String sharedASCIIString) {
		this.sharedASCIIString = sharedASCIIString;
	}
	
	public String getSharedASCIIString() {
		return sharedASCIIString;
	}
	
	public String getAlarmUEI() {
		return alarmUEI;
	}

	public void setAlarmUEI(String alarmUEI) {
		this.alarmUEI = alarmUEI;
	}

	public String getClearAlarmUEI() {
		return clearAlarmUEI;
	}

	public void setClearAlarmUEI(String clearAlarmUEI) {
		this.clearAlarmUEI = clearAlarmUEI;
	}

	/**
	 * listens for incoming connection on defined port (default is 8154)
	 */
	public void run() {
		log=ThreadCategory.getInstance(this.getClass());
		if(alarmUEI==null)
			throw new IllegalStateException("The property alarmUEI cannot be null!");
		if(clearAlarmUEI==null)
			throw new IllegalStateException("The property clearAlarmUEI cannot be null!");
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
				if(sharedASCIIString!=null)
					session.setSharedAsciiAuthString(sharedASCIIString);
				session.setAlarmUEI(alarmUEI);
				session.setClearAlarmUEI(clearAlarmUEI);
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
        isl.setAlarmUEI("uei.opennms.org/nodes/nodeUp");
        isl.setClearAlarmUEI("uei.opennms.org/nodes/nodeDown");
		
		
		isl.start();

	}

}



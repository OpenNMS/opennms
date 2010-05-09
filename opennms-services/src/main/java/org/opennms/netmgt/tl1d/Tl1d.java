/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfree.util.Log;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.Tl1ConfigurationDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.InitializingBean;

/**
 * OpenNMS TL1 Daemon!
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@EventListener(name="OpenNMS:Tl1d")
public class Tl1d extends AbstractServiceDaemon implements PausableFiber, InitializingBean {

    /*
     * The last status sent to the service control manager.
     */
    private volatile int m_status = START_PENDING;
    private volatile Thread m_tl1MesssageProcessor;
    private volatile EventForwarder m_eventForwarder;
	private volatile Tl1ConfigurationDao m_configurationDao;

    private final BlockingQueue<Tl1AutonomousMessage> m_tl1Queue = new LinkedBlockingQueue<Tl1AutonomousMessage>();
    private final List<Tl1Client> m_tl1Clients = new ArrayList<Tl1Client>();

    public Tl1d() {
        super("OpenNMS.Tl1d");
    }
	
    /**
     * 
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleRelooadConfigurationEvent(Event e) {
        

        if (isReloadConfigEventTarget(e)) {
            EventBuilder ebldr = null;
            try {
                stopListeners();
                removeClients();
                /*
                 * leave everything currently on the queue, no need to mess with that, might want a handler
                 * someday for emptying the current queue on a reload event or even a pause clients or something.
                 * 
                 * Don't interrupt message processor, it simply waits on the queue from something to be added.
                 * 
                 */

                m_configurationDao.update();

                initializeTl1Connections();

                startClients();

                log().debug("handleReloadConfigurationEvent: "+m_tl1Clients.size()+" defined.");
                log().info("handleReloadConfigurationEvent: completed.");
                
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Tl1d");

            } catch (Exception exception) {
                log().error("handleReloadConfigurationEvent: failed.", exception);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Tl1d");
                ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
            }
            
            if (ebldr != null) {
                m_eventForwarder.sendNow(ebldr.getEvent());
            }

        }
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParms().getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Tl1d".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Tl1d was target of reload event: "+isTarget);
        return isTarget;
    }

    public synchronized void onInit() {
        initializeTl1Connections();  
    }

    public synchronized void onStart() {
        log().info("onStart: Initializing Tl1d message processing." );
        
        m_tl1MesssageProcessor = new Thread("Tl1-Message-Processor") {
            public void run() {
                doMessageProcessing();
            }
        };

        log().info("onStart: starting message processing thread...");
        m_tl1MesssageProcessor.start();
        log().info("onStart: message processing thread started.");

        startClients();
        
        log().info("onStart: Finished Initializing Tl1d connections.");
    }

    private void startClients() {
        log().info("startClients: starting clients...");
        
        for (Tl1Client client : m_tl1Clients) {
            log().debug("startClients: starting client: "+client);
            client.start();
            log().debug("startClients: started client.");
        }
        
        log().info("startClients: clients started.");
    }

	public synchronized void onStop() {
		stopListeners();
        m_tl1MesssageProcessor.interrupt();
		removeClients();
	}
	
	private void removeClients() {
	    
	    log().info("removeClients: removing current set of defined TL1 clients...");
	    
	    Iterator<Tl1Client> it = m_tl1Clients.iterator();
	    while (it.hasNext()) {
            Tl1Client client = it.next();
            
            log().debug("removeClients: removing client: "+client);
            
            client = null;
            it.remove();
        }
	    
	    log().info("removeClients: all clients removed.");
	}

    private void stopListeners() {
        log().info("stopListeners: calling stop on all clients...");
        
        for (Tl1Client client : m_tl1Clients) {
            log().debug("stopListeners: calling stop on client: "+client);
			client.stop();
		}
        
        log().info("stopListeners: clients stopped.");
    }

    private void initializeTl1Connections() {
        log().info("onInit: Initializing Tl1d connections..." );
    
        List<Tl1Element> configElements = m_configurationDao.getElements();
    
        for(Tl1Element element : configElements) {
            try {
                Tl1Client client = (Tl1Client) Class.forName(element.getTl1ClientApi()).newInstance();
                
                log().debug("initializeTl1Connections: initializing client: "+client);
                
                client.setHost(element.getHost());
                client.setPort(element.getPort());
                client.setTl1Queue(m_tl1Queue);
                client.setMessageProcessor((Tl1AutonomousMessageProcessor) Class.forName(element.getTl1MessageParser()).newInstance());
                client.setLog(log());
                client.setReconnectionDelay(element.getReconnectDelay());
                m_tl1Clients.add(client);
                
                log().debug("initializeTl1Connections: client initialized.");
            } catch (InstantiationException e) {
                log().error("onInit: could not instantiate specified class.", e);
            } catch (IllegalAccessException e) {
                log().error("onInit: could not access specified class.", e);
            } catch (ClassNotFoundException e) {
                log().error("onInit: could not find specified class.", e);
            }
        }

        log().info("onInit: Finished Initializing Tl1d connections.");
    }

    private void processMessage(Tl1AutonomousMessage message) {
        
        log().debug("processMessage: Processing message: "+message);

        EventBuilder bldr = new EventBuilder(Tl1AutonomousMessage.UEI, "Tl1d");
        bldr.setHost(message.getHost());
        bldr.setInterface(message.getHost()); //interface is the IP
        bldr.setService("TL-1"); //Service it TL-1
        bldr.setSeverity(message.getId().getHighestSeverity());
        
        bldr.setTime(message.getTimestamp());
        bldr.addParam("raw-message", message.getRawMessage());
        bldr.addParam("alarm-code", message.getId().getAlarmCode());
        bldr.addParam("atag", message.getId().getAlarmTag());
        bldr.addParam("verb", message.getId().getVerb());
        bldr.addParam("autoblock", message.getAutoBlock().getBlock());
        bldr.addParam("aid",message.getAutoBlock().getAid());
        bldr.addParam("additionalParams",message.getAutoBlock().getAdditionalParams());
        
        m_eventForwarder.sendNow(bldr.getEvent());
        
        log().debug("processMessage: Message processed: "+ message);
    }


    /**
     * Returns the current status of the service.
     * 
     * @return The service's status.
     */
    public synchronized int getStatus() {
        return m_status;
    }

    private void doMessageProcessing() {
        log().debug("doMessageProcessing: Processing messages.");
        boolean cont = true;
        while (cont ) {
            try {
                log().debug("doMessageProcessing: taking message from queue..");
                
                Tl1AutonomousMessage message = m_tl1Queue.take();
                
                log().debug("doMessageProcessing: message taken: "+message);
                
                processMessage(message);
            } catch (InterruptedException e) {
                Log.warn("doMessageProcessing: received interrupt: "+e, e);
            }
        }
        
        log().debug("doMessageProcessing: Exiting processing messages.");
    }
    
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setConfigurationDao(Tl1ConfigurationDao configurationDao) {
        m_configurationDao = configurationDao;
    }


}

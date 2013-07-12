/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.tl1d;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.Tl1ConfigurationDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenNMS TL1 Daemon!
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name="OpenNMS:Tl1d", logPrefix="tl1d")
public class Tl1d extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Tl1d.class);

    private static final String LOG4J_CATEGORY = "tl1d";

	/*
     * The last status sent to the service control manager.
     */
    private volatile int m_status = START_PENDING;
    private volatile Thread m_tl1MesssageProcessor;
    private volatile EventForwarder m_eventForwarder;
	private volatile Tl1ConfigurationDao m_configurationDao;

    private final BlockingQueue<Tl1AutonomousMessage> m_tl1Queue = new LinkedBlockingQueue<Tl1AutonomousMessage>();
    private final List<Tl1Client> m_tl1Clients = new ArrayList<Tl1Client>();

    /**
     * <p>Constructor for Tl1d.</p>
     */
    public Tl1d() {
        super(LOG4J_CATEGORY);
    }
	
    /**
     * <p>handleRelooadConfigurationEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
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

                LOG.debug("handleReloadConfigurationEvent: {} defined.", m_tl1Clients.size());
                LOG.info("handleReloadConfigurationEvent: completed.");
                
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Tl1d");

            } catch (Throwable exception) {
                LOG.error("handleReloadConfigurationEvent: failed.", exception);
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
        
        List<Parm> parmCollection = event.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Tl1d".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        LOG.debug("isReloadConfigEventTarget: Tl1d was target of reload event: {}", isTarget);
        return isTarget;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    public synchronized void onInit() {
        initializeTl1Connections();  
    }

    /**
     * <p>onStart</p>
     */
    @Override
    public synchronized void onStart() {
        LOG.info("onStart: Initializing Tl1d message processing.");
        
        m_tl1MesssageProcessor = new Thread("Tl1-Message-Processor") {
            @Override
            public void run() {
                doMessageProcessing();
            }
        };

        LOG.info("onStart: starting message processing thread...");
        m_tl1MesssageProcessor.start();
        LOG.info("onStart: message processing thread started.");

        startClients();
        
        LOG.info("onStart: Finished Initializing Tl1d connections.");
    }

    private void startClients() {
        LOG.info("startClients: starting clients...");
        
        for (Tl1Client client : m_tl1Clients) {
            LOG.debug("startClients: starting client: {}", client);
            client.start();
            LOG.debug("startClients: started client.");
        }
        
        LOG.info("startClients: clients started.");
    }

	/**
	 * <p>onStop</p>
	 */
    @Override
	public synchronized void onStop() {
		stopListeners();
        m_tl1MesssageProcessor.interrupt();
		removeClients();
	}
	
	private void removeClients() {
	    
	    LOG.info("removeClients: removing current set of defined TL1 clients...");
	    
	    Iterator<Tl1Client> it = m_tl1Clients.iterator();
	    while (it.hasNext()) {
            Tl1Client client = it.next();
            
            LOG.debug("removeClients: removing client: {}", client);
            
            client = null;
            it.remove();
        }
	    
	    LOG.info("removeClients: all clients removed.");
	}

    private void stopListeners() {
        LOG.info("stopListeners: calling stop on all clients...");
        
        for (Tl1Client client : m_tl1Clients) {
            LOG.debug("stopListeners: calling stop on client: {}", client);
			client.stop();
		}
        
        LOG.info("stopListeners: clients stopped.");
    }

    private void initializeTl1Connections() {
        LOG.info("onInit: Initializing Tl1d connections...");
    
        List<Tl1Element> configElements = m_configurationDao.getElements();
    
        for(Tl1Element element : configElements) {
            try {
                Tl1Client client = (Tl1Client) Class.forName(element.getTl1ClientApi()).newInstance();
                
                LOG.debug("initializeTl1Connections: initializing client: {}", client);
                
                client.setHost(element.getHost());
                client.setPort(element.getPort());
                client.setTl1Queue(m_tl1Queue);
                client.setMessageProcessor((Tl1AutonomousMessageProcessor) Class.forName(element.getTl1MessageParser()).newInstance());
                client.setReconnectionDelay(element.getReconnectDelay());
                m_tl1Clients.add(client);
                
                LOG.debug("initializeTl1Connections: client initialized.");
            } catch (InstantiationException e) {
                LOG.error("onInit: could not instantiate specified class.", e);
            } catch (IllegalAccessException e) {
                LOG.error("onInit: could not access specified class.", e);
            } catch (ClassNotFoundException e) {
                LOG.error("onInit: could not find specified class.", e);
            }
        }

        LOG.info("onInit: Finished Initializing Tl1d connections.");
    }

    private void processMessage(Tl1AutonomousMessage message) {
        
        LOG.debug("processMessage: Processing message: {}", message);

        EventBuilder bldr = new EventBuilder(Tl1AutonomousMessage.UEI, "Tl1d");
        bldr.setHost(message.getHost());
        bldr.setInterface(addr(message.getHost())); //interface is the IP
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
        
        LOG.debug("processMessage: Message processed: {}", message);
    }


    /**
     * Returns the current status of the service.
     *
     * @return The service's status.
     */
    @Override
    public synchronized int getStatus() {
        return m_status;
    }

    private void doMessageProcessing() {
        LOG.debug("doMessageProcessing: Processing messages.");
        boolean cont = true;
        while (cont ) {
            try {
                LOG.debug("doMessageProcessing: taking message from queue..");
                
                Tl1AutonomousMessage message = m_tl1Queue.take();
                
                LOG.debug("doMessageProcessing: message taken: {}", message);
                
                processMessage(message);
            } catch (InterruptedException e) {
                LOG.warn("doMessageProcessing: received interrupt", e);
            }
        }
        
        LOG.debug("doMessageProcessing: Exiting processing messages.");
    }
    
    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>setConfigurationDao</p>
     *
     * @param configurationDao a {@link org.opennms.netmgt.dao.api.Tl1ConfigurationDao} object.
     */
    public void setConfigurationDao(Tl1ConfigurationDao configurationDao) {
        m_configurationDao = configurationDao;
    }


}
